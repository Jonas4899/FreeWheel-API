package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.DriverDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.DriverEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.UserEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.DriverRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.UserRepository;
import com.freewheel.FreeWheelBackend.servicios.DriverService;
import com.freewheel.FreeWheelBackend.servicios.StorageService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class DriverServiceImpl implements DriverService {
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Autowired
    public DriverServiceImpl(DriverRepository driverRepository,UserRepository userRepository, StorageService storageService) {
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @Override
    public DriverDTO createDriver(DriverDTO driverDTO) {
        //Obtener el usuario
        UserEntity user = userRepository.findById(driverDTO.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        //Crear el conductor
        DriverEntity savedDriver = DriverEntity.builder()
                .usuario(user)
                .licenciaConduccion(driverDTO.getLicenciaConduccion())
                .build();
        //Guardar el conductor en la BD
        savedDriver = driverRepository.save(savedDriver);

        return DriverDTO.builder()
                .id(savedDriver.getId())
                .usuarioId(savedDriver.getUsuario().getId())
                .licenciaConduccion(savedDriver.getLicenciaConduccion())
                .build();

    }

    @Override
    @Transactional
    public DriverDTO createDriverWithLicencia(DriverDTO driverDTO, MultipartFile licenciaConduccion) {
        // Obtener el usuario
        UserEntity user = userRepository.findById(driverDTO.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {
            String licenciaConduccionFileName = "licenciaConduccion_" + driverDTO.getLicenciaConduccion() + "_" + UUID.randomUUID().toString();
            String licenciaConduccionUrl = storageService.uploadFile(licenciaConduccion, licenciaConduccionFileName, "licencias-conduccion");

            DriverEntity savedDriver = DriverEntity.builder()
                    .usuario(user)
                    .licenciaConduccion(licenciaConduccionUrl)
                    .build();

            savedDriver = driverRepository.save(savedDriver);

            return DriverDTO.builder()
                    .id(savedDriver.getId())
                    .usuarioId(savedDriver.getUsuario().getId())
                    .licenciaConduccion(savedDriver.getLicenciaConduccion())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al crear conductor: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado al crear conductor: " + e.getMessage());
        }
    }
}
