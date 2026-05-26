package com.piedraazul.mspacientes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "pacientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @NotBlank
    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String numeroDocumento;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String telefono;

    @Column
    private String direccion;

    @Column
    private String eps;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPaciente estado = EstadoPaciente.ACTIVO;
}
