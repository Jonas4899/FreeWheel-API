package com.freewheel.FreeWheelBackend.controladores;

import com.freewheel.FreeWheelBackend.persistencia.dtos.SolicitudReservaDTO;
import com.freewheel.FreeWheelBackend.servicios.SolicitudReservaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/solicitudes-reserva")
public class SolicitudReservaController {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudReservaController.class);
    private final SolicitudReservaService solicitudReservaService;

    @Autowired
    public SolicitudReservaController(SolicitudReservaService solicitudReservaService) {
        this.solicitudReservaService = solicitudReservaService;
    }

    /**
     * Obtiene el historial de solicitudes de reserva para un conductor específico
     * @param conductorId ID del conductor
     * @return Lista de solicitudes de reserva asociadas a los viajes del conductor
     */
    @GetMapping("/conductor/{conductorId}")
    public ResponseEntity<List<SolicitudReservaDTO>> obtenerHistorialSolicitudesConductor(@PathVariable Long conductorId) {
        logger.info("Recibida solicitud para obtener historial de solicitudes del conductor con ID: {}", conductorId);
        
        try {
            if (conductorId == null || conductorId <= 0) {
                logger.warn("ID de conductor inválido: {}", conductorId);
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }
            
            List<SolicitudReservaDTO> solicitudes = solicitudReservaService.obtenerHistorialSolicitudesConductor(conductorId);
            logger.info("Solicitud procesada con éxito. Se encontraron {} solicitudes para el conductor {}", solicitudes.size(), conductorId);
            return ResponseEntity.ok(solicitudes);
            
        } catch (Exception e) {
            logger.error("Error al obtener historial de solicitudes para el conductor {}: {}", conductorId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    /**
     * Obtiene el historial de solicitudes de reserva para un conductor a partir del userId
     * @param userId ID del usuario (conductor)
     * @return Lista de solicitudes de reserva asociadas a los viajes del conductor
     */
    @GetMapping("/conductor/usuario/{userId}")
    public ResponseEntity<List<SolicitudReservaDTO>> obtenerHistorialSolicitudesConductorPorUsuario(@PathVariable Long userId) {
        logger.info("Recibida solicitud para obtener historial de solicitudes del conductor (userId) con ID: {}", userId);
        try {
            if (userId == null || userId <= 0) {
                logger.warn("ID de usuario inválido: {}", userId);
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }
            List<SolicitudReservaDTO> solicitudes = solicitudReservaService.obtenerHistorialSolicitudesConductorPorUsuario(userId);
            logger.info("Solicitud procesada con éxito. Se encontraron {} solicitudes para el usuario/conductor {}", solicitudes.size(), userId);
            return ResponseEntity.ok(solicitudes);
        } catch (Exception e) {
            logger.error("Error al obtener historial de solicitudes para el usuario/conductor {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PutMapping("/aceptar-solicitud/{id}")
    public ResponseEntity<String> aceptarSolicitudReserva(@PathVariable Long id) {
        logger.info("Recibida la solicitud con ID: {}", id);

        try {
            if (id == null || id <= 0) {
                logger.warn("ID de solicitud inválido: {}", id);
                return ResponseEntity.badRequest().body("ID de solicitud inválido");
            }

            boolean aceptada = solicitudReservaService.aceptarSolicitudReserva(id);
            if (aceptada) {
                logger.info("Solicitud con ID {} aceptada correctamente", id);
                return ResponseEntity.ok("Solicitud aceptada");
            } else {
                logger.warn("No se encontró la solicitud con ID {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solicitud no encontrada");
            }
        } catch (Exception e) {
            logger.error("Error al aceptar la solicitud con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al aceptar la solicitud");
        }
    }
}

