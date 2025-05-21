package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.PassengerDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.PassengerRequestDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.TripDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.PassengerEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.TripEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.UserEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.PassengerRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.TripRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.UserRepository;
import com.freewheel.FreeWheelBackend.servicios.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    @Autowired
    public PassengerServiceImpl(PassengerRepository passengerRepository, UserRepository userRepository, TripRepository tripRepository) {
        this.passengerRepository = passengerRepository;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
    }

    @Override
    public PassengerDTO createPassenger(PassengerRequestDTO requestDTO) {
        long viajeId = requestDTO.getViajeId();
        long userId = requestDTO.getUsuarioId();
        int asientosSolicitados = requestDTO.getAsientosSolicitados();

        // Obtener información del usuario
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener información del viaje para verificar asientos disponibles
        TripEntity viaje = tripRepository.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("viaje no encontrado"));

        if (asientosSolicitados > viaje.getAsientosDisponibles()) {
            throw new RuntimeException("No hay suficientes asientos disponibles. " +
                    "Asientos solicitados: " + asientosSolicitados +
                    ", Asientos disponibles: " + viaje.getAsientosDisponibles());
        }

        PassengerEntity passengerEntity = PassengerEntity.builder()
                .usuario(user)
                .viajeId(viajeId)
                .asientosSolicitados(asientosSolicitados)
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

    //Obtener todos los pasajeros pendientes de un determinado viaje
    @Override
    public List<PassengerDTO> getPendingPassengersByTripId(long tripId) {
        List<PassengerEntity> passengers = passengerRepository.findByViajeIdAndEstado(tripId, "PENDIENTE");

        return passengers.stream()
            .map(entity -> {
                UserEntity user = entity.getUsuario();

                UserDTO pasajeroDTO = UserDTO.builder()
                        .id(user.getId())
                        .nombre(user.getNombre())
                        .apellido(user.getApellido())
                        .correo(user.getCorreo())
                        .telefono(user.getTelefono())
                        .fotoPerfil(user.getFotoPerfil())
                        .organizacionCodigo(user.getOrganizacion() != null ?
                                user.getOrganizacion().getCodigo() : null)
                        .build();

                TripDTO tripDTO = TripDTO.builder().id(entity.getViajeId()).build();

                return PassengerDTO.builder()
                        .id(entity.getId())
                        .viaje(tripDTO)
                        .pasajero(pasajeroDTO)
                        .asientosSolicitados(entity.getAsientosSolicitados())
                        .pagoRealizado(entity.isPagoRealizado())
                        .estado(entity.getEstado())
                        .build();
            })
            .collect(Collectors.toList());
    }
}