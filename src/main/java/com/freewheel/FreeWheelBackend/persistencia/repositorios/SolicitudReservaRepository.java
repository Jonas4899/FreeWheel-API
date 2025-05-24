package com.freewheel.FreeWheelBackend.persistencia.repositorios;

import com.freewheel.FreeWheelBackend.persistencia.entidades.SolicitudReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitudReservaRepository extends JpaRepository<SolicitudReservaEntity, Long> {

    @Query("SELECT sr FROM SolicitudReservaEntity sr " +
           "JOIN sr.viaje v " +
           "WHERE v.conductorId = :conductorId " +
           "ORDER BY sr.fechaSolicitud DESC")
    List<SolicitudReservaEntity> findAllByConductorId(@Param("conductorId") Long conductorId);

    List<SolicitudReservaEntity> findAllByPasajero_Id(Long usuarioId);

    List<SolicitudReservaEntity> findAllByViaje_Id(Long viajeId);
}