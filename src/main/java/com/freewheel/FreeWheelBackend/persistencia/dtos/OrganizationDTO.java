package com.freewheel.FreeWheelBackend.persistencia.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDTO {
    private Long id;
    private String nombre;
    private String codigo;
    private String direccion;
    private String telefono;
    private String nit;
}