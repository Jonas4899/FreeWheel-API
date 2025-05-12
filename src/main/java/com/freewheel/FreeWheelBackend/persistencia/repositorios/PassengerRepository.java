package com.freewheel.FreeWheelBackend.persistencia.repositorios;

import com.freewheel.FreeWheelBackend.persistencia.entidades.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository extends JpaRepository<PassengerEntity, Long> {
}
