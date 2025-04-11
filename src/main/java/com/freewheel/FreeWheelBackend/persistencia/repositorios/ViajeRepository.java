package com.freewheel.FreeWheelBackend.persistencia.repositorios;

import com.freewheel.FreeWheelBackend.persistencia.entidades.ViajeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
// JpaSpecificationExecutor es necesario para usar Specifications (búsquedas dinámicas)
public interface ViajeRepository extends JpaRepository<ViajeEntity, Long>, JpaSpecificationExecutor<ViajeEntity> {
    // Puedes añadir métodos de consulta específicos aquí si los necesitas
    // ej: List<ViajeEntity> findByEstado(String estado);
}