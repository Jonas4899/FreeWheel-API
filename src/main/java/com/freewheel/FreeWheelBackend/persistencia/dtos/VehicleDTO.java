package com.freewheel.FreeWheelBackend.persistencia.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {
    private long id;
    private String placa;
    private String marca;
    private String modelo;
    private int anio;
    private String color;
    private String tipo;
    private int capacidadPasajeros;
    private String licenciaTransito;
    private String soat;
    private String certificadoRevision;
    private long conductorId;
    private String foto;
}
