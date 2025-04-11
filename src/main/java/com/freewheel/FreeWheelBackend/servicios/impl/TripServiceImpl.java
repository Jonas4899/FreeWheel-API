/*
package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.DriverDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.TripDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.TripSearchCriteriaDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.DriverEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.TripEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.VehicleEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.DriverRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.TripRepository;
import org.springframework.transaction.annotation.Transactional;
import com.freewheel.FreeWheelBackend.servicios.TripService;
import jakarta.persistence.criteria.Predicate; // <--- Importar Predicate
import org.slf4j.Logger; // <--- Importar Logger
import org.slf4j.LoggerFactory; // <--- Importar LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification; // <--- Importar Specification
import org.springframework.stereotype.Service;

import java.util.ArrayList; // <--- Importar ArrayList
import java.util.List; // <--- Importar List
import java.util.stream.Collectors; // <--- Importar Collectors

@Service
public class TripServiceImpl implements TripService {
    private static final Logger logger = LoggerFactory.getLogger(TripServiceImpl.class);
    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;

    public TripServiceImpl(TripRepository tripRepository, DriverRepository driverRepository) {
        this.tripRepository = tripRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public TripDTO createTrip(TripDTO tripDTO) {
        DriverEntity driver = driverRepository.findById(tripDTO.getConductorId())
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        TripEntity savedTrip = TripEntity.builder()
                .conductorId(tripDTO.getConductorId())
                .fecha(tripDTO.getFecha())
                .horaInicio(tripDTO.getHoraInicio())
                .horaFin(tripDTO.getHoraFin())
                .precioAsiento(tripDTO.getPrecioAsiento())
                .asientosDisponibles(tripDTO.getAsientosDisponibles())
                .direccionOrigen(tripDTO.getDireccionOrigen())
                .latitudOrigen(tripDTO.getLatitudOrigen())
                .longitudDestino(tripDTO.getLongitudDestino())
                .direccionDestino(tripDTO.getDireccionDestino())
                .latitudDestino(tripDTO.getLatitudDestino())
                .longitudDestino(tripDTO.getLongitudDestino())
                .estado(tripDTO.getEstado())
                .build();

        savedTrip = tripRepository.save(savedTrip);

        return TripDTO.builder()
                .id(savedTrip.getId())
                .conductorId(savedTrip.getConductorId())
                .fecha(savedTrip.getFecha())
                .horaInicio(savedTrip.getHoraInicio())
                .horaFin(savedTrip.getHoraFin())
                .precioAsiento(savedTrip.getPrecioAsiento())
                .asientosDisponibles(savedTrip.getAsientosDisponibles())
                .direccionOrigen(savedTrip.getDireccionOrigen())
                .latitudOrigen(savedTrip.getLatitudOrigen())
                .longitudDestino(savedTrip.getLongitudDestino())
                .direccionDestino(savedTrip.getDireccionDestino())
                .latitudDestino(savedTrip.getLatitudDestino())
                .longitudDestino(savedTrip.getLongitudDestino())
                .estado(savedTrip.getEstado())
                .build();
    }

    @Override
    @Transactional(readOnly = true) //  Read-only transaction for search
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

            // 5. Filter by origin proximity
            if (criteria.getLatitudOrigenBusqueda() != null && criteria.getLongitudOrigenBusqueda() != null && criteria.getRadioBusquedaKm() != null) {
                double radioMetros = criteria.getRadioBusquedaKm() * 1000.0;

                // Corrected ST_DWithin for PostgreSQL (and other databases)
                Predicate origenCercano = criteriaBuilder.isTrue(
                        criteriaBuilder.function(
                                "ST_DWithin",  // Function name
                                Boolean.class,
                                criteriaBuilder.function(
                                        "ST_SetSRID",
                                        Object.class,
                                        criteriaBuilder.function(
                                                "ST_MakePoint",
                                                Object.class,
                                                root.get("longitudOrigen"),
                                                root.get("latitudOrigen")
                                        ),
                                        criteriaBuilder.literal(4326)
                                ),
                                criteriaBuilder.function(
                                        "ST_SetSRID",
                                        Object.class,
                                        criteriaBuilder.function(
                                                "ST_MakePoint",
                                                Object.class,
                                                criteriaBuilder.literal(criteria.getLongitudOrigenBusqueda()), // Use literal() here
                                                criteriaBuilder.literal(criteria.getLatitudOrigenBusqueda())    // And here
                                        ),
                                        criteriaBuilder.literal(4326)
                                ),
                                criteriaBuilder.literal(radioMetros),
                                criteriaBuilder.literal(true) // use_spheroid for PostGIS
                        )
                );
                predicates.add(origenCercano);
                logger.trace("Añadido predicado: Origen dentro de {} metros de ({}, {})", radioMetros, criteria.getLatitudOrigenBusqueda(), criteria.getLongitudOrigenBusqueda());
            }

            // 6. Filter by destination proximity
            if (criteria.getLatitudDestinoBusqueda() != null && criteria.getLongitudDestinoBusqueda() != null && criteria.getRadioBusquedaKm() != null) {
                double radioMetros = criteria.getRadioBusquedaKm() * 1000.0;
                Predicate destinoCercano = criteriaBuilder.isTrue(
                        criteriaBuilder.function(
                                "ST_DWithin",
                                Boolean.class,
                                criteriaBuilder.function(
                                        "ST_SetSRID",
                                        Object.class,
                                        criteriaBuilder.function(
                                                "ST_MakePoint",
                                                Object.class,
                                                root.get("longitudDestino"),
                                                root.get("latitudDestino")
                                        ),
                                        criteriaBuilder.literal(4326)
                                ),
                                criteriaBuilder.function(
                                        "ST_SetSRID",
                                        Object.class,
                                        criteriaBuilder.function(
                                                "ST_MakePoint",
                                                Object.class,
                                                criteriaBuilder.literal(criteria.getLongitudDestinoBusqueda()), // Use literal() here
                                                criteriaBuilder.literal(criteria.getLatitudDestinoBusqueda())    // And here
                                        ),
                                        criteriaBuilder.literal(4326)
                                ),
                                criteriaBuilder.literal(radioMetros),
                                criteriaBuilder.literal(true)
                        )
                );
                predicates.add(destinoCercano);
                logger.trace("Añadido predicado: Destino dentro de {} metros de ({}, {})", radioMetros, criteria.getLatitudDestinoBusqueda(), criteria.getLongitudDestinoBusqueda());
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<TripEntity> viajesEncontrados = tripRepository.findAll(spec);
        logger.info("Encontrados {} viajes.", viajesEncontrados.size());
        return viajesEncontrados.stream()
                .map(this::mapToTripDTO)
                .collect(Collectors.toList());
    }

    private TripDTO mapToTripDTO(TripEntity entity) {
        return TripDTO.builder()
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
                .estado(entity.getEstado())
                .build();
    }
}
*/
package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.TripDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.TripSearchCriteriaDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.DriverEntity; // Asegúrate que este import sea necesario aquí o si debe estar en el servicio que lo usa
import com.freewheel.FreeWheelBackend.persistencia.entidades.TripEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.DriverRepository; // Asegúrate que este import sea necesario aquí
import com.freewheel.FreeWheelBackend.persistencia.repositorios.TripRepository;
import com.freewheel.FreeWheelBackend.servicios.TripService;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Puedes quitarlo si usas inyección por constructor final
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import org.springframework.util.StringUtils; // No parece usarse, se puede quitar

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TripServiceImpl implements TripService {

    private static final Logger logger = LoggerFactory.getLogger(TripServiceImpl.class);

    private final TripRepository tripRepository;
    // DriverRepository solo es necesario si createTrip necesita validar el conductor aquí.
    // Si la validación ocurre antes (ej. en el controlador o un servicio de validación),
    // podrías quitar esta dependencia de TripServiceImpl.
    private final DriverRepository driverRepository;

    // Inyección por constructor (preferida sobre @Autowired en campos)
    public TripServiceImpl(TripRepository tripRepository, DriverRepository driverRepository) {
        this.tripRepository = tripRepository;
        this.driverRepository = driverRepository; // Necesario para la validación actual en createTrip
    }

    @Override
    @Transactional // Asegura la atomicidad de la creación
    public TripDTO createTrip(TripDTO tripDTO) {
        logger.debug("Iniciando creación de viaje: {}", tripDTO);

        // Validación básica del conductor (Considera mover a una capa de validación)
        driverRepository.findById(tripDTO.getConductorId())
                .orElseThrow(() -> {
                    logger.error("Intento de crear viaje fallido: Conductor no encontrado con ID: {}", tripDTO.getConductorId());
                    // Lanza una excepción más específica si tienes una definida
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
                .longitudOrigen(tripDTO.getLongitudOrigen()) // <-- CORREGIDO
                .direccionDestino(tripDTO.getDireccionDestino())
                .latitudDestino(tripDTO.getLatitudDestino())
                .longitudDestino(tripDTO.getLongitudDestino())
                .estado(tripDTO.getEstado() != null ? tripDTO.getEstado() : "por iniciar") // Estado por defecto
                .build();

        try {
            TripEntity savedTrip = tripRepository.save(tripEntity);
            logger.info("Viaje creado exitosamente con ID: {}", savedTrip.getId());
            return mapToTripDTO(savedTrip);
        } catch (Exception e) {
            // Captura excepciones de persistencia si es necesario loggear/manejar específicamente
            logger.error("Error al guardar el viaje en la base de datos", e);
            // Relanza o maneja la excepción según tu estrategia de manejo de errores
            throw new RuntimeException("Error al guardar el viaje", e);
        }
    }

    @Override
    @Transactional(readOnly = true) // Optimiza para consultas de solo lectura
    public List<TripDTO> buscarViajesDisponibles(TripSearchCriteriaDTO criteria) {
        logger.debug("Buscando viajes con criterios: {}", criteria);

        Specification<TripEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter by estado (asegúrate que 'por iniciar' sea el estado correcto)
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
                    // Decide si lanzar error o ignorar el filtro de radio
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
            // Mapea las entidades a DTOs para la respuesta
            return viajesEncontrados.stream()
                    .map(this::mapToTripDTO) // Usa el método helper corregido
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Captura excepciones durante la ejecución de la consulta (puede ser por la función espacial)
            logger.error("Error al ejecutar la consulta de búsqueda de viajes", e);
            // Lanza una excepción personalizada o una RuntimeException
            throw new RuntimeException("Error al buscar viajes disponibles", e);
        }
    }

    // Método helper para mapear de Entidad a DTO
    private TripDTO mapToTripDTO(TripEntity entity) {
        if (entity == null) {
            return null; // O lanzar excepción si una entidad nula no es esperada
        }
        return TripDTO.builder()
                .id(entity.getId())
                .conductorId(entity.getConductorId())
                .fecha(entity.getFecha())
                .horaInicio(entity.getHoraInicio())
                .horaFin(entity.getHoraFin())
                .precioAsiento(entity.getPrecioAsiento())
                .asientosDisponibles(entity.getAsientosDisponibles())
                .direccionOrigen(entity.getDireccionOrigen())
                .latitudOrigen(entity.getLatitudOrigen())
                .longitudOrigen(entity.getLongitudOrigen()) // <-- CORREGIDO
                .direccionDestino(entity.getDireccionDestino())
                .latitudDestino(entity.getLatitudDestino())
                .longitudDestino(entity.getLongitudDestino())
                .estado(entity.getEstado())
                .build();
    }
}
