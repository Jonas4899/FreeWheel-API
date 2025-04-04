package com.freewheel.FreeWheelBackend.controladores;

import com.freewheel.FreeWheelBackend.servicios.UserService;
import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/usuarios")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        try {
            UserDTO createdUser = userService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            logger.error("Error al crear usuario: ", e);
            throw e;
        }
    }
    
    /**
     * Create a new user with profile image
     * 
     * @param nombre User's first name
     * @param apellido User's last name
     * @param correo User's email
     * @param telefono User's phone number
     * @param contraseña User's password
     * @param organizacionId User's organization ID
     * @param profileImage User's profile image file (must be an image)
     * @return The created user with ID
     */
    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> createUserWithImage(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("correo") String correo,
            @RequestParam("telefono") String telefono,
            @RequestParam("contraseña") String contraseña,
            @RequestParam("organizacionId") Long organizacionId,
            @RequestParam(value = "profileImage", required = true) MultipartFile profileImage) {
        
        logger.info("Recibida solicitud para crear usuario con imagen de perfil. Email: {}, Nombre de archivo: {}, Tamaño: {}", 
                correo, profileImage.getOriginalFilename(), profileImage.getSize());
        
        try {
            // Validate image file
            if (profileImage.isEmpty()) {
                logger.warn("La imagen de perfil está vacía");
                return ResponseEntity.badRequest().build();
            }
            
            String contentType = profileImage.getContentType();
            logger.info("Tipo de contenido de la imagen: {}", contentType);
            
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.warn("El archivo no es una imagen válida: {}", contentType);
                return ResponseEntity.badRequest().build();
            }
            
            // Create user DTO from parameters
            UserDTO userDTO = UserDTO.builder()
                    .nombre(nombre)
                    .apellido(apellido)
                    .correo(correo)
                    .telefono(telefono)
                    .contraseña(contraseña)
                    .organizacionId(organizacionId)
                    .build();
            
            logger.info("DTO de usuario creado, procediendo a guardarlo con la imagen");
            
            // Call service method
            UserDTO createdUser = userService.createUserWithProfileImage(userDTO, profileImage);
            logger.info("Usuario creado exitosamente con ID: {}", createdUser.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IOException e) {
            logger.error("Error de I/O al procesar la imagen: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            logger.error("Error de runtime al crear usuario: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error inesperado al crear usuario con imagen: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
