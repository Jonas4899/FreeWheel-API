package com.freewheel.FreeWheelBackend.persistencia.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalificacionViajeDTO {

    private Long id;
    private Long viajeId;
    private Long pasajeroUsuarioId;
    private String pasajeroNombreCompleto;
    private Long conductorEvaluadoUsuarioId;
    private Short puntuacion;
    private String comentario;
    private ZonedDateTime fechaCalificacion;
}