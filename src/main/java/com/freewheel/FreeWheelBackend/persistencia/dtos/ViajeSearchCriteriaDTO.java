package com.freewheel.FreeWheelBackend.persistencia.dtos;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
public class ViajeSearchCriteriaDTO {

    private String lugarInicio;
    private String lugarDestino;

    // Define el patrón esperado para la fecha en la URL (ej: ?fecha=15/05/2024)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate fecha;

    // Define el patrón esperado para la hora en la URL (ej: ?horaInicioDesde=08:00)
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaInicioDesde;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaInicioHasta;

    private Integer numeroAsientosRequeridos;

    // Podrías añadir validaciones aquí usando anotaciones de Jakarta Bean Validation
    // ej: @Min(1) para numeroAsientosRequeridos
}