package com.freewheel.FreeWheelBackend.persistencia.entidades;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehiculos")

public class VehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="placa",unique = true)
    private String placa;

    @Column(name="marca")
    private String marca;

    @Column(name="modelo")
    private String modelo;

    @Column(name="anio")
    private int anio;

    @Column(name="color")
    private String color;

    @Column(name="tipo")
    private String tipo;

    @Column(name="capacidad_pasajeros")
    private int capacidadPasajeros;

    @Column(name="licencia_transito")
    private String licenciaTransito;

    @Column(name="soat")
    private String soat;

    @Column(name="certificado_revision")
    private String certificadoRevision;

    @ManyToOne
    @JoinColumn(name="conductor_id", nullable = false)
    private DriverEntity conductor;

    @Column(name="foto")
    private String foto;
}
