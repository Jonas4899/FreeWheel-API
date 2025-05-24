package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.CalificacionViajeDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.CrearCalificacionRequestDTO;

import java.util.List;

public interface CalificacionViajeService {

    /**
     * Permite a un pasajero crear una nueva calificación para un viaje finalizado.
     *
     * @param requestDTO Contiene el ID del viaje, la puntuación y el comentario.
     * @param pasajeroUsuarioId El ID del usuario (pasajero) autenticado que emite la calificación.
     * @return El DTO de la calificación creada.
     * @throws RuntimeException Si el viaje no existe, no está finalizado, el pasajero no participó,
     * o si el pasajero ya calificó este viaje.
     */
    CalificacionViajeDTO crearCalificacion(CrearCalificacionRequestDTO requestDTO, Long pasajeroUsuarioId);

    /**
     * Obtiene todas las calificaciones otorgadas a un conductor específico.
     *
     * @param conductorUsuarioId El ID del usuario (conductor).
     * @return Una lista de CalificacionViajeDTO.
     */
    List<CalificacionViajeDTO> obtenerCalificacionesPorConductor(Long conductorUsuarioId);

    /**
     * Calcula la calificación promedio para un conductor específico.
     *
     * @param conductorUsuarioId El ID del usuario (conductor).
     * @return El promedio de las calificaciones como Double, o null si no hay calificaciones.
     */
    Double obtenerPromedioCalificacionConductor(Long conductorUsuarioId);

    /**
     * Obtiene el número total de calificaciones recibidas por un conductor.
     *
     * @param conductorUsuarioId El ID del usuario (conductor).
     * @return El conteo de calificaciones.
     */
    Long obtenerNumeroDeCalificacionesConductor(Long conductorUsuarioId);

    /**
     * Verifica si un pasajero ya ha calificado un viaje específico.
     *
     * @param viajeId El ID del viaje.
     * @param pasajeroUsuarioId El ID del usuario (pasajero).
     * @return true si el pasajero ya calificó el viaje, false en caso contrario.
     */
    boolean haCalificadoPasajero(Long viajeId, Long pasajeroUsuarioId);

    /**
     * Obtiene una calificación específica por su ID.
     *
     * @param calificacionId El ID de la calificación.
     * @return El DTO de la calificación si se encuentra.
     * @throws RuntimeException Si la calificación no se encuentra.
     */
    CalificacionViajeDTO obtenerCalificacionPorId(Long calificacionId);

}
