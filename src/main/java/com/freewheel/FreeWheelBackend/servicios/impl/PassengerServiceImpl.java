package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.PassengerDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.PassengerRequestDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.TripDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.*;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.PassengerRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.TripRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.UserRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.DriverRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.VehicleRepository;
import com.freewheel.FreeWheelBackend.servicios.PassengerService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class PassengerServiceImpl implements PassengerService {

    // Incluir calificaciones también
    //@Autowired
    //private RatingRepository ratingRepository;

    private final PassengerRepository passengerRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    @Autowired
    public PassengerServiceImpl(PassengerRepository passengerRepository, UserRepository userRepository, TripRepository tripRepository, VehicleRepository vehicleRepository, DriverRepository driverRepository) {
        this.passengerRepository = passengerRepository;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public PassengerDTO createPassenger(PassengerRequestDTO requestDTO) {
        long viajeId = requestDTO.getViajeId();
        long userId = requestDTO.getUsuarioId();
        int asientosSolicitados = requestDTO.getAsientosSolicitados();

        // Verificar si el pasajero ya está registrado en este viaje
        if (passengerRepository.existsByUsuario_IdAndViajeId(userId, viajeId)) {
            throw new RuntimeException("Ya has solicitado un asiento en este viaje");
        }

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

    private PassengerDTO convertToDTO(PassengerEntity entity) {
        // Obtener información completa del viaje para el DTO
        TripEntity tripEntity = tripRepository.findById(entity.getViajeId()).orElse(null);
        TripDTO tripDTO = null;

        if (tripEntity != null) {
            // Obtener información del conductor - CORREGIDO
            DriverEntity driverEntity = driverRepository.findById(tripEntity.getConductorId()).orElse(null);
            UserEntity conductorUser = driverEntity != null ? driverEntity.getUsuario() : null;

            // Obtener información del vehículo del conductor
            List<VehicleEntity> vehiculos = vehicleRepository.findByConductorId(tripEntity.getConductorId());
            VehicleEntity vehiculo = vehiculos != null && !vehiculos.isEmpty() ? vehiculos.get(0) : null;

            tripDTO = TripDTO.builder()
                    .id(tripEntity.getId())
                    .conductorId(tripEntity.getConductorId())
                    .nombreConductor(conductorUser != null ? conductorUser.getNombre() : null)
                    .apellidoConductor(conductorUser != null ? conductorUser.getApellido() : null)
                    .fotoConductor(conductorUser != null ? conductorUser.getFotoPerfil() : null)
                    .telefonoConductor(conductorUser != null ? conductorUser.getTelefono() : null)
                    .fecha(tripEntity.getFecha())
                    .horaInicio(tripEntity.getHoraInicio())
                    .horaFin(tripEntity.getHoraFin())
                    .precioAsiento(tripEntity.getPrecioAsiento())
                    .asientosDisponibles(tripEntity.getAsientosDisponibles())
                    .direccionOrigen(tripEntity.getDireccionOrigen())
                    .latitudOrigen(tripEntity.getLatitudOrigen())
                    .longitudOrigen(tripEntity.getLongitudOrigen())
                    .direccionDestino(tripEntity.getDireccionDestino())
                    .latitudDestino(tripEntity.getLatitudDestino())
                    .longitudDestino(tripEntity.getLongitudDestino())
                    .estado(tripEntity.getEstado())
                    // Agregar información del vehículo
                    .vehiculoPlaca(vehiculo != null ? vehiculo.getPlaca() : null)
                    .vehiculoMarca(vehiculo != null ? vehiculo.getMarca() : null)
                    .vehiculoModelo(vehiculo != null ? vehiculo.getModelo() : null)
                    .vehiculoColor(vehiculo != null ? vehiculo.getColor() : null)
                    .vehiculoTipo(vehiculo != null ? vehiculo.getTipo() : null)
                    .vehiculoFoto(vehiculo != null ? vehiculo.getFoto() : null)
                    .build();
        }

        return PassengerDTO.builder()
                .id(entity.getId())
                .viaje(tripDTO)
                .asientosSolicitados(entity.getAsientosSolicitados())
                .pagoRealizado(entity.isPagoRealizado())
                .estado(entity.getEstado())
                .build();
    }

    //Obtener los viajes usando el usuario_id
    @Override
    public List<PassengerDTO> getPassengerTripsByUserId(long userId) {
        List<PassengerEntity> passengerEntities = passengerRepository.findByUsuario_Id(userId);

        // Filtrar viajes por estado del viaje y estado del pasajero
        List<PassengerDTO> passengerDTOs = passengerEntities.stream()
                .filter(passenger -> {
                    TripEntity trip = tripRepository.findById(passenger.getViajeId()).orElse(null);
                    return trip != null &&
                            (trip.getEstado().equals("por iniciar") || trip.getEstado().equals("iniciado")) &&
                            (passenger.getEstado().equals("PENDIENTE") || passenger.getEstado().equals("ACEPTADO"));
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Ordenar por fecha (más cercana primero) y luego por hora de inicio
        passengerDTOs.sort(Comparator.comparing((PassengerDTO dto) -> {
            TripDTO trip = dto.getViaje();
            return trip != null ? trip.getFecha() : null;
        }).thenComparing((PassengerDTO dto) -> {
            TripDTO trip = dto.getViaje();
            return trip != null ? trip.getHoraInicio() : null;
        }));

        return passengerDTOs;
    }

    //Eliminar un pasajero
    @Override
    @Transactional
    public Map<String, Object> removePassengerFromTrip(long usuarioId, long viajeId) {
        // Buscar el pasajero por usuarioId y viajeId
        List<PassengerEntity> passengersFound = passengerRepository.findByUsuario_IdAndViajeId(usuarioId, viajeId);

        if (passengersFound.isEmpty()) {
            throw new RuntimeException("No se encontró ningún registro de pasajero para el usuario " + usuarioId + " en el viaje " + viajeId);
        }

        PassengerEntity passengerEntity = passengersFound.get(0);
        String estadoPasajero = passengerEntity.getEstado();
        int asientosSolicitados = passengerEntity.getAsientosSolicitados();

        // Si el estado es ACEPTADO, actualizar los asientos disponibles en el viaje
        if ("ACEPTADO".equals(estadoPasajero)) {
            TripEntity tripEntity = tripRepository.findById(viajeId)
                    .orElseThrow(() -> new RuntimeException("Viaje no encontrado con ID: " + viajeId));

            // Actualizar asientos disponibles
            int nuevosAsientosDisponibles = tripEntity.getAsientosDisponibles() + asientosSolicitados;
            tripEntity.setAsientosDisponibles(nuevosAsientosDisponibles);
            tripRepository.save(tripEntity);
        }

        // Eliminar al pasajero
        passengerRepository.delete(passengerEntity);

        // Preparar respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Pasajero eliminado del viaje correctamente");
        response.put("estadoPrevio", estadoPasajero);
        response.put("asientosSolicitados", asientosSolicitados);
        response.put("asientosActualizados", "ACEPTADO".equals(estadoPasajero));

        return response;
    }

}