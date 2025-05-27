package com.freewheel.FreeWheelBackend.persistencia.repositorios;

import com.freewheel.FreeWheelBackend.persistencia.entidades.TripEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<TripEntity, Long>, JpaSpecificationExecutor<TripEntity> {
    List<TripEntity> findByConductorId(Long conductorId);
    List<TripEntity> findTop6ByEstadoOrderByFechaAscHoraInicioAsc(String estado);
}
