package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.PassengerDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.PassengerRequestDTO;
import java.util.List;

public interface PassengerService {
    PassengerDTO createPassenger(PassengerRequestDTO requestDTO);
    List<PassengerDTO> getPendingPassengersByTripId(long tripId);
}