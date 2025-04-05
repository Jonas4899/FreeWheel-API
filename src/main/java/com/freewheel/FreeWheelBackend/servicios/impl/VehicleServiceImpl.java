package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.VehicleDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.DriverEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.VehicleEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.DriverRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.VehicleRepository;
import com.freewheel.FreeWheelBackend.servicios.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    @Autowired
    public VehicleServiceImpl(VehicleRepository vehicleRepository, DriverRepository driverRepository ) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public VehicleDTO createVehicle(VehicleDTO vehicleDTO) {
        //obtener el conductor
        DriverEntity driver = driverRepository.findById(vehicleDTO.getConductorId())
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        //Crear el vehiculo
        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setPlaca(vehicleDTO.getPlaca());
        vehicle.setMarca(vehicleDTO.getMarca());
        vehicle.setModelo(vehicleDTO.getModelo());
        vehicle.setAnio(vehicleDTO.getAnio());
        vehicle.setColor(vehicleDTO.getColor());
        vehicle.setTipo(vehicleDTO.getTipo());
        vehicle.setCapacidadPasajeros(vehicleDTO.getCapacidadPasajeros());
        vehicle.setLicenciaTransito(vehicleDTO.getLicenciaTransito());
        vehicle.setSoat(vehicleDTO.getSoat());
        vehicle.setCertificadoRevision(vehicleDTO.getCertificadoRevision());
        vehicle.setConductor(driver);
        vehicle.setFoto(vehicleDTO.getFoto());

        //Guardar el vehiculo en la BD
        VehicleEntity savedVehicle = vehicleRepository.save(vehicle);

        return new VehicleDTO(
            savedVehicle.getId(),
            savedVehicle.getPlaca(),
            savedVehicle.getMarca(),
            savedVehicle.getModelo(),
            savedVehicle.getAnio(),
            savedVehicle.getColor(),
            savedVehicle.getTipo(),
            savedVehicle.getCapacidadPasajeros(),
            savedVehicle.getLicenciaTransito(),
            savedVehicle.getSoat(),
            savedVehicle.getCertificadoRevision(),
            savedVehicle.getConductor().getId(),
            savedVehicle.getFoto()
        );
    }

    @Override
    public List<VehicleEntity> getAllVehicles() {
        return vehicleRepository.findAll();
    }
}
