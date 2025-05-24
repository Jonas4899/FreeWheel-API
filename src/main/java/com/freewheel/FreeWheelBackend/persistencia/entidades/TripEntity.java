package com.freewheel.FreeWheelBackend.persistencia.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "viajes")

public class TripEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "conductor_id")
    private long conductorId;

    @Column(name="fecha")
    private LocalDate fecha;

    @Column(name="hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "precio_asiento")
    private int precioAsiento;

    @Column(name = "asientos_disponibles")
    private int asientosDisponibles;

    //datos origen -----------------

    @Column(name="direccion_origen")
    private String direccionOrigen;

    @Column(name="latitud_origen")
    private double latitudOrigen;

    @Column(name="longitud_origen")
    private double longitudOrigen;

    //datos destino -----------------

    @Column(name="direccion_destino")
    private String direccionDestino;

    @Column(name="latitud_destino")
    private double latitudDestino;

    @Column(name="longitud_destino")
    private double longitudDestino;

    @Column(name="estado")
    private String estado;
}
