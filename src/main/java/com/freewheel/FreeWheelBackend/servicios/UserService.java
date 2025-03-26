package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
}
