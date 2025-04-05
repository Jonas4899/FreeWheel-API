package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.DriverDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface DriverService {
    DriverDTO createDriver(DriverDTO driverDTO);
    DriverDTO createDriverWithLicencia(DriverDTO driverDTO, MultipartFile licenciaConduccion);
}
