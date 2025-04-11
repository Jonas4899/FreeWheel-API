package com.freewheel.FreeWheelBackend.persistencia.repositorios;

import com.freewheel.FreeWheelBackend.persistencia.entidades.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {
    Optional<OrganizationEntity> findByNit(String nit);
    // Busca por el campo 'codigo' de la entidad OrganizationEntity
    Optional<OrganizationEntity> findByCodigo(String codigo);
}