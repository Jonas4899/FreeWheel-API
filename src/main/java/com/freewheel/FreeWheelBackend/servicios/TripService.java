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
    TripDTO iniciarViaje(Long tripId); // Nuevo metodo para iniciar un viaje
    TripDTO cancelarViajeConductor(Long tripId); // Nuevo metodo para cancelar un viaje un viaje
    TripDTO finalizarViaje(Long tripId);
    List<TripDTO> obtenerViajesPorConductorId(Long conductorId);
}
