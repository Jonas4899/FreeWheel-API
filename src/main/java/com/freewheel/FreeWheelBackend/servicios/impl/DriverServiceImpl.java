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
import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class DriverServiceImpl implements DriverService {
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Autowired
    public DriverServiceImpl(DriverRepository driverRepository, UserRepository userRepository, StorageService storageService) {
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @Override
    public DriverDTO createDriver(DriverDTO driverDTO) {
        // Verificar si el usuario ya está registrado como conductor
        Optional<DriverEntity> existingDriver = driverRepository.findByUsuario_Id(driverDTO.getUsuarioId());
        if (existingDriver.isPresent()) {
            throw new RuntimeException("El usuario ya está registrado como conductor");
        }

        // Obtener el usuario
        UserEntity user = userRepository.findById(driverDTO.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear el conductor
        DriverEntity savedDriver = DriverEntity.builder()
                .usuario(user)
                .licenciaConduccionFrontal(driverDTO.getLicenciaConduccionFrontal())
                .licenciaConduccionTrasera(driverDTO.getLicenciaConduccionTrasera())
                .build();

        // Guardar el conductor en la BD
        savedDriver = driverRepository.save(savedDriver);

        return DriverDTO.builder()
                .id(savedDriver.getId())
                .usuarioId(savedDriver.getUsuario().getId())
                .licenciaConduccionFrontal(savedDriver.getLicenciaConduccionFrontal())
                .licenciaConduccionTrasera(savedDriver.getLicenciaConduccionTrasera())
                .build();
    }

    @Override
    @Transactional
    public DriverDTO createDriverWithLicencia(DriverDTO driverDTO, MultipartFile licenciaFrontal, MultipartFile licenciaTrasera) {
        // Verificar si el usuario ya está registrado como conductor
        Optional<DriverEntity> existingDriver = driverRepository.findByUsuario_Id(driverDTO.getUsuarioId());
        if (existingDriver.isPresent()) {
            throw new RuntimeException("El usuario ya está registrado como conductor");
        }

        // Obtener el usuario
        UserEntity user = userRepository.findById(driverDTO.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {
            String licenciaFrontalFileName = "licenciaFrontal_" + UUID.randomUUID().toString();
            String licenciaTraseraFileName = "licenciaTrasera_" + UUID.randomUUID().toString();

            String licenciaFrontalUrl = storageService.uploadFile(licenciaFrontal, licenciaFrontalFileName, "licencias-conduccion");
            String licenciaTraseraUrl = storageService.uploadFile(licenciaTrasera, licenciaTraseraFileName, "licencias-conduccion");

            DriverEntity savedDriver = DriverEntity.builder()
                    .usuario(user)
                    .licenciaConduccionFrontal(licenciaFrontalUrl)
                    .licenciaConduccionTrasera(licenciaTraseraUrl)
                    .build();

            savedDriver = driverRepository.save(savedDriver);

            return DriverDTO.builder()
                    .id(savedDriver.getId())
                    .usuarioId(savedDriver.getUsuario().getId())
                    .licenciaConduccionFrontal(savedDriver.getLicenciaConduccionFrontal())
                    .licenciaConduccionTrasera(savedDriver.getLicenciaConduccionTrasera())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al crear conductor: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado al crear conductor: " + e.getMessage());
        }
    }

    @Override
    @Transactional // Opcional, dependiendo si necesitas transaccionalidad aquí
    public UserDTO getUserByDriverId(Long driverId) {
        Optional<DriverEntity> driverEntityOptional = driverRepository.findById(driverId);
        if (driverEntityOptional.isPresent()) {
            DriverEntity driverEntity = driverEntityOptional.get();
            UserEntity userEntity = driverEntity.getUsuario();
            if (userEntity != null) {
                // Mapear UserEntity a UserDTO
                // Esta lógica de mapeo podría estar en un método helper o directamente aquí
                return UserDTO.builder()
                        .id(userEntity.getId())
                        .nombre(userEntity.getNombre())
                        .apellido(userEntity.getApellido())
                        .correo(userEntity.getCorreo())
                        .telefono(userEntity.getTelefono())
                        .fotoPerfil(userEntity.getFotoPerfil())
                        .organizacionCodigo(userEntity.getOrganizacion() != null ? userEntity.getOrganizacion().getCodigo() : null)
                        .build();
            }
        }
        return null;
    }
}