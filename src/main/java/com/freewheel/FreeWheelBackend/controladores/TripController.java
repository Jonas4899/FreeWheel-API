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

import java.util.List;
import java.util.Collections;

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

    // --- NUEVO ENDPOINT PARA BUSCAR VIAJES ---
    /**
     * Busca viajes disponibles según criterios, incluyendo proximidad geográfica.
     * Los criterios se pasan como parámetros de URL.
     * Ejemplo: /viajes/buscar?latitudOrigenBusqueda=4.71&longitudOrigenBusqueda=-74.07&latitudDestinoBusqueda=4.60&longitudDestinoBusqueda=-74.08&fecha=11/04/2025&radioBusquedaKm=5&numeroAsientosRequeridos=1
     *
     * @param criteria Objeto que mapea los parámetros de la query (?param1=val1&...)
     * @return ResponseEntity con la lista de TripDTO encontrados o un error.
     */
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

    // Método helper simple para crear un DTO de error (opcional)
    private TripDTO createErrorDTO(String errorMessage) {
        TripDTO errorDto = new TripDTO();
        // Puedes usar un campo existente como 'estado' o añadir uno específico para errores
        errorDto.setEstado("ERROR: " + errorMessage);
        return errorDto;
    }
}
