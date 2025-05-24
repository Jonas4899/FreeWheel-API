package com.freewheel.FreeWheelBackend.controladores;

import com.freewheel.FreeWheelBackend.persistencia.dtos.AuthResponseDTO; // Necesario para obtener UserDTO
import com.freewheel.FreeWheelBackend.persistencia.dtos.CalificacionViajeDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.CrearCalificacionRequestDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import com.freewheel.FreeWheelBackend.seguridad.JwtUtils; // Para extraer info del token si es necesario, aunque es mejor por Principal
import com.freewheel.FreeWheelBackend.servicios.CalificacionViajeService;
import io.jsonwebtoken.Claims; // Para extraer claims del token
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/calificaciones") // Ruta base para las calificaciones
public class CalificacionViajeController {

    private static final Logger logger = LoggerFactory.getLogger(CalificacionViajeController.class);

    private final CalificacionViajeService calificacionViajeService;
    private final JwtUtils jwtUtils;


    @Autowired
    public CalificacionViajeController(CalificacionViajeService calificacionViajeService, JwtUtils jwtUtils) {
        this.calificacionViajeService = calificacionViajeService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Endpoint para crear una nueva calificación.
     * El pasajero debe estar autenticado.
     */
    @PostMapping
    public ResponseEntity<?> crearCalificacion(
            @RequestBody CrearCalificacionRequestDTO requestDTO,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extraer el ID del usuario del token JWT.
            // Es más robusto y seguro obtener el UserDetails/Principal de Spring Security.
            String token = authorizationHeader.substring(7); // Quita "Bearer "
            String correoDesdeToken = jwtUtils.extractEmail(token); //
            Long pasajeroUsuarioId = jwtUtils.extractClaim(token, claims -> claims.get("id", Long.class));


            if (pasajeroUsuarioId == null) {
                logger.warn("No se pudo extraer el ID del usuario del token para el correo: {}", correoDesdeToken);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "No se pudo identificar al usuario desde el token."));
            }

            logger.info("Solicitud para crear calificación recibida para viajeId: {} por pasajeroId: {}", requestDTO.getViajeId(), pasajeroUsuarioId);

            CalificacionViajeDTO calificacionCreada = calificacionViajeService.crearCalificacion(requestDTO, pasajeroUsuarioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(calificacionCreada);

        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error de validación al crear calificación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (RuntimeException e) {
            // Captura otras excepciones de runtime como "Viaje no encontrado", "Usuario no encontrado"
            logger.error("Error de runtime al crear calificación: ", e);
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Ocurrió un error al procesar la solicitud: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado al crear calificación: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Ocurrió un error inesperado al crear la calificación."));
        }
    }

    /**
     * Endpoint para obtener todas las calificaciones de un conductor específico.
     */
    @GetMapping("/conductor/{conductorUsuarioId}")
    public ResponseEntity<?> obtenerCalificacionesPorConductor(@PathVariable Long conductorUsuarioId) {
        try {
            logger.info("Solicitud para obtener calificaciones del conductor ID: {}", conductorUsuarioId);
            List<CalificacionViajeDTO> calificaciones = calificacionViajeService.obtenerCalificacionesPorConductor(conductorUsuarioId);
            if (calificaciones.isEmpty()) {
                return ResponseEntity.ok(Collections.singletonMap("mensaje", "El conductor aún no tiene calificaciones."));
            }
            return ResponseEntity.ok(calificaciones);
        } catch (RuntimeException e) {
            logger.warn("Error al obtener calificaciones del conductor {}: {}", conductorUsuarioId, e.getMessage());
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error al obtener calificaciones del conductor."));
        }
    }

    /**
     * Endpoint para obtener el promedio de calificación y el número de calificaciones de un conductor.
     */
    @GetMapping("/conductor/{conductorUsuarioId}/resumen")
    public ResponseEntity<?> obtenerResumenCalificacionesConductor(@PathVariable Long conductorUsuarioId) {
        try {
            logger.info("Solicitud para obtener resumen de calificaciones del conductor ID: {}", conductorUsuarioId);
            Double promedio = calificacionViajeService.obtenerPromedioCalificacionConductor(conductorUsuarioId);
            Long numeroCalificaciones = calificacionViajeService.obtenerNumeroDeCalificacionesConductor(conductorUsuarioId);

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("conductorUsuarioId", conductorUsuarioId);
            resumen.put("calificacionPromedio", promedio == null ? 0.0 : promedio); // Devuelve 0.0 si no hay calificaciones
            resumen.put("numeroDeCalificaciones", numeroCalificaciones);

            return ResponseEntity.ok(resumen);
        } catch (RuntimeException e) {
            logger.warn("Error al obtener resumen de calificaciones del conductor {}: {}", conductorUsuarioId, e.getMessage());
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error al obtener resumen de calificaciones del conductor."));
        }
    }


    /**
     * Endpoint para verificar si el pasajero autenticado ya calificó un viaje específico.
     */
    @GetMapping("/viaje/{viajeId}/ha-calificado")
    public ResponseEntity<?> haCalificadoPasajero(
            @PathVariable Long viajeId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.substring(7);
            Long pasajeroUsuarioId = jwtUtils.extractClaim(token, claims -> claims.get("id", Long.class));

            if (pasajeroUsuarioId == null) {
                logger.warn("No se pudo extraer el ID del usuario del token.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "No se pudo identificar al usuario desde el token."));
            }

            logger.info("Verificando si pasajero ID: {} ha calificado viaje ID: {}", pasajeroUsuarioId, viajeId);
            boolean haCalificado = calificacionViajeService.haCalificadoPasajero(viajeId, pasajeroUsuarioId);
            return ResponseEntity.ok(Collections.singletonMap("haCalificado", haCalificado));

        } catch (Exception e) {
            logger.error("Error al verificar si el pasajero ha calificado el viaje {}: ", viajeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error al verificar el estado de la calificación."));
        }
    }

    /**
     * Endpoint para obtener una calificación específica por su ID.
     */
    @GetMapping("/{calificacionId}")
    public ResponseEntity<?> obtenerCalificacionPorId(@PathVariable Long calificacionId) {
        try {
            logger.info("Solicitud para obtener calificación por ID: {}", calificacionId);
            CalificacionViajeDTO calificacion = calificacionViajeService.obtenerCalificacionPorId(calificacionId);
            return ResponseEntity.ok(calificacion);
        } catch (RuntimeException e) {
            logger.warn("Error al obtener calificación por ID {}: {}", calificacionId, e.getMessage());
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Error al obtener la calificación."));
        }
    }
}
