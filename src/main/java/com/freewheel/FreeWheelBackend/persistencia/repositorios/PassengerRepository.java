package com.freewheel.FreeWheelBackend.persistencia.repositorios;

import com.freewheel.FreeWheelBackend.persistencia.entidades.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PassengerRepository extends JpaRepository<PassengerEntity, Long> {
    List<PassengerEntity> findByViajeIdAndEstado(long viajeId, String estado);
    boolean existsByUsuario_IdAndViajeId(long usuarioId, long viajeId);
    List<PassengerEntity> findByUsuario_Id(long usuarioId);
    List<PassengerEntity> findByUsuario_IdAndViajeId(long usuarioId, long viajeId);
}
