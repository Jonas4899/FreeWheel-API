package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.SolicitudReservaDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.TripDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.DriverEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.SolicitudReservaEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.TripEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.UserEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.DriverRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.SolicitudReservaRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.TripRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.UserRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.DriverRepository;
import com.freewheel.FreeWheelBackend.servicios.SolicitudReservaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolicitudReservaServiceImpl implements SolicitudReservaService {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudReservaServiceImpl.class);
    
    private final SolicitudReservaRepository solicitudReservaRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;

    public SolicitudReservaServiceImpl(SolicitudReservaRepository solicitudReservaRepository, 
                                      TripRepository tripRepository,
                                      UserRepository userRepository, DriverRepository driverRepository) {
        this.solicitudReservaRepository = solicitudReservaRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudReservaDTO> obtenerHistorialSolicitudesConductor(Long conductorId) {
        logger.debug("Obteniendo historial de solicitudes para el conductor con ID: {}", conductorId);

        List<SolicitudReservaEntity> solicitudes = solicitudReservaRepository.findAllByConductorId(conductorId);
        logger.info("Se encontraron {} solicitudes para el conductor con ID: {}", solicitudes.size(), conductorId);

        List<SolicitudReservaDTO> solicitudesDTO = solicitudes.stream()
                .map(this::mapToSolicitudReservaDTO)
                .collect(Collectors.toList());

        // Ordenar por fecha del viaje (más cercana primero) y luego por hora de inicio
        solicitudesDTO.sort(Comparator.comparing((SolicitudReservaDTO dto) -> {
            TripDTO viaje = dto.getViaje();
            return viaje != null ? viaje.getFecha() : null;
        }).thenComparing((SolicitudReservaDTO dto) -> {
            TripDTO viaje = dto.getViaje();
            return viaje != null ? viaje.getHoraInicio() : null;
        }));

        return solicitudesDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudReservaDTO> obtenerHistorialSolicitudesConductorPorUsuario(Long userId) {
        logger.debug("Buscando conductor asociado al usuario con ID: {}", userId);

        DriverEntity conductor = driverRepository.findByUsuario_Id(userId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        // Ahora sí usamos el método existente con el ID de conductor correcto
        return obtenerHistorialSolicitudesConductor(conductor.getId());
    }

    private SolicitudReservaDTO mapToSolicitudReservaDTO(SolicitudReservaEntity entity) {
        if (entity == null) {
            return null;
        }

        // Obtener datos del viaje completos
        TripDTO tripDTO = null;
        if (entity.getViaje() != null) {
            TripEntity tripEntity = entity.getViaje();
            tripDTO = TripDTO.builder()
                    .id(tripEntity.getId())
                    .conductorId(tripEntity.getConductorId())
                    .fecha(tripEntity.getFecha())
                    .horaInicio(tripEntity.getHoraInicio())
                    .horaFin(tripEntity.getHoraFin())
                    .precioAsiento(tripEntity.getPrecioAsiento())
                    .asientosDisponibles(tripEntity.getAsientosDisponibles())
                    .direccionOrigen(tripEntity.getDireccionOrigen())
                    .latitudOrigen(tripEntity.getLatitudOrigen())
                    .longitudOrigen(tripEntity.getLongitudOrigen())
                    .direccionDestino(tripEntity.getDireccionDestino())
                    .latitudDestino(tripEntity.getLatitudDestino())
                    .longitudDestino(tripEntity.getLongitudDestino())
                    .estado(tripEntity.getEstado())
                    .build();
        }

        // Obtener datos del pasajero
        UserDTO pasajeroDTO = null;
        if (entity.getPasajero() != null) {
            UserEntity pasajeroEntity = entity.getPasajero();
            pasajeroDTO = UserDTO.builder()
                    .id(pasajeroEntity.getId())
                    .correo(pasajeroEntity.getCorreo())
                    .nombre(pasajeroEntity.getNombre())
                    .apellido(pasajeroEntity.getApellido())
                    .telefono(pasajeroEntity.getTelefono())
                    .fotoPerfil(pasajeroEntity.getFotoPerfil())
                    .organizacionCodigo(pasajeroEntity.getOrganizacion() != null ?
                            pasajeroEntity.getOrganizacion().getCodigo() : null)
                    .build();
        }

        return SolicitudReservaDTO.builder()
                .id(entity.getId())
                .viajeId(entity.getViaje() != null ? entity.getViaje().getId() : 0)
                .viaje(tripDTO)
                .pasajeroId(entity.getPasajero() != null ? entity.getPasajero().getId() : 0)
                .pasajero(pasajeroDTO)
                .fechaSolicitud(entity.getFechaSolicitud())
                .estado(entity.getEstado())
                .fechaRespuesta(entity.getFechaRespuesta())
                .mensajePasajero(entity.getMensajePasajero())
                .mensajeConductor(entity.getMensajeConductor())
                .asientosSolicitados(entity.getAsientosSolicitados()) // Añadir asientos solicitados
                .build();
    }

    @Override
    @Transactional
    public boolean aceptarSolicitudReserva(Long id) {
        logger.debug("Aceptando solicitud de reserva con ID: {}", id);

        SolicitudReservaEntity solicitud = solicitudReservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Revisar si hay asientos disponibles
        TripEntity viaje = solicitud.getViaje();

        if(viaje == null) {
            logger.warn("El viaje asociado a la solicitud con ID: {} no existe", id);
            return false;
        }

        // Verificar primero si la solicitud ya fue aceptada
        if("ACEPTADO".equals(solicitud.getEstado())) {
            logger.warn("La solicitud con ID: {} ya ha sido aceptada anteriormente", id);
            return false;
        }

        // Y también verificar el estado del viaje de forma segura
        if(viaje.getEstado() != null && viaje.getEstado().equalsIgnoreCase("ACEPTADO")) {
            logger.warn("El viaje asociado a la solicitud con ID: {} ya ha sido aceptado", id);
            return false;
        }

        if(viaje.getAsientosDisponibles() < solicitud.getAsientosSolicitados()) {
            logger.warn("No hay asientos disponibles para la solicitud con ID: {}", id);
            return false;
        }

        // Actualizar el viaje
        viaje.setAsientosDisponibles(viaje.getAsientosDisponibles() - solicitud.getAsientosSolicitados());
        tripRepository.save(viaje);

        // Actualizar la solicitud zoned date
        solicitud.setFechaRespuesta(java.time.ZonedDateTime.now());

        // Actualizar el estado de la solicitud
        solicitud.setEstado("ACEPTADO");
        solicitudReservaRepository.save(solicitud);

        logger.info("Solicitud de reserva con ID: {} aceptada exitosamente", id);
        return true;
    }

    @Override
    @Transactional
    public boolean rechazarSolicitudReserva(Long id) {
        logger.debug("Rechazando solicitud de reserva con ID: {}", id);

        SolicitudReservaEntity solicitud = solicitudReservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Revisar si la solicitud ya fue aceptada
        if("ACEPTADO".equals(solicitud.getEstado())) {
            logger.warn("La solicitud con ID: {} ya ha sido aceptada anteriormente", id);
            return false;
        }

        // Actualizar la solicitud zoned date
        solicitud.setFechaRespuesta(java.time.ZonedDateTime.now());

        // Actualizar el estado de la solicitud
        solicitud.setEstado("RECHAZADO");
        solicitudReservaRepository.save(solicitud);

        logger.info("Solicitud de reserva con ID: {} rechazada exitosamente", id);
        return true;
    }
}

