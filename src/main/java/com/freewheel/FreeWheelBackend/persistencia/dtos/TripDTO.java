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

    // Información del conductor para la interfaz
    private String nombreConductor;
    private String apellidoConductor;
    private String fotoConductor;
    private String telefonoConductor;
    private Double calificacionConductor; // Promedio de calificaciones

    // Información del vehículo para la interfaz
    private String vehiculoPlaca;
    private String vehiculoMarca;
    private String vehiculoModelo;
    private String vehiculoColor;
    private String vehiculoTipo;
    private String vehiculoFoto;
}
