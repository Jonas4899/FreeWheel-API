// src/main/java/com/freewheel/FreeWheelBackend/persistencia/dtos/PassengerRequestDTO.java
package com.freewheel.FreeWheelBackend.persistencia.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerRequestDTO {
    private long usuarioId;
    private long viajeId;
}