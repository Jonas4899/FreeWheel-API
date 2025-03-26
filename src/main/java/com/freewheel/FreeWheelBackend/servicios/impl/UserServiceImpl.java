package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.OrganizationEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.UserEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.OrganizationRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.UserRepository;
import com.freewheel.FreeWheelBackend.servicios.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        // Validar que el correo no esté registrado
        if (userRepository.findByCorreo(userDTO.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        // Buscar la organización
        OrganizationEntity organizacion = organizationRepository.findById(userDTO.getOrganizacionId())
                .orElseThrow(() -> new RuntimeException("Organización no encontrada"));

        final UserEntity userEntity = userDtoToUserEntity(userDTO);
        userEntity.setOrganizacion(organizacion);

        final UserEntity savedUserEntity = userRepository.save(userEntity);

        return userEntityToUserDto(savedUserEntity);
    }

    private UserEntity userDtoToUserEntity(UserDTO userDTO) {
        return UserEntity.builder()
                .nombre(userDTO.getNombre())
                .correo(userDTO.getCorreo())
                .telefono(userDTO.getTelefono())
                .organizacion(organizationRepository.findById(userDTO.getOrganizacionId())
                        .orElseThrow(() -> new RuntimeException("Organización no encontrada")))
                .build();
    }

    private UserDTO userEntityToUserDto(UserEntity userEntity) {
        return UserDTO.builder()
                .nombre(userEntity.getNombre())
                .correo(userEntity.getCorreo())
                .telefono(userEntity.getTelefono())
                .organizacionId(userEntity.getOrganizacion().getId())
                .build();
    }
}
