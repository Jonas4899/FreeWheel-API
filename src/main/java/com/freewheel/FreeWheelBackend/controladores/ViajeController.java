package com.freewheel.FreeWheelBackend.controladores;

import com.freewheel.FreeWheelBackend.persistencia.dtos.ViajeDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.ViajeSearchCriteriaDTO;
import com.freewheel.FreeWheelBackend.servicios.ViajeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // Añadir logger
import org.slf4j.LoggerFactory; // Añadir logger
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/viajes")
@RequiredArgsConstructor
public class ViajeController {

    private static final Logger logger = LoggerFactory.getLogger(ViajeController.class); // Añadir logger
    private final ViajeService viajeService;

    /**
     * Endpoint para buscar viajes disponibles según criterios.
     * Los criterios se pasan como parámetros de la URL.
     * Ejemplo: /viajes/buscar?lugarInicio=Bogota&fecha=15/05/2024&numeroAsientosRequeridos=2
     *
     * @param criteria Objeto DTO que mapea los parámetros de la query.
     * @return ResponseEntity con la lista de ViajeDTO encontrados o una lista vacía.
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ViajeDTO>> buscarViajesDisponibles(@ModelAttribute ViajeSearchCriteriaDTO criteria) {
        logger.info("Buscando viajes con criterios: {}", criteria); // Log de criterios
        try {
            List<ViajeDTO> viajes = viajeService.buscarViajesDisponibles(criteria);
            logger.info("Encontrados {} viajes.", viajes.size()); // Log de resultado
            return ResponseEntity.ok(viajes);
        } catch (Exception e) {
            logger.error("Error al buscar viajes: ", e); // Log de error
            // Devuelve un error 500 o maneja la excepción como prefieras
            return ResponseEntity.internalServerError().build();
        }
    }
}