package com.freewheel.FreeWheelBackend.persistencia.dtos;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class TripDTO {
    private long id;
    private long conductorId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int precioAsiento;
    private int asientosDisponibles;

    //datos origen -----------------

    private String direccionOrigen;
    private double latitudOrigen;
    private double longitudOrigen;

    //datos destino -----------------

    private String direccionDestino;
    private double latitudDestino;
    private double longitudDestino;

    private String estado;
}
