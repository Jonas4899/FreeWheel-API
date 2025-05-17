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

    /**
     * Acepta una solicitud de reserva
     * @param id ID de la solicitud
     * @return Mensaje de éxito o error
     */
    boolean aceptarSolicitudReserva(Long id);
}