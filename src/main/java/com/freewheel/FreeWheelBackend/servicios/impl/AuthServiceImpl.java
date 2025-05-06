package com.freewheel.FreeWheelBackend.servicios.impl;
import com.freewheel.FreeWheelBackend.persistencia.dtos.AuthRequestDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.AuthResponseDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.DriverEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.UserEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.DriverRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.UserRepository;
import com.freewheel.FreeWheelBackend.seguridad.JwtUtils;
import com.freewheel.FreeWheelBackend.servicios.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDTO login(AuthRequestDTO authRequest) {
        UserEntity usuario = userRepository.findByCorreo(authRequest.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(authRequest.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        Optional<DriverEntity> driverOpt = driverRepository.findByUsuario_Id(usuario.getId());
        boolean isDriver = driverRepository.findByUsuario_Id(usuario.getId()).isPresent();
        Long conductorId = driverOpt.map(DriverEntity::getId).orElse(null);

        UserDTO userDTO = UserDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .fotoPerfil(usuario.getFotoPerfil())
                .organizacionCodigo(usuario.getOrganizacion().getCodigo())
                .isDriver(isDriver)
                .conductorId(conductorId)
                .build();

        String token = jwtUtils.generateToken(userDTO);

        return AuthResponseDTO.builder()
                .token(token)
                .usuario(userDTO)
                .build();
    }
}
