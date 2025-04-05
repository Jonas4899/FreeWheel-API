package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.DriverDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.DriverEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.UserEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.DriverRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.UserRepository;
import com.freewheel.FreeWheelBackend.servicios.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DriverServiceImpl implements DriverService {
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    @Autowired
    public DriverServiceImpl(DriverRepository driverRepository,UserRepository userRepository) {
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
    }

    @Override
    public DriverDTO createDriver(DriverDTO driverDTO) {
        //Obtener el usuario
        UserEntity user = userRepository.findById(driverDTO.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        //Crear el conductor
        DriverEntity driver = new DriverEntity();
        driver.setLicenciaConduccion(driverDTO.getLicenciaConduccion());
        driver.setUsuario(user);

        //Guardar el conductor en la BD
        DriverEntity savedDriver = driverRepository.save(driver);

        return new DriverDTO(
                savedDriver.getId(),
                savedDriver.getUsuario().getId(),
                savedDriver.getLicenciaConduccion()
        );

    }
}
