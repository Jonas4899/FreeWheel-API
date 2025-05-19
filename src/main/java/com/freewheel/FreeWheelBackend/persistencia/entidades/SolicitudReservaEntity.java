package com.freewheel.FreeWheelBackend.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pasajeros_viaje")
public class SolicitudReservaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "viaje_id", nullable = false)
    private TripEntity viaje;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private UserEntity pasajero;

    @Column(name = "fecha_solicitud", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime fechaSolicitud;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "asientos_solicitados")
    private int asientosSolicitados;

    @Column(name = "fecha_respuesta")
    private ZonedDateTime fechaRespuesta;

    @Column(name = "mensaje_pasajero")
    private String mensajePasajero;

    @Column(name = "mensaje_conductor")
    private String mensajeConductor;
}
