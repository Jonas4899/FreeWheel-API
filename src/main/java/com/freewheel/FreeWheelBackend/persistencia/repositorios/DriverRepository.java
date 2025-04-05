package com.freewheel.FreeWheelBackend.persistencia.repositorios;

import com.freewheel.FreeWheelBackend.persistencia.entidades.DriverEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<DriverEntity, Long> {
}
