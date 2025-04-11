package com.freewheel.FreeWheelBackend.persistencia.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViajeDTO {

    private Long id;
    private Long conductorId; // Asegúrate que este campo exista en tu ViajeEntity o ajústalo
    private String lugarInicio;
    private String lugarDestino;
    private LocalDate fecha; // Considera usar String si el formato del request es fijo, o maneja la conversión
    private LocalTime horaInicio; // Considera usar String o maneja la conversión
    private LocalTime horaFin; // Considera usar String o maneja la conversión
    private BigDecimal precioAsiento;
    private Integer numeroAsientosDisponibles;
    private String estado;
}