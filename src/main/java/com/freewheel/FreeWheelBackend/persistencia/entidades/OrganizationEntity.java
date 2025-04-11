package com.freewheel.FreeWheelBackend.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "organizaciones") // Asegúrate que la tabla se llame 'organizaciones'
public class OrganizationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    // Este es el campo importante para la búsqueda por código
    @Column(name = "codigo", unique = true) // Mapea a la columna 'codigo' que debe ser única
    private String codigo;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "nit", unique = true)
    private String nit;
}