package com.freewheel.FreeWheelBackend.persistencia.repositorios;

import com.freewheel.FreeWheelBackend.persistencia.entidades.SolicitudReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitudReservaRepository extends JpaRepository<SolicitudReservaEntity, Long> {
    
    /**
     * Encuentra todas las solicitudes de reserva asociadas a los viajes de un conductor específico
     * @param conductorId el ID del conductor
     * @return lista de solicitudes de reserva
     */
    @Query("SELECT sr FROM SolicitudReservaEntity sr " +
           "JOIN sr.viaje v " +
           "WHERE v.conductorId = :conductorId " +
           "ORDER BY sr.fechaSolicitud DESC")
    List<SolicitudReservaEntity> findAllByConductorId(@Param("conductorId") Long conductorId);

    List<SolicitudReservaEntity> findAllByPasajero_Id(Long usuarioId);
}