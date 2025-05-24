package com.freewheel.FreeWheelBackend.persistencia.repositorios;

import com.freewheel.FreeWheelBackend.persistencia.entidades.CalificacionViajeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CalificacionViajeRepository extends JpaRepository<CalificacionViajeEntity, Long> {

    /**
     * Busca una calificación específica dada por un pasajero para un viaje.
     * Útil para verificar si un pasajero ya calificó un viaje.
     *
     * @param viajeId El ID del viaje.
     * @param pasajeroUsuarioId El ID del usuario (pasajero).
     * @return Un Optional conteniendo la CalificacionViajeEntity si existe, o vacío si no.
     */
    Optional<CalificacionViajeEntity> findByViaje_IdAndPasajeroUsuario_Id(Long viajeId, Long pasajeroUsuarioId);

    /**
     * Encuentra todas las calificaciones otorgadas a un conductor específico.
     *
     * @param conductorEvaluadoUsuarioId El ID del usuario (conductor) que fue evaluado.
     * @return Una lista de CalificacionViajeEntity.
     */
    List<CalificacionViajeEntity> findByConductorEvaluadoUsuario_Id(Long conductorEvaluadoUsuarioId);

    /**
     * Calcula el promedio de las puntuaciones para un conductor específico.
     * Ignora comentarios nulos o vacíos si solo se quieren considerar calificaciones con comentarios,
     * pero en este caso, promedia todas las puntuaciones.
     *
     * @param conductorEvaluadoUsuarioId El ID del usuario (conductor) que fue evaluado.
     * @return El promedio de las calificaciones como Double, o null si no hay calificaciones.
     */
    @Query("SELECT AVG(c.puntuacion) FROM CalificacionViajeEntity c WHERE c.conductorEvaluadoUsuario.id = :conductorEvaluadoUsuarioId")
    Double findAveragePuntuacionByConductorEvaluadoUsuarioId(@Param("conductorEvaluadoUsuarioId") Long conductorEvaluadoUsuarioId);

    /**
     * Cuenta el número total de calificaciones recibidas por un conductor específico.
     *
     * @param conductorEvaluadoUsuarioId El ID del usuario (conductor) que fue evaluado.
     * @return El número total de calificaciones.
     */
    @Query("SELECT COUNT(c) FROM CalificacionViajeEntity c WHERE c.conductorEvaluadoUsuario.id = :conductorEvaluadoUsuarioId")
    Long countByConductorEvaluadoUsuarioId(@Param("conductorEvaluadoUsuarioId") Long conductorEvaluadoUsuarioId);
}
