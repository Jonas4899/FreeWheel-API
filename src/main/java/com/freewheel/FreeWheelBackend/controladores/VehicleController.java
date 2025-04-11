package com.freewheel.FreeWheelBackend.controladores;

import com.freewheel.FreeWheelBackend.persistencia.dtos.VehicleDTO;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import com.freewheel.FreeWheelBackend.servicios.VehicleService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/vehiculos")
public class VehicleController {
    private final VehicleService vehicleService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<VehicleDTO> createVehicle(@RequestBody VehicleDTO vehicleDTO) {
        try {
            VehicleDTO newVehicle = vehicleService.createVehicle(vehicleDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(newVehicle);
        } catch (Exception e) {
            System.out.println("Error al crear el vehiculo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping(value= "/registrar-con-documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VehicleDTO> createVehicleConDocumentos(
            @RequestParam("placa") String placa,
            @RequestParam("marca") String marca,
            @RequestParam("modelo") String modelo,
            @RequestParam("anio") int anio,
            @RequestParam("color") String color,
            @RequestParam("tipo") String tipo,
            @RequestParam("capacidadPasajeros") int capacidadPasajeros,
            @RequestParam("licenciaTransito") MultipartFile licenciaTransito,
            @RequestParam("soat") MultipartFile soat,
            @RequestParam("certificadoRevision") MultipartFile certificadoRevision,
            @RequestParam("conductorId") long conductorId,
            @RequestParam("foto") MultipartFile foto) {

        try {
            //validar archivos
            if (licenciaTransito.isEmpty() || soat.isEmpty() || certificadoRevision.isEmpty() || foto.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Validar que los documentos sean PDF
            if (!isPdfFile(licenciaTransito)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(VehicleDTO.builder().build());
            }

            if (!isPdfFile(soat)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(VehicleDTO.builder().build());
            }

            if (!isPdfFile(certificadoRevision)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(VehicleDTO.builder().build());
            }

            // Validar que la foto sea una imagen
            if (!isImageFile(foto)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(VehicleDTO.builder().build());
            }

            VehicleDTO vehicleDTO = VehicleDTO.builder()
                    .placa(placa)
                    .marca(marca)
                    .modelo(modelo)
                    .anio(anio)
                    .color(color)
                    .tipo(tipo)
                    .capacidadPasajeros(capacidadPasajeros)
                    .conductorId(conductorId)
                    .build();

            // Llamar al servicio
            VehicleDTO createdVehicle = vehicleService.createVehicleWithDocuments(
                    vehicleDTO, licenciaTransito, soat, certificadoRevision, foto);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.equals("application/pdf");
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}
