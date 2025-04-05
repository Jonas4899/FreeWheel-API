package com.freewheel.FreeWheelBackend.controladores;

import com.freewheel.FreeWheelBackend.persistencia.dtos.DriverDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.VehicleDTO;
import com.freewheel.FreeWheelBackend.servicios.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/usuarios/conductores")
@CrossOrigin("*")
public class DriverController {

    private final DriverService driverService;

    @Autowired
    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<DriverDTO> createDriver(@RequestBody DriverDTO driverDTO) {
        return ResponseEntity.ok(driverService.createDriver(driverDTO));
    }

    @PostMapping(value = "/registrar-con-licencia",  consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DriverDTO> createDriverConLicencia(
        @RequestParam("usuarioId") long usuarioId,
        @RequestParam("licenciaConduccion") MultipartFile licenciaConduccion
    ) {

        try {
            if (licenciaConduccion.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            if (!isPdfFile(licenciaConduccion)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(DriverDTO.builder().build());
            }

            DriverDTO driverDTO = DriverDTO.builder()
                    .usuarioId(usuarioId)
                    .build();

            // Llamar al servicio
            DriverDTO createdVehicle = driverService.createDriverWithLicencia(
                    driverDTO, licenciaConduccion);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdVehicle);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    private boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.equals("application/pdf");
    }
}

