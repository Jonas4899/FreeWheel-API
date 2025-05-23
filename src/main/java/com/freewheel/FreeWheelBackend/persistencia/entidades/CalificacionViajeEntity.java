package com.freewheel.FreeWheelBackend.persistencia.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "calificaciones_viaje", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"viaje_id", "pasajero_usuario_id"})
})
public class CalificacionViajeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    private TripEntity viaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pasajero_usuario_id", nullable = false)
    private UserEntity pasajeroUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conductor_evaluado_usuario_id", nullable = false)
    private UserEntity conductorEvaluadoUsuario;

    @Column(name = "puntuacion", nullable = false)
    private Short puntuacion;

    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @CreationTimestamp
    @Column(name = "fecha_calificacion", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime fechaCalificacion;
}