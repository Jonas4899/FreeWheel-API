package com.freewheel.FreeWheelBackend.persistencia.entidades;

import jakarta.persistence.*;
import lombok.Data; // O @Getter, @Setter, etc. si prefieres ser más explícito

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "viaje") // Asegúrate que la tabla se llame 'viaje'
@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
public class ViajeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Cambiar a relación ManyToOne con ConductorEntity cuando se haga merge
    // Temporalmente como Long. Asegúrate que la columna se llame 'conductor_id'
    @Column(name = "conductor_id", nullable = false)
    private Long conductorId;

    @Column(name = "lugar_inicio", nullable = false)
    private String lugarInicio;

    @Column(name = "lugar_destino", nullable = false)
    private String lugarDestino;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "precio_asiento", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioAsiento;

    @Column(name = "numero_asientos_disponibles", nullable = false)
    private Integer numeroAsientosDisponibles;

    // Podrías usar un Enum para el estado si los valores son fijos
    @Column(name = "estado", nullable = false)
    private String estado; // Ej: "por iniciar", "en curso", "finalizado"
}