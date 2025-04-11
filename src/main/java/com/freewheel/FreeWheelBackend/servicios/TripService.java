package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.TripDTO;
import org.springframework.stereotype.Service;

@Service
public interface TripService {
    TripDTO createTrip(TripDTO tripDTO);
}
