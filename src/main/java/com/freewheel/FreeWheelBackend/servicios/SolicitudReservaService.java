package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.SolicitudReservaDTO;

import java.util.List;

public interface SolicitudReservaService {
    /**
     * Obtiene el historial de solicitudes de reserva para un conductor específico
     * @param conductorId ID del conductor
     * @return Lista de solicitudes de reserva
     */
    List<SolicitudReservaDTO> obtenerHistorialSolicitudesConductor(Long conductorId);
}