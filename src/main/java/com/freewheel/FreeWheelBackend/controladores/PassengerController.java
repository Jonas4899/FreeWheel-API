package com.freewheel.FreeWheelBackend.controladores;

import com.freewheel.FreeWheelBackend.persistencia.dtos.PassengerDTO;
import com.freewheel.FreeWheelBackend.persistencia.dtos.PassengerRequestDTO;
import com.freewheel.FreeWheelBackend.servicios.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pasajeros")
public class PassengerController {

    private final PassengerService passengerService;

    @Autowired
    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @PostMapping("/crear")
    public ResponseEntity<PassengerDTO> createPassenger(@RequestBody PassengerRequestDTO requestDTO) {
        PassengerDTO savedPassenger = passengerService.createPassenger(requestDTO);
        return new ResponseEntity<>(savedPassenger, HttpStatus.CREATED);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));

        // Determinar el código de estado según el mensaje de error
        HttpStatus status = ex.getMessage().contains("no encontrado") ?
                HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(response, status);
    }
}