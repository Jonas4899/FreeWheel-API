package com.freewheel.FreeWheelBackend.persistencia.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pasajeros_viaje")
public class PassengerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name="usuario_id", referencedColumnName = "id")
    private UserEntity usuario;

    @Column(name="viaje_id")
    private long viajeId;

    @Column(name="pago_realizado")
    private boolean pagoRealizado;

    @Column(name="estado")
    private String estado;
}
