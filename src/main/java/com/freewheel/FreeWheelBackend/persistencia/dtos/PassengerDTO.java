package com.freewheel.FreeWheelBackend.persistencia.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PassengerDTO {
    private long id;
    private TripDTO viaje;
    private UserDTO pasajero;
    private boolean pagoRealizado;
    private String estado;
}
