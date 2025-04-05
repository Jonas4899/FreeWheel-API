package com.freewheel.FreeWheelBackend.persistencia.entidades;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conductores")
public class DriverEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name="usuario_id", referencedColumnName = "id")
    private UserEntity usuario;

    @Column(name = "licencia_conduccion")
    private String licenciaConduccion;
}


