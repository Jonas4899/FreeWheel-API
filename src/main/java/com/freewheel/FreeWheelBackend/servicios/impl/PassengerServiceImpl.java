package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.PassengerDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.PassengerRequestDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.TripDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.PassengerEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.UserEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.PassengerRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.UserRepository;
import com.freewheel.FreeWheelBackend.servicios.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepository;
    private final UserRepository userRepository;

    @Autowired
    public PassengerServiceImpl(PassengerRepository passengerRepository, UserRepository userRepository) {
        this.passengerRepository = passengerRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PassengerDTO createPassenger(PassengerRequestDTO requestDTO) {
        long viajeId = requestDTO.getViajeId();
        long userId = requestDTO.getUsuarioId();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        PassengerEntity passengerEntity = PassengerEntity.builder()
                .usuario(user)
                .viajeId(viajeId)
                .pagoRealizado(false)    // Valor por defecto
                .estado("PENDIENTE")      // Valor por defecto
                .build();

        PassengerEntity savedPassenger = passengerRepository.save(passengerEntity);

        UserDTO pasajeroDTO = UserDTO.builder()
                .id(user.getId())
                .nombre(user.getNombre())
                .apellido(user.getApellido())
                .correo(user.getCorreo())
                .telefono(user.getTelefono())
                .fotoPerfil(user.getFotoPerfil())
                .organizacionCodigo(user.getOrganizacion() != null ? user.getOrganizacion().getCodigo() : null)
                .build();

        TripDTO tripDTO = TripDTO.builder().id(viajeId).build();

        return PassengerDTO.builder()
                .id(savedPassenger.getId())
                .viaje(tripDTO)
                .pasajero(pasajeroDTO)
                .pagoRealizado(false)
                .estado("PENDIENTE")
                .build();
    }
}