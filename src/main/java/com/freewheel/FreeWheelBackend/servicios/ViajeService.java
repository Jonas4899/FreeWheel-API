package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.ViajeDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.ViajeSearchCriteriaDTO;

import java.util.List;

public interface ViajeService {

    /**
     * Busca viajes disponibles según los criterios proporcionados.
     * Solo busca viajes en estado 'por iniciar'.
     *
     * @param criteria DTO con los criterios de búsqueda.
     * @return Lista de ViajeDTO que coinciden con los criterios.
     */
    List<ViajeDTO> buscarViajesDisponibles(ViajeSearchCriteriaDTO criteria);
}