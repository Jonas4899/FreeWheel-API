package com.freewheel.FreeWheelBackend.persistencia.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudReservaDTO {
    private long id;
    private long viajeId;
    private TripDTO viaje;
    private long pasajeroId;
    private UserDTO pasajero;
    private ZonedDateTime fechaSolicitud;
    private String estado;
    private ZonedDateTime fechaRespuesta;
    private String mensajePasajero;
    private String mensajeConductor;
}