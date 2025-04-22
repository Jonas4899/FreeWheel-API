package com.freewheel.FreeWheelBackend.persistencia.repositorios;

import com.freewheel.FreeWheelBackend.persistencia.entidades.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
    /**
     * Encuentra todos los vehículos asociados a un conductor específico
     * @param conductorId El ID del conductor
     * @return Lista de vehículos del conductor
     */
    List<VehicleEntity> findByConductorId(Long conductorId);
}
