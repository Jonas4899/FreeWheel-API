package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.VehicleDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.VehicleEntity;

import java.util.List;

public interface VehicleService {
    VehicleDTO createVehicle(VehicleDTO vehicleDTO);
    List<VehicleEntity> getAllVehicles();
    //VehicleDTO getVehicleById(Long id);
    //VehicleDTO getVehicleByPlaca(String placa);
    //VehicleDTO getVechicleByDriver(long conductor_id);
}
