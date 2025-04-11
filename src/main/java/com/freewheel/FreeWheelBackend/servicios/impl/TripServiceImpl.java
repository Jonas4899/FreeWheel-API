package com.freewheel.FreeWheelBackend.servicios.impl;

import com.freewheel.FreeWheelBackend.persistencia.dtos.DriverDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.TripDTO;
import com.freewheel.FreeWheelBackend.persistencia.entidades.DriverEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.TripEntity;
import com.freewheel.FreeWheelBackend.persistencia.entidades.VehicleEntity;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.DriverRepository;
import com.freewheel.FreeWheelBackend.persistencia.repositorios.TripRepository;
import com.freewheel.FreeWheelBackend.servicios.TripService;
import org.springframework.stereotype.Service;

@Service
public class TripServiceImpl implements TripService {
    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;

    public TripServiceImpl(TripRepository tripRepository, DriverRepository driverRepository) {
        this.tripRepository = tripRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public TripDTO createTrip(TripDTO tripDTO) {
        DriverEntity driver = driverRepository.findById(tripDTO.getConductorId())
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        TripEntity savedTrip = TripEntity.builder()
                .conductorId(tripDTO.getConductorId())
                .fecha(tripDTO.getFecha())
                .horaInicio(tripDTO.getHoraInicio())
                .horaFin(tripDTO.getHoraFin())
                .precioAsiento(tripDTO.getPrecioAsiento())
                .asientosDisponibles(tripDTO.getAsientosDisponibles())
                .direccionOrigen(tripDTO.getDireccionOrigen())
                .latitudOrigen(tripDTO.getLatitudOrigen())
                .longitudDestino(tripDTO.getLongitudDestino())
                .direccionDestino(tripDTO.getDireccionDestino())
                .latitudDestino(tripDTO.getLatitudDestino())
                .longitudDestino(tripDTO.getLongitudDestino())
                .estado(tripDTO.getEstado())
                .build();

        savedTrip = tripRepository.save(savedTrip);

        return TripDTO.builder()
                .id(savedTrip.getId())
                .conductorId(savedTrip.getConductorId())
                .fecha(savedTrip.getFecha())
                .horaInicio(savedTrip.getHoraInicio())
                .horaFin(savedTrip.getHoraFin())
                .precioAsiento(savedTrip.getPrecioAsiento())
                .asientosDisponibles(savedTrip.getAsientosDisponibles())
                .direccionOrigen(savedTrip.getDireccionOrigen())
                .latitudOrigen(savedTrip.getLatitudOrigen())
                .longitudDestino(savedTrip.getLongitudDestino())
                .direccionDestino(savedTrip.getDireccionDestino())
                .latitudDestino(savedTrip.getLatitudDestino())
                .longitudDestino(savedTrip.getLongitudDestino())
                .estado(savedTrip.getEstado())
                .build();
    }
}
