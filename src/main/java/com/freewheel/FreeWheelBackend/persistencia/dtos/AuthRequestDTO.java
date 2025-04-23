package com.freewheel.FreeWheelBackend.persistencia.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequestDTO {
    private String correo;
    private String password;
}
