package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.DriverDTO;

import java.util.List;
import java.util.Optional;

public interface DriverService {
    DriverDTO createDriver(DriverDTO driverDTO);
    //List<DriverDTO> getAllDrivers();
    //Optional<DriverDTO> getByUser_id(Long usuario_id);
    //DriverDTO updateDriver(DriverDTO driverDTO);
}
