package com.freewheel.FreeWheelBackend.servicios;

import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);

    /**
     * Create a user with profile image
     * @param userDTO the user data
     * @param profileImage the profile image file
     * @return the created user
     * @throws IOException if the image cannot be uploaded
     */
    UserDTO createUserWithProfileImage(UserDTO userDTO, MultipartFile profileImage) throws IOException;
    
    /**
     * Get user by ID
     * @param id the user ID
     * @return the user data if found, null otherwise
     */
    UserDTO getUserById(long id);
}
