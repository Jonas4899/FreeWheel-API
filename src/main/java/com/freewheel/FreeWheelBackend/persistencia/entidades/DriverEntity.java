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

    @Column(name = "usuario_id",unique = true)
    private long usuarioId;

    @Column(name = "licencia_conduccion")
    private String licenciaConduccion;
}


