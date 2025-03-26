package com.freewheel.FreeWheelBackend.persistencia.repositorios;

import com.freewheel.FreeWheelBackend.persistencia.entidades.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
    VehicleEntity findById(long id);
    VehicleEntity findByPlaca(String placa);
    Optional<VehicleEntity> findByConductorId(long conductor_id);
    VehicleEntity save(VehicleEntity vehicleEntity);
}
