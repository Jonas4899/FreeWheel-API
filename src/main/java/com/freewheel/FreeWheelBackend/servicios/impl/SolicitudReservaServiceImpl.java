package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.SolicitudReservaDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.TripDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.SolicitudReservaEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.TripEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.UserEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.SolicitudReservaRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.TripRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.UserRepository;
import com.freewheel.FreeWheelBackend.servicios.SolicitudReservaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolicitudReservaServiceImpl implements SolicitudReservaService {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudReservaServiceImpl.class);
    
    private final SolicitudReservaRepository solicitudReservaRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public SolicitudReservaServiceImpl(SolicitudReservaRepository solicitudReservaRepository, 
                                      TripRepository tripRepository,
                                      UserRepository userRepository) {
        this.solicitudReservaRepository = solicitudReservaRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudReservaDTO> obtenerHistorialSolicitudesConductor(Long conductorId) {
        logger.debug("Obteniendo historial de solicitudes para el conductor con ID: {}", conductorId);
        
        List<SolicitudReservaEntity> solicitudes = solicitudReservaRepository.findAllByConductorId(conductorId);
        logger.info("Se encontraron {} solicitudes para el conductor con ID: {}", solicitudes.size(), conductorId);
        
        return solicitudes.stream()
                .map(this::mapToSolicitudReservaDTO)
                .collect(Collectors.toList());
    }
    
    private SolicitudReservaDTO mapToSolicitudReservaDTO(SolicitudReservaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Obtener datos del viaje
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
                    // Agregar otras propiedades necesarias del pasajero
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
}