package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.PassengerDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.PassengerRequestDTO;
import java.util.List;
import java.util.Map;

public interface PassengerService {
    PassengerDTO createPassenger(PassengerRequestDTO requestDTO);
    List<PassengerDTO> getPendingPassengersByTripId(long tripId);
    List<PassengerDTO> getPassengerTripsByUserId(long userId);
    Map<String, Object> removePassengerFromTrip(long usuarioId, long viajeId);
}