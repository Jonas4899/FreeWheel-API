package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.VehicleDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.DriverEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.VehicleEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.DriverRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.VehicleRepository;
import com.freewheel.FreeWheelBackend.servicios.StorageService;
import com.freewheel.FreeWheelBackend.servicios.VehicleService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final StorageService storageService;

    @Autowired
    public VehicleServiceImpl(VehicleRepository vehicleRepository, DriverRepository driverRepository, StorageService storageService ) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.storageService = storageService;
    }

    @Override
    public VehicleDTO createVehicle(VehicleDTO vehicleDTO) {
        // Obtener el conductor
        DriverEntity driver = driverRepository.findById(vehicleDTO.getConductorId())
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        // Crear y guardar el vehículo usando Builder
        VehicleEntity savedVehicle = VehicleEntity.builder()
                .placa(vehicleDTO.getPlaca())
                .marca(vehicleDTO.getMarca())
                .modelo(vehicleDTO.getModelo())
                .anio(vehicleDTO.getAnio())
                .color(vehicleDTO.getColor())
                .tipo(vehicleDTO.getTipo())
                .capacidadPasajeros(vehicleDTO.getCapacidadPasajeros())
                .licenciaTransito(vehicleDTO.getLicenciaTransito())
                .soat(vehicleDTO.getSoat())
                .certificadoRevision(vehicleDTO.getCertificadoRevision())
                .conductor(driver)
                .foto(vehicleDTO.getFoto())
                .build();

        savedVehicle = vehicleRepository.save(savedVehicle);

        // Convertir a DTO y retornar usando Builder
        return VehicleDTO.builder()
                .id(savedVehicle.getId())
                .placa(savedVehicle.getPlaca())
                .marca(savedVehicle.getMarca())
                .modelo(savedVehicle.getModelo())
                .anio(savedVehicle.getAnio())
                .color(savedVehicle.getColor())
                .tipo(savedVehicle.getTipo())
                .capacidadPasajeros(savedVehicle.getCapacidadPasajeros())
                .licenciaTransito(savedVehicle.getLicenciaTransito())
                .soat(savedVehicle.getSoat())
                .certificadoRevision(savedVehicle.getCertificadoRevision())
                .conductorId(savedVehicle.getConductor().getId())
                .foto(savedVehicle.getFoto())
                .build();
    }

    @Override
    @Transactional
    public VehicleDTO createVehicleWithDocuments(VehicleDTO vehicleDTO, MultipartFile licenciaTransito, MultipartFile soat, MultipartFile certificadoRevision, MultipartFile foto) {
        //obtener el conductor
        DriverEntity driver = driverRepository.findById(vehicleDTO.getConductorId())
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        try {
            //generar nombres unicos para cada archivo
            String licenciaFileName = "licencia_" + vehicleDTO.getPlaca() + "_" + UUID.randomUUID().toString();
            String soatFileName = "soat_" + vehicleDTO.getPlaca() + "_" + UUID.randomUUID().toString();
            String certificadoFileName = "certificado_" + vehicleDTO.getPlaca() + "_" + UUID.randomUUID().toString();
            String fotoFileName = "vehiculo_" + vehicleDTO.getPlaca() + "_" + UUID.randomUUID().toString();

            // Subir archivos y obtener URLs
            String licenciaTransitoUrl = storageService.uploadFile(licenciaTransito, licenciaFileName, "licencias-transito");
            String soatUrl = storageService.uploadFile(soat, soatFileName, "certificados-soat");
            String certificadoRevisionUrl = storageService.uploadFile(certificadoRevision, certificadoFileName, "certificados-revision");
            String fotoUrl = storageService.uploadFile(foto, fotoFileName, "fotos-vehiculo");

            VehicleEntity savedVehicle = VehicleEntity.builder()
                    .placa(vehicleDTO.getPlaca())
                    .marca(vehicleDTO.getMarca())
                    .modelo(vehicleDTO.getModelo())
                    .anio(vehicleDTO.getAnio())
                    .color(vehicleDTO.getColor())
                    .tipo(vehicleDTO.getTipo())
                    .capacidadPasajeros(vehicleDTO.getCapacidadPasajeros())
                    .licenciaTransito(licenciaTransitoUrl)
                    .soat(soatUrl)
                    .certificadoRevision(certificadoRevisionUrl)
                    .conductor(driver)
                    .foto(fotoUrl)
                    .build();

            //Guardar el vehiculo en la BD
            savedVehicle = vehicleRepository.save(savedVehicle);

            // Convertir a DTO y retornar
            return VehicleDTO.builder()
                    .id(savedVehicle.getId())
                    .placa(savedVehicle.getPlaca())
                    .marca(savedVehicle.getMarca())
                    .modelo(savedVehicle.getModelo())
                    .anio(savedVehicle.getAnio())
                    .color(savedVehicle.getColor())
                    .tipo(savedVehicle.getTipo())
                    .capacidadPasajeros(savedVehicle.getCapacidadPasajeros())
                    .licenciaTransito(savedVehicle.getLicenciaTransito())
                    .soat(savedVehicle.getSoat())
                    .certificadoRevision(savedVehicle.getCertificadoRevision())
                    .conductorId(savedVehicle.getConductor().getId())
                    .foto(savedVehicle.getFoto())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Error al subir los documentos del vehiculo");
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al crear el vehiculo: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado al crear el vehiculo: " + e.getMessage());
        }
    }
}
