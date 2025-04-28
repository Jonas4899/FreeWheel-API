// Añadir al DriverRepository
package com.freewheel.FreeWheelBackend.persistencia.repositorios;

import com.freewheel.FreeWheelBackend.persistencia.entidades.DriverEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<DriverEntity, Long> {
    Optional<DriverEntity> findByUsuario_Id(Long usuarioId);
    boolean existsByUsuario_Id(Long usuarioId);
}