package com.freewheel.FreeWheelBackend.persistencia.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private String fotoPerfil;
    private Long organizacionId;
}
