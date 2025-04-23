package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.OrganizationEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.UserEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.OrganizationRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.UserRepository;
import com.freewheel.FreeWheelBackend.servicios.StorageService;
import com.freewheel.FreeWheelBackend.servicios.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StorageService storageService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           OrganizationRepository organizationRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           StorageService storageService) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
    }

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        // Validar que el correo no esté registrado
        if (userRepository.findByCorreo(userDTO.getCorreo()).isPresent()) {
            // Considera lanzar una excepción más específica, ej: CorreoYaRegistradoException
            throw new RuntimeException("El correo ya está registrado");
        }

        // Buscar la organización por código
        OrganizationEntity organizacion = organizationRepository.findByCodigo(userDTO.getOrganizacionCodigo())
                .orElseThrow(() -> new RuntimeException("Organización no encontrada")); // Esta excepción fue la que te salió antes

        final UserEntity userEntity = userDtoToUserEntity(userDTO);
        userEntity.setOrganizacion(organizacion); // Asegúrate de asignar la organización encontrada

        final UserEntity savedUserEntity = userRepository.save(userEntity);

        return userEntityToUserDto(savedUserEntity);
    }

    @Override
    @Transactional
    public UserDTO createUserWithProfileImage(UserDTO userDTO, MultipartFile profileImage) throws IOException {
        // Validar que el correo no esté registrado
        if (userRepository.findByCorreo(userDTO.getCorreo()).isPresent()) {
            // Considera lanzar una excepción más específica
            throw new RuntimeException("El correo ya está registrado");
        }

        // Buscar la organización por código
        OrganizationEntity organizacion = organizationRepository.findByCodigo(userDTO.getOrganizacionCodigo())
                .orElseThrow(() -> new RuntimeException("Organización no encontrada"));

        // Generate a unique filename for the profile image
        String fileName = "perfil_" + UUID.randomUUID().toString();

        // Upload the file and get its URL
        String imageUrl = storageService.uploadFile(profileImage, fileName, "fotos-perfil");

        // Set the profile image URL in the DTO before conversion
        userDTO.setFotoPerfil(imageUrl);

        final UserEntity userEntity = userDtoToUserEntity(userDTO);
        userEntity.setOrganizacion(organizacion); // Asigna la organización

        final UserEntity savedUserEntity = userRepository.save(userEntity);

        return userEntityToUserDto(savedUserEntity);
    }

    private UserEntity userDtoToUserEntity(UserDTO userDTO) {
        // Ya no necesitas buscar la organización aquí si la pasas como parámetro
        // o si la asignas después de crear el builder como en createUser()
        return UserEntity.builder()
                .nombre(userDTO.getNombre())
                .apellido(userDTO.getApellido())
                .correo(userDTO.getCorreo())
                .telefono(userDTO.getTelefono())
                .password(passwordEncoder.encode(userDTO.getPassword())) // Cambiado contraseña por password
                .fotoPerfil(userDTO.getFotoPerfil())
                // La organización se asigna fuera de este método ahora en createUser/createUserWithImage
                // .organizacion(organizationRepository.findByCodigo(userDTO.getOrganizacionCodigo())
                //        .orElseThrow(() -> new RuntimeException("Organización no encontrada")))
                .build();
    }

    private UserDTO userEntityToUserDto(UserEntity userEntity) {
        return UserDTO.builder()
                .id(userEntity.getId())
                .nombre(userEntity.getNombre())
                .apellido(userEntity.getApellido())
                .correo(userEntity.getCorreo())
                .telefono(userEntity.getTelefono())
                // Por seguridad, normalmente no se devuelve la contraseña
                // .password(userEntity.getPassword())
                .fotoPerfil(userEntity.getFotoPerfil())
                // Asegúrate de que la organización no sea null antes de acceder a getCodigo()
                .organizacionCodigo(userEntity.getOrganizacion() != null ? userEntity.getOrganizacion().getCodigo() : null)
                .build();
    }
}