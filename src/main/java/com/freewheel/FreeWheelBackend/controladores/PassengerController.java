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
import java.util.List;

@RestController
@RequestMapping("/pasajeros")
public class PassengerController {

    private final PassengerService passengerService;

    @Autowired
    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    //crear pasajeros
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

    //Obtener los pasajeros pendientes de un viaje
    @GetMapping("/viaje/{viajeId}")
    public ResponseEntity<?> getPendingPassengersByTripId(@PathVariable long viajeId) {
        List<PassengerDTO> pendingPassengers = passengerService.getPendingPassengersByTripId(viajeId);

        if (pendingPassengers.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "No hay pasajeros pendientes para este viaje");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(pendingPassengers);
    }

    //Obtener los viajes usando usuario_id
    @GetMapping("/viajes-usuario/{usuarioId}")
    public ResponseEntity<?> getPassengerTripsByUserId(@PathVariable long usuarioId) {
        List<PassengerDTO> passengerTrips = passengerService.getPassengerTripsByUserId(usuarioId);

        if (passengerTrips.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "No se encontraron viajes iniciados o por iniciar para este usuario");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(passengerTrips);
    }

    @DeleteMapping("/eliminar")
    public ResponseEntity<?> removePassenger(
            @RequestParam("usuarioId") long usuarioId,
            @RequestParam("viajeId") long viajeId) {

        try {
            Map<String, Object> resultado = passengerService.removePassengerFromTrip(usuarioId, viajeId);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException ex) {
            Map<String, String> response = new HashMap<>();
            response.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception ex) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error al eliminar al pasajero: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}