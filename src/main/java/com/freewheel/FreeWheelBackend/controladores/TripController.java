package com.freewheel.FreeWheelBackend.controladores;

import com.freewheel.FreeWheelBackend.persistencia.dtos.TripDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.TripSearchCriteriaDTO; // <--- Importar Criterio DTO
import com.freewheel.FreeWheelBackend.servicios.TripService;
import org.slf4j.Logger; // <--- Importar Logger
import org.slf4j.LoggerFactory; // <--- Importar LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/viajes")
public class TripController {
    private static final Logger logger = LoggerFactory.getLogger(TripController.class);
    private final TripService tripService;

    @Autowired
    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping("/crear")
    public ResponseEntity<TripDTO> createTrip(@RequestBody TripDTO tripDTO) {
        try {
            TripDTO newTrip = tripService.createTrip(tripDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(newTrip);
        } catch (Exception e) {
            System.out.println("Error al crear viaje: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/buscar")
    public ResponseEntity<List<TripDTO>> buscarViajesDisponibles(@RequestBody TripSearchCriteriaDTO criteria) {
        logger.info("Recibida solicitud de búsqueda de viajes con criterios: {}", criteria);
        try {
            // Validaciones básicas de criterios si son necesarias aquí
            if ((criteria.getLatitudOrigenBusqueda() != null && criteria.getLongitudOrigenBusqueda() == null) ||
                    (criteria.getLatitudOrigenBusqueda() == null && criteria.getLongitudOrigenBusqueda() != null)) {
                logger.warn("Búsqueda de origen incompleta: Se requiere latitud y longitud.");
                return ResponseEntity.badRequest().body(Collections.singletonList(createErrorDTO("Latitud y longitud de origen son requeridas juntas."))); // Ejemplo de error
            }
            if ((criteria.getLatitudDestinoBusqueda() != null && criteria.getLongitudDestinoBusqueda() == null) ||
                    (criteria.getLatitudDestinoBusqueda() == null && criteria.getLongitudDestinoBusqueda() != null)) {
                logger.warn("Búsqueda de destino incompleta: Se requiere latitud y longitud.");
                return ResponseEntity.badRequest().body(Collections.singletonList(createErrorDTO("Latitud y longitud de destino son requeridas juntas."))); // Ejemplo de error
            }

            List<TripDTO> viajes = tripService.buscarViajesDisponibles(criteria);
            logger.info("Búsqueda exitosa. {} viajes encontrados.", viajes.size());
            return ResponseEntity.ok(viajes);

        } catch (IllegalArgumentException e) { // Capturar errores específicos si el servicio los lanza
            logger.error("Argumento inválido en la búsqueda de viajes: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonList(createErrorDTO(e.getMessage())));
        } catch (Exception e) {
            logger.error("Error inesperado durante la búsqueda de viajes: ", e);
            // Evita devolver la traza completa del error al cliente por seguridad
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonList(createErrorDTO("Error interno al procesar la búsqueda.")));
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<List<TripDTO>> listarViajes(@RequestParam("userId") Long userId, @RequestParam(value = "esConductor", defaultValue = "false") boolean esConductor ) {
        logger.info("Recibida solicitud para listar viajes del usuario: {}, esConductor: {}", userId, esConductor);
        try {
            List<TripDTO> viajes = tripService.obtenerViajesPorUsuario(userId, esConductor);
            return ResponseEntity.ok(viajes);
        } catch (RuntimeException e) {
            logger.error("Error al listar viajes para el usuario {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        } catch (Exception e) {
            logger.error("Error inesperado al listar viajes para el usuario {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PatchMapping("/{tripId}/iniciar")
    public ResponseEntity<TripDTO> iniciarViaje(@PathVariable Long tripId) {
        logger.info("Recibida solicitud para iniciar el viaje con ID: {}", tripId);
        try {
            TripDTO viajeIniciado = tripService.iniciarViaje(tripId);
            return ResponseEntity.ok(viajeIniciado);
        } catch (IllegalStateException e) {
            // Error de estado inválido (ej: viaje ya iniciado)
            logger.warn("No se pudo iniciar el viaje {}: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(createErrorDTO(e.getMessage()));
        } catch (RuntimeException e) {
            // Viaje no encontrado u otro error
            logger.error("Error al iniciar el viaje {}: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorDTO(e.getMessage()));
        } catch (Exception e) {
            // Error inesperado
            logger.error("Error inesperado al iniciar el viaje {}: ", tripId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorDTO("Error interno al iniciar el viaje."));
        }
    }

    @PatchMapping("/{tripId}/finalizar")
    public ResponseEntity<TripDTO> finalizarViaje(@PathVariable Long tripId) {
        logger.info("Recibida solicitud para finalizar el viaje con ID: {}", tripId);
        try {
            TripDTO viajeFinalizado = tripService.finalizarViaje(tripId);
            return ResponseEntity.ok(viajeFinalizado);
        } catch (IllegalStateException e) {
            // Error de estado inválido (ej: viaje ya finalizado)
            logger.warn("No se pudo finalizar el viaje {}: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(createErrorDTO(e.getMessage()));
        } catch (RuntimeException e) {
            // Viaje no encontrado u otro error
            logger.error("Error al finalizar el viaje {}: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorDTO(e.getMessage()));
        } catch (Exception e) {
            // Error inesperado
            logger.error("Error inesperado al finalizar el viaje {}: ", tripId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorDTO("Error interno al finalizar el viaje."));
        }
    }

    private TripDTO createErrorDTO(String errorMessage) {
        TripDTO errorDto = new TripDTO();
        // Puedes usar un campo existente como 'estado' o añadir uno específico para errores
        errorDto.setEstado("ERROR: " + errorMessage);
        return errorDto;
    }

    @GetMapping("/conductor/{conductorId}")
    public ResponseEntity<?> listarViajesPorConductorId(@PathVariable("conductorId") Long conductorId) {
        logger.info("Recibida solicitud para listar viajes del conductor con ID: {}", conductorId);
        try {
            List<TripDTO> viajes = tripService.obtenerViajesPorConductorId(conductorId);

            if (viajes.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("mensaje", "NO tienes viajes creados");
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(viajes);
        } catch (Exception e) {
            logger.error("Error al listar viajes para el conductor con ID {}: {}", conductorId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
}
