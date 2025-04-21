package com.freewheel.FreeWheelBackend.controladores;

import com.freewheel.FreeWheelBackend.persistencia.dtos.AuthRequestDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.AuthResponseDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import com.freewheel.FreeWheelBackend.servicios.AuthService;
import com.freewheel.FreeWheelBackend.servicios.UserService;
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
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        try {
            UserDTO createdUser = userService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            logger.error("Error al crear usuario: ", e);
            // Considera devolver un ResponseEntity de error más específico
            // en lugar de relanzar la excepción directamente si quieres
            // manejarlo de forma centralizada con @ControllerAdvice
            throw e;
        }
    }

    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> createUserWithImage(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("correo") String correo,
            @RequestParam("telefono") String telefono,
            @RequestParam("contraseña") String contraseña,
            @RequestParam("organizacionCodigo") String organizacionCodigo,
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
                    .organizacionCodigo(organizacionCodigo)
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

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO authRequest) {
        return ResponseEntity.ok(authService.login(authRequest));
    }
}
