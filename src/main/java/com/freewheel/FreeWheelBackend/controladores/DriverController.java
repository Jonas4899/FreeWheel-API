package com.freewheel.FreeWheelBackend.controladores;

import com.freewheel.FreeWheelBackend.persistencia.dtos.DriverDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import com.freewheel.FreeWheelBackend.servicios.DriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/conductores")
@CrossOrigin("*")
public class DriverController {

    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);
    private final DriverService driverService;

    @Autowired
    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<DriverDTO> createDriver(@RequestBody DriverDTO driverDTO) {
        try {
            return ResponseEntity.ok(driverService.createDriver(driverDTO));
        } catch (RuntimeException e) {
            logger.error("Error al crear conductor: {}", e.getMessage());
            if (e.getMessage().contains("ya está registrado como conductor")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping(value = "/registrar-con-licencia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DriverDTO> createDriverConLicencia(
            @RequestParam("usuarioId") long usuarioId,
            @RequestParam("licenciaFrontal") MultipartFile licenciaFrontal,
            @RequestParam("licenciaTrasera") MultipartFile licenciaTrasera
    ) {
        try {
            if (licenciaFrontal.isEmpty() || licenciaTrasera.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            if (!isImageFile(licenciaFrontal) || !isImageFile(licenciaTrasera)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(DriverDTO.builder().build());
            }

            DriverDTO driverDTO = DriverDTO.builder()
                    .usuarioId(usuarioId)
                    .build();

            // Llamar al servicio
            DriverDTO createdDriver = driverService.createDriverWithLicencia(
                    driverDTO, licenciaFrontal, licenciaTrasera);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdDriver);

        } catch (RuntimeException e) {
            logger.error("Error al crear conductor con licencia: {}", e.getMessage());
            if (e.getMessage().contains("ya está registrado como conductor")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error inesperado al crear conductor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserInfoByDriverId(@PathVariable Long id) {
        logger.info("Solicitud para obtener información del usuario para el conductor con ID: {}", id);
        try {
            UserDTO userDTO = driverService.getUserByDriverId(id);
            if (userDTO != null) {
                return ResponseEntity.ok(userDTO);
            } else {
                logger.warn("No se encontró información del usuario para el conductor con ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error al obtener información del usuario para el conductor con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}