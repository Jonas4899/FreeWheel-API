package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.TripDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.TripSearchCriteriaDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.*;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.*;
import com.freewheel.FreeWheelBackend.servicios.TripService;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TripServiceImpl implements TripService {

    private static final Logger logger = LoggerFactory.getLogger(TripServiceImpl.class);

    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final SolicitudReservaRepository solicitudReservaRepository;

    // Inyección por constructor con todos los repositorios necesarios
    public TripServiceImpl(
            TripRepository tripRepository, 
            DriverRepository driverRepository,
            UserRepository userRepository,
            VehicleRepository vehicleRepository,
            SolicitudReservaRepository solicitudReservaRepository) {
        this.tripRepository = tripRepository;
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.solicitudReservaRepository = solicitudReservaRepository;
    }

    @Override
    @Transactional 
    public TripDTO createTrip(TripDTO tripDTO) {
        logger.debug("Iniciando creación de viaje: {}", tripDTO);

        // Validación básica del conductor
        driverRepository.findById(tripDTO.getConductorId())
                .orElseThrow(() -> {
                    logger.error("Intento de crear viaje fallido: Conductor no encontrado con ID: {}", tripDTO.getConductorId());
                    return new RuntimeException("Conductor no encontrado con ID: " + tripDTO.getConductorId());
                });

        TripEntity tripEntity = TripEntity.builder()
                .conductorId(tripDTO.getConductorId())
                .fecha(tripDTO.getFecha())
                .horaInicio(tripDTO.getHoraInicio())
                .horaFin(tripDTO.getHoraFin())
                .precioAsiento(tripDTO.getPrecioAsiento())
                .asientosDisponibles(tripDTO.getAsientosDisponibles())
                .direccionOrigen(tripDTO.getDireccionOrigen())
                .latitudOrigen(tripDTO.getLatitudOrigen())
                .longitudOrigen(tripDTO.getLongitudOrigen())
                .direccionDestino(tripDTO.getDireccionDestino())
                .latitudDestino(tripDTO.getLatitudDestino())
                .longitudDestino(tripDTO.getLongitudDestino())
                .estado(tripDTO.getEstado() != null ? tripDTO.getEstado() : "por iniciar")
                .build();

        try {
            TripEntity savedTrip = tripRepository.save(tripEntity);
            logger.info("Viaje creado exitosamente con ID: {}", savedTrip.getId());
            return mapToTripDTO(savedTrip);
        } catch (Exception e) {
            logger.error("Error al guardar el viaje en la base de datos", e);
            throw new RuntimeException("Error al guardar el viaje", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDTO> buscarViajesDisponibles(TripSearchCriteriaDTO criteria) {
        logger.debug("Buscando viajes con criterios: {}", criteria);

        Specification<TripEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter by estado
            predicates.add(criteriaBuilder.equal(root.get("estado"), "por iniciar"));
            logger.trace("Añadido predicado: estado = 'por iniciar'");

            // 2. Filter by fecha
            if (criteria.getFecha() != null) {
                predicates.add(criteriaBuilder.equal(root.get("fecha"), criteria.getFecha()));
                logger.trace("Añadido predicado: fecha = {}", criteria.getFecha());
            }

            // 3. Filter by horaInicio range
            if (criteria.getHoraInicioDesde() != null && criteria.getHoraInicioHasta() != null) {
                predicates.add(criteriaBuilder.between(root.get("horaInicio"), criteria.getHoraInicioDesde(), criteria.getHoraInicioHasta()));
                logger.trace("Añadido predicado: horaInicio BETWEEN {} AND {}", criteria.getHoraInicioDesde(), criteria.getHoraInicioHasta());
            } else if (criteria.getHoraInicioDesde() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("horaInicio"), criteria.getHoraInicioDesde()));
                logger.trace("Añadido predicado: horaInicio >= {}", criteria.getHoraInicioDesde());
            } else if (criteria.getHoraInicioHasta() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("horaInicio"), criteria.getHoraInicioHasta()));
                logger.trace("Añadido predicado: horaInicio <= {}", criteria.getHoraInicioHasta());
            }

            // 4. Filter by asientosDisponibles
            if (criteria.getNumeroAsientosRequeridos() != null && criteria.getNumeroAsientosRequeridos() > 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("asientosDisponibles"), criteria.getNumeroAsientosRequeridos()));
                logger.trace("Añadido predicado: asientosDisponibles >= {}", criteria.getNumeroAsientosRequeridos());
            }

            // --- Filtros de Proximidad Geográfica (Requieren PostGIS habilitado en BD) ---

            // 5. Filter by origin proximity
            if (criteria.getLatitudOrigenBusqueda() != null && criteria.getLongitudOrigenBusqueda() != null && criteria.getRadioBusquedaKm() != null) {
                if (criteria.getRadioBusquedaKm() <= 0) {
                    logger.warn("Radio de búsqueda de origen inválido (<= 0): {}", criteria.getRadioBusquedaKm());
                } else {
                    double radioMetros = criteria.getRadioBusquedaKm() * 1000.0;
                    Predicate origenCercano = criteriaBuilder.isTrue(
                            criteriaBuilder.function(
                                    "ST_DWithin", Boolean.class,
                                    criteriaBuilder.function("ST_SetSRID", Object.class,
                                            criteriaBuilder.function("ST_MakePoint", Object.class, root.get("longitudOrigen"), root.get("latitudOrigen")),
                                            criteriaBuilder.literal(4326) // SRID WGS84
                                    ),
                                    criteriaBuilder.function("ST_SetSRID", Object.class,
                                            criteriaBuilder.function("ST_MakePoint", Object.class,
                                                    criteriaBuilder.literal(criteria.getLongitudOrigenBusqueda()),
                                                    criteriaBuilder.literal(criteria.getLatitudOrigenBusqueda())
                                            ),
                                            criteriaBuilder.literal(4326) // SRID WGS84
                                    ),
                                    criteriaBuilder.literal(radioMetros),
                                    criteriaBuilder.literal(true) // use_spheroid (PostGIS)
                            )
                    );
                    predicates.add(origenCercano);
                    logger.trace("Añadido predicado: Origen dentro de {} metros de ({}, {})", radioMetros, criteria.getLatitudOrigenBusqueda(), criteria.getLongitudOrigenBusqueda());
                }
            }

            // 6. Filter by destination proximity
            if (criteria.getLatitudDestinoBusqueda() != null && criteria.getLongitudDestinoBusqueda() != null && criteria.getRadioBusquedaKm() != null) {
                if (criteria.getRadioBusquedaKm() <= 0) {
                    logger.warn("Radio de búsqueda de destino inválido (<= 0): {}", criteria.getRadioBusquedaKm());
                } else {
                    double radioMetros = criteria.getRadioBusquedaKm() * 1000.0;
                    Predicate destinoCercano = criteriaBuilder.isTrue(
                            criteriaBuilder.function(
                                    "ST_DWithin", Boolean.class,
                                    criteriaBuilder.function("ST_SetSRID", Object.class,
                                            criteriaBuilder.function("ST_MakePoint", Object.class, root.get("longitudDestino"), root.get("latitudDestino")),
                                            criteriaBuilder.literal(4326)
                                    ),
                                    criteriaBuilder.function("ST_SetSRID", Object.class,
                                            criteriaBuilder.function("ST_MakePoint", Object.class,
                                                    criteriaBuilder.literal(criteria.getLongitudDestinoBusqueda()),
                                                    criteriaBuilder.literal(criteria.getLatitudDestinoBusqueda())
                                            ),
                                            criteriaBuilder.literal(4326)
                                    ),
                                    criteriaBuilder.literal(radioMetros),
                                    criteriaBuilder.literal(true) // use_spheroid (PostGIS)
                            )
                    );
                    predicates.add(destinoCercano);
                    logger.trace("Añadido predicado: Destino dentro de {} metros de ({}, {})", radioMetros, criteria.getLatitudDestinoBusqueda(), criteria.getLongitudDestinoBusqueda());
                }
            }

            // Construye la consulta final combinando todos los predicados con AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        try {
            List<TripEntity> viajesEncontrados = tripRepository.findAll(spec);
            logger.info("Búsqueda completada. Encontrados {} viajes.", viajesEncontrados.size());
            
            // Mapea las entidades a DTOs para la respuesta, incluyendo info de conductor y vehículo
            return viajesEncontrados.stream()
                    .map(this::mapToTripDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error al ejecutar la consulta de búsqueda de viajes", e);
            throw new RuntimeException("Error al buscar viajes disponibles", e);
        }
    }

    @Override
    @Transactional
    public TripDTO iniciarViaje(Long tripId) {
        logger.debug("Iniciando viaje con ID: {}", tripId);

        TripEntity tripEntity = tripRepository.findById(tripId)
                .orElseThrow(() -> {
                    logger.error("Viaje no encontrado con ID: {}", tripId);
                    return new RuntimeException("Viaje no encontrado con ID: " + tripId);
                });

        // Verificamos que el viaje esté en estado 'por iniciar'
        if (!"por iniciar".equals(tripEntity.getEstado())) {
            logger.warn("No se puede iniciar el viaje ID: {} porque su estado actual es: {}",
                        tripId, tripEntity.getEstado());
            throw new IllegalStateException(
                "No se puede iniciar el viaje porque su estado actual es: " + tripEntity.getEstado());
        }

        // Actualizamos únicamente el estado del viaje
        tripEntity.setEstado("iniciado");

        try {
            TripEntity savedTrip = tripRepository.save(tripEntity);
            logger.info("Viaje con ID: {} iniciado exitosamente", tripId);
            return mapToTripDTO(savedTrip);
        } catch (Exception e) {
            logger.error("Error al iniciar el viaje con ID: {}", tripId, e);
            throw new RuntimeException("Error al iniciar el viaje", e);
        }
    }

    // Método helper para mapear de Entidad a DTO con información adicional
    private TripDTO mapToTripDTO(TripEntity entity) {
        if (entity == null) {
            return null;
        }
        
        TripDTO.TripDTOBuilder builder = TripDTO.builder()
                .id(entity.getId())
                .conductorId(entity.getConductorId())
                .fecha(entity.getFecha())
                .horaInicio(entity.getHoraInicio())
                .horaFin(entity.getHoraFin())
                .precioAsiento(entity.getPrecioAsiento())
                .asientosDisponibles(entity.getAsientosDisponibles())
                .direccionOrigen(entity.getDireccionOrigen())
                .latitudOrigen(entity.getLatitudOrigen())
                .longitudOrigen(entity.getLongitudOrigen())
                .direccionDestino(entity.getDireccionDestino())
                .latitudDestino(entity.getLatitudDestino())
                .longitudDestino(entity.getLongitudDestino())
                .estado(entity.getEstado());
        
        // Obtener información del conductor
        try {
            DriverEntity driverEntity = driverRepository.findById(entity.getConductorId()).orElse(null);
            if (driverEntity != null) {
                UserEntity userEntity = driverEntity.getUsuario();
                if (userEntity != null) {
                    builder.nombreConductor(userEntity.getNombre());
                    builder.apellidoConductor(userEntity.getApellido());
                    builder.fotoConductor(userEntity.getFotoPerfil());
                    builder.telefonoConductor(userEntity.getTelefono());
                    
                    // En un sistema real, aquí calcularíamos la calificación promedio del conductor
                    // basado en calificaciones históricas
                    builder.calificacionConductor(4.5); // Valor de ejemplo
                }
                
                // Buscar el vehículo del conductor
                // Asumimos que el conductor tiene un solo vehículo o tomamos el primero
                // En un sistema real, podríamos necesitar buscar por el vehículo específico del viaje
                List<VehicleEntity> vehiculos = vehicleRepository.findByConductorId(entity.getConductorId());
                if (!vehiculos.isEmpty()) {
                    VehicleEntity vehiculo = vehiculos.get(0);
                    builder.vehiculoPlaca(vehiculo.getPlaca());
                    builder.vehiculoMarca(vehiculo.getMarca());
                    builder.vehiculoModelo(vehiculo.getModelo());
                    builder.vehiculoColor(vehiculo.getColor());
                    builder.vehiculoTipo(vehiculo.getTipo());
                    builder.vehiculoFoto(vehiculo.getFoto());
                }
            }
        } catch (Exception e) {
            logger.warn("No se pudo obtener información completa del conductor o vehículo para el viaje ID: {}", entity.getId(), e);
            // Continuamos con la información básica del viaje
        }
        
        return builder.build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDTO> obtenerViajesPorUsuario(Long userId, boolean esConductor) {
        logger.debug("Buscando viajes para el usuario con ID: {}", userId);

        List<TripEntity> viajes;

        if (esConductor) {
            DriverEntity conductor = driverRepository.findByUsuario_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
            Long conductorId = conductor.getId();
            viajes = tripRepository.findByConductorId(conductorId);
        } else {
            List<SolicitudReservaEntity> solicitudes = solicitudReservaRepository.findAllByPasajero_Id(userId);
            logger.info("Se encontraron {} solicitudes para el usuario con ID: {}", solicitudes.size(), userId);
            List<Long> viajeIds = solicitudes.stream()
                    .map(solicitud -> solicitud.getViaje().getId())
                    .collect(Collectors.toList());

            viajes = tripRepository.findAllById(viajeIds);
        }

        if (viajes.isEmpty()) {
            logger.info("No se encontraron viajes para el usuario con ID: {}", userId);
            return new ArrayList<>();
        } else {
            logger.info("Se encontraron {} viajes para el usuario con ID: {}", viajes.size(), userId);
            return viajes.stream()
                    .map(this::mapToTripDTO)
                    .collect(Collectors.toList());
        }
    }
}
