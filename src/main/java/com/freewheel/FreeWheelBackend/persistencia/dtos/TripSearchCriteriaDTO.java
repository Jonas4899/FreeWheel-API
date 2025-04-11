package com.freewheel.FreeWheelBackend.persistencia.dtos;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TripSearchCriteriaDTO {

    // --- Criterios de Ubicación Geográfica ---
    private Double latitudOrigenBusqueda;    // Latitud del punto ORIGEN que busca el usuario
    private Double longitudOrigenBusqueda;   // Longitud del punto ORIGEN que busca el usuario

    private Double latitudDestinoBusqueda;   // Latitud del punto DESTINO que busca el usuario
    private Double longitudDestinoBusqueda;  // Longitud del punto DESTINO que busca el usuario

    // Radio de búsqueda en kilómetros (ajusta la unidad según prefieras)
    private Double radioBusquedaKm = 5.0; // Ejemplo: Default 5km

    // --- Otros Criterios ---
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate fecha;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaInicioDesde;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaInicioHasta;

    private Integer numeroAsientosRequeridos;

    // Considera añadir más criterios si son necesarios (ej. rango de precio)
}