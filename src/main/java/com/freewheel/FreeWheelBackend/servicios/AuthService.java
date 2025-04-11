package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.AuthRequestDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO login(AuthRequestDTO authRequest);
}
