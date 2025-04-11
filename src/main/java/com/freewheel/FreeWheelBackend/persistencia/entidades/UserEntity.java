package com.freewheel.FreeWheelBackend.persistencia.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios") // Asegúrate que la tabla se llame 'usuarios' en tu BD
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre",nullable = false)
    private String nombre;

    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Column(name = "correo", nullable = false, unique = true)
    private String correo;

    @Column(name = "telefono",nullable = false)
    private String telefono;

    @Column(name = "contraseña", nullable = false) // Considera renombrar a 'password' o 'hash_contrasena'
    private String contraseña;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @ManyToOne(fetch = FetchType.LAZY) // Usar LAZY es generalmente buena práctica
    @JoinColumn(name = "organizacion_id", nullable = false) // Asegúrate que la FK se llame 'organizacion_id'
    private OrganizationEntity organizacion;
}