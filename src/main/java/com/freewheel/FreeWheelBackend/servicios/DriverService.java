package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.DriverDTO;
import org.springframework.web.multipart.MultipartFile;

public interface DriverService {
    DriverDTO createDriver(DriverDTO driverDTO);
    DriverDTO createDriverWithLicencia(DriverDTO driverDTO, MultipartFile licenciaFrontal, MultipartFile licenciaTrasera);
}