package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.TripDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.TripSearchCriteriaDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TripService {
    TripDTO createTrip(TripDTO tripDTO);
    List<TripDTO> buscarViajesDisponibles(TripSearchCriteriaDTO criteria);
    List<TripDTO> obtenerViajesPorUsuario(Long userId, boolean esConductor);
}
