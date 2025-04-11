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
    private String contraseña; // Considera llamar a este campo 'password' o similar en el DTO y no devolverlo en las respuestas
    private String fotoPerfil;
    private String organizacionCodigo; // Este es el campo que viene en el JSON como 'organizacionId' según tu request anterior, asegúrate que el mapeo sea correcto o renombra el campo JSON
}