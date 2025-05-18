package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.DriverDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import org.springframework.web.multipart.MultipartFile;

public interface DriverService {
    DriverDTO createDriver(DriverDTO driverDTO);
    DriverDTO createDriverWithLicencia(DriverDTO driverDTO, MultipartFile licenciaFrontal, MultipartFile licenciaTrasera);
    UserDTO getUserByDriverId(Long driverId);
    DriverDTO getDriverByUser(Long userId);
}