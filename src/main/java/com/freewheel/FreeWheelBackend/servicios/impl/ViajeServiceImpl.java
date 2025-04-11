package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.ViajeDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.ViajeSearchCriteriaDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.ViajeEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.ViajeRepository;
import com.freewheel.FreeWheelBackend.servicios.ViajeService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // Añadir logger
import org.slf4j.LoggerFactory; // Añadir logger
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // Para comprobar strings vacíos

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViajeServiceImpl implements ViajeService {

    private static final Logger logger = LoggerFactory.getLogger(ViajeServiceImpl.class); // Añadir logger
    private final ViajeRepository viajeRepository;

    @Override
    public List<ViajeDTO> buscarViajesDisponibles(ViajeSearchCriteriaDTO criteria) {
        logger.debug("Creando especificación para búsqueda de viajes con criterios: {}", criteria);

        Specification<ViajeEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Siempre buscar solo viajes 'por iniciar' (o el estado que uses para 'disponible')
            predicates.add(criteriaBuilder.equal(root.get("estado"), "por iniciar"));
            logger.trace("Añadido predicado: estado = 'por iniciar'");

            if (criteria.getLugarInicio() != null && StringUtils.hasText(criteria.getLugarInicio())) {
                // TODO: Integrar validación/búsqueda por proximidad con servicio de mapas (ej. Geoapify, Google Maps)
                // Búsqueda simple por ahora (case-insensitive)
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lugarInicio")), "%" + criteria.getLugarInicio().toLowerCase() + "%"));
                logger.trace("Añadido predicado: lugarInicio LIKE '%{}%'", criteria.getLugarInicio().toLowerCase());
            }

            if (criteria.getLugarDestino() != null && StringUtils.hasText(criteria.getLugarDestino())) {
                // TODO: Integrar validación/búsqueda por proximidad
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lugarDestino")), "%" + criteria.getLugarDestino().toLowerCase() + "%"));
                logger.trace("Añadido predicado: lugarDestino LIKE '%{}%'", criteria.getLugarDestino().toLowerCase());
            }

            if (criteria.getFecha() != null) {
                predicates.add(criteriaBuilder.equal(root.get("fecha"), criteria.getFecha()));
                logger.trace("Añadido predicado: fecha = {}", criteria.getFecha());
            }

            // Lógica para rango de hora de inicio
            if (criteria.getHoraInicioDesde() != null && criteria.getHoraInicioHasta() != null) {
                // Asegurarse que desde <= hasta? Podría hacerse en validación DTO
                predicates.add(criteriaBuilder.between(root.get("horaInicio"), criteria.getHoraInicioDesde(), criteria.getHoraInicioHasta()));
                logger.trace("Añadido predicado: horaInicio BETWEEN {} AND {}", criteria.getHoraInicioDesde(), criteria.getHoraInicioHasta());
            } else if (criteria.getHoraInicioDesde() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("horaInicio"), criteria.getHoraInicioDesde()));
                logger.trace("Añadido predicado: horaInicio >= {}", criteria.getHoraInicioDesde());
            } else if (criteria.getHoraInicioHasta() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("horaInicio"), criteria.getHoraInicioHasta()));
                logger.trace("Añadido predicado: horaInicio <= {}", criteria.getHoraInicioHasta());
            }


            if (criteria.getNumeroAsientosRequeridos() != null && criteria.getNumeroAsientosRequeridos() > 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("numeroAsientosDisponibles"), criteria.getNumeroAsientosRequeridos()));
                logger.trace("Añadido predicado: numeroAsientosDisponibles >= {}", criteria.getNumeroAsientosRequeridos());
            }

            // Combinar todos los predicados con AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        logger.debug("Ejecutando búsqueda en repositorio con la especificación creada.");
        List<ViajeEntity> viajesEncontrados = viajeRepository.findAll(spec);
        logger.info("Búsqueda completada. Encontrados {} viajes.", viajesEncontrados.size());

        // Mapear entidades a DTOs
        return viajesEncontrados.stream()
                .map(this::mapToViajeDTO)
                .collect(Collectors.toList());
    }

    // Mapeo de Entidad a DTO (considera usar MapStruct para esto)
    private ViajeDTO mapToViajeDTO(ViajeEntity entity) {
        return new ViajeDTO(
                entity.getId(),
                entity.getConductorId(), // Asumiendo que tienes este campo
                entity.getLugarInicio(),
                entity.getLugarDestino(),
                entity.getFecha(),
                entity.getHoraInicio(),
                entity.getHoraFin(),
                entity.getPrecioAsiento(),
                entity.getNumeroAsientosDisponibles(),
                entity.getEstado()
        );
    }
}