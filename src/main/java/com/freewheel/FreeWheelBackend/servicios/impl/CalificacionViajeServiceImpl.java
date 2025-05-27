package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.CalificacionViajeDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.CrearCalificacionRequestDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.CalificacionViajeEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.DriverEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.SolicitudReservaEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.TripEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.UserEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.CalificacionViajeRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.DriverRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.SolicitudReservaRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.TripRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.UserRepository;
import com.freewheel.FreeWheelBackend.servicios.CalificacionViajeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalificacionViajeServiceImpl implements CalificacionViajeService {

    private static final Logger logger = LoggerFactory.getLogger(CalificacionViajeServiceImpl.class);

    private final CalificacionViajeRepository calificacionViajeRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final SolicitudReservaRepository solicitudReservaRepository;

    @Autowired
    public CalificacionViajeServiceImpl(
            CalificacionViajeRepository calificacionViajeRepository,
            TripRepository tripRepository,
            UserRepository userRepository,
            DriverRepository driverRepository,
            SolicitudReservaRepository solicitudReservaRepository) {
        this.calificacionViajeRepository = calificacionViajeRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.solicitudReservaRepository = solicitudReservaRepository;
    }

    @Override
    @Transactional
    public CalificacionViajeDTO crearCalificacion(CrearCalificacionRequestDTO requestDTO, Long pasajeroUsuarioIdActual) {
        logger.info("Intentando crear calificación para viaje ID: {} por pasajero ID: {}", requestDTO.getViajeId(), pasajeroUsuarioIdActual);

        // 1. Validar que el pasajero (usuario actual) exista
        UserEntity pasajero = userRepository.findById(pasajeroUsuarioIdActual)
                .orElseThrow(() -> {
                    logger.warn("Intento de calificar por usuario no existente ID: {}", pasajeroUsuarioIdActual);
                    return new RuntimeException("Usuario pasajero no encontrado con ID: " + pasajeroUsuarioIdActual);
                });

        // 2. Validar que el viaje exista
        TripEntity viaje = tripRepository.findById(requestDTO.getViajeId())
                .orElseThrow(() -> {
                    logger.warn("Intento de calificar viaje no existente ID: {}", requestDTO.getViajeId());
                    return new RuntimeException("Viaje no encontrado con ID: " + requestDTO.getViajeId());
                });

        // 3. Validar que el viaje esté finalizado
        if (!"finalizado".equalsIgnoreCase(viaje.getEstado())) {
            logger.warn("Intento de calificar viaje ID: {} que no está finalizado. Estado actual: {}", viaje.getId(), viaje.getEstado());
            throw new IllegalStateException("Solo se pueden calificar viajes que estén en estado 'finalizado'. Estado actual: " + viaje.getEstado());
        }

        // 4. Validar que el pasajero haya participado en el viaje (solicitud aceptada)
        //    (Asumimos que 'SolicitudReservaEntity' es la fuente de verdad para los pasajeros de un viaje)
        //    Alternativamente, si tienes una entidad 'PassengerEntity' podrías usar esa.
        List<SolicitudReservaEntity> solicitudesDelViaje = solicitudReservaRepository.findAllByViaje_Id(viaje.getId()); //
        boolean pasajeroParticipo = solicitudesDelViaje.stream()
                .anyMatch(solicitud -> "ACEPTADO".equalsIgnoreCase(solicitud.getEstado()) && //
                        solicitud.getPasajero().getId().equals(pasajeroUsuarioIdActual));

        if (!pasajeroParticipo) {
            logger.warn("Usuario ID: {} intentó calificar viaje ID: {} pero no participó o su solicitud no fue aceptada.", pasajeroUsuarioIdActual, viaje.getId());
            throw new IllegalStateException("El usuario no participó en este viaje o su solicitud no fue aceptada.");
        }


        // 5. Validar que el pasajero no haya calificado este viaje antes
        if (haCalificadoPasajero(viaje.getId(), pasajeroUsuarioIdActual)) {
            logger.warn("Usuario ID: {} intentó calificar viaje ID: {} nuevamente.", pasajeroUsuarioIdActual, viaje.getId());
            throw new IllegalStateException("Ya has calificado este viaje anteriormente.");
        }

        // 6. Obtener el conductor del viaje para asociarlo a la calificación
        // El TripEntity tiene conductorId, que es el ID de DriverEntity
        DriverEntity driverEntity = driverRepository.findById(viaje.getConductorId())
                .orElseThrow(() -> {
                    // Esto sería raro si el viaje existe, pero es una buena práctica validarlo
                    logger.error("No se encontró el DriverEntity para el conductorId {} del viajeId {}", viaje.getConductorId(), viaje.getId());
                    return new RuntimeException("Conductor asociado al viaje no encontrado.");
                });
        UserEntity conductorEvaluado = driverEntity.getUsuario(); //
        if (conductorEvaluado == null) {
            logger.error("El DriverEntity ID {} no tiene un UserEntity asociado.", driverEntity.getId());
            throw new RuntimeException("Información de usuario del conductor no encontrada.");
        }


        // 7. Crear y guardar la entidad de calificación
        CalificacionViajeEntity nuevaCalificacion = CalificacionViajeEntity.builder()
                .viaje(viaje)
                .pasajeroUsuario(pasajero)
                .conductorEvaluadoUsuario(conductorEvaluado)
                .puntuacion(requestDTO.getPuntuacion())
                .comentario(requestDTO.getComentario())
                // fechaCalificacion se setea automáticamente por @CreationTimestamp
                .build();

        CalificacionViajeEntity calificacionGuardada = calificacionViajeRepository.save(nuevaCalificacion);
        logger.info("Calificación guardada con ID: {} para viaje ID: {} por pasajero ID: {}", calificacionGuardada.getId(), viaje.getId(), pasajeroUsuarioIdActual);

        return mapToCalificacionViajeDTO(calificacionGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalificacionViajeDTO> obtenerCalificacionesPorConductor(Long conductorUsuarioId) {
        logger.debug("Obteniendo calificaciones para conductor ID: {}", conductorUsuarioId);
        // Validar que el conductor (usuario) exista
        userRepository.findById(conductorUsuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario conductor no encontrado con ID: " + conductorUsuarioId));

        List<CalificacionViajeEntity> calificaciones = calificacionViajeRepository.findByConductorEvaluadoUsuario_Id(conductorUsuarioId);
        return calificaciones.stream()
                .map(this::mapToCalificacionViajeDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double obtenerPromedioCalificacionConductor(Long conductorUsuarioId) {
        logger.debug("Calculando promedio de calificación para conductor ID: {}", conductorUsuarioId);
        // Validar que el conductor (usuario) exista
        userRepository.findById(conductorUsuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario conductor no encontrado con ID: " + conductorUsuarioId));
        return calificacionViajeRepository.findAveragePuntuacionByConductorEvaluadoUsuarioId(conductorUsuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long obtenerNumeroDeCalificacionesConductor(Long conductorUsuarioId) {
        logger.debug("Contando calificaciones para conductor ID: {}", conductorUsuarioId);
        userRepository.findById(conductorUsuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario conductor no encontrado con ID: " + conductorUsuarioId));
        return calificacionViajeRepository.countByConductorEvaluadoUsuarioId(conductorUsuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean haCalificadoPasajero(Long viajeId, Long pasajeroUsuarioId) {
        logger.debug("Verificando si pasajero ID: {} ha calificado viaje ID: {}", pasajeroUsuarioId, viajeId);
        return calificacionViajeRepository.findByViaje_IdAndPasajeroUsuario_Id(viajeId, pasajeroUsuarioId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public CalificacionViajeDTO obtenerCalificacionPorId(Long calificacionId) {
        logger.debug("Obteniendo calificación por ID: {}", calificacionId);
        CalificacionViajeEntity calificacion = calificacionViajeRepository.findById(calificacionId)
                .orElseThrow(() -> {
                    logger.warn("Calificación no encontrada con ID: {}", calificacionId);
                    return new RuntimeException("Calificación no encontrada con ID: " + calificacionId);
                });
        return mapToCalificacionViajeDTO(calificacion);
    }

    // --- Método helper para mapear Entidad a DTO ---
    private CalificacionViajeDTO mapToCalificacionViajeDTO(CalificacionViajeEntity entity) {
        if (entity == null) {
            return null;
        }
        String nombreCompletoPasajero = "";
        if (entity.getPasajeroUsuario() != null) {
            nombreCompletoPasajero = entity.getPasajeroUsuario().getNombre() + " " + entity.getPasajeroUsuario().getApellido(); //
        }

        return CalificacionViajeDTO.builder()
                .id(entity.getId())
                .viajeId(entity.getViaje() != null ? entity.getViaje().getId() : null)
                .pasajeroUsuarioId(entity.getPasajeroUsuario() != null ? entity.getPasajeroUsuario().getId() : null)
                .pasajeroNombreCompleto(nombreCompletoPasajero)
                .conductorEvaluadoUsuarioId(entity.getConductorEvaluadoUsuario() != null ? entity.getConductorEvaluadoUsuario().getId() : null)
                .puntuacion(entity.getPuntuacion())
                .comentario(entity.getComentario())
                .fechaCalificacion(entity.getFechaCalificacion())
                .build();
    }
}
