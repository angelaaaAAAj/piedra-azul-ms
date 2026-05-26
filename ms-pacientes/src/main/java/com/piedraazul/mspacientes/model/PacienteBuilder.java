package com.piedraazul.mspacientes.model;

import java.time.LocalDate;

// ══════════════════════════════════════════════════════
// PATRÓN GOF: BUILDER
// Propósito: construir un objeto Paciente paso a paso,
// permitiendo campos opcionales sin constructores enormes.
// HU-09 — Registro autónomo del paciente
// ══════════════════════════════════════════════════════
public class PacienteBuilder {

    // Campos obligatorios
    private final String nombre;
    private final String apellido;
    private final String numeroDocumento;
    private final LocalDate fechaNacimiento;
    private final String email;

    // Campos opcionales
    private String telefono;
    private String direccion;
    private String eps;
    private EstadoPaciente estado = EstadoPaciente.ACTIVO;

    // Constructor con campos obligatorios
    public PacienteBuilder(String nombre,
                           String apellido,
                           String numeroDocumento,
                           LocalDate fechaNacimiento,
                           String email) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.numeroDocumento = numeroDocumento;
        this.fechaNacimiento = fechaNacimiento;
        this.email = email;
    }

    // Métodos encadenables para campos opcionales
    public PacienteBuilder telefono(String telefono) {
        this.telefono = telefono;
        return this;
    }

    public PacienteBuilder direccion(String direccion) {
        this.direccion = direccion;
        return this;
    }

    public PacienteBuilder eps(String eps) {
        this.eps = eps;
        return this;
    }

    public PacienteBuilder estado(EstadoPaciente estado) {
        this.estado = estado;
        return this;
    }

    // Construye el objeto final
    public Paciente build() {
        Paciente paciente = new Paciente();
        paciente.setNombre(this.nombre);
        paciente.setApellido(this.apellido);
        paciente.setNumeroDocumento(this.numeroDocumento);
        paciente.setFechaNacimiento(this.fechaNacimiento);
        paciente.setEmail(this.email);
        paciente.setTelefono(this.telefono);
        paciente.setDireccion(this.direccion);
        paciente.setEps(this.eps);
        paciente.setEstado(this.estado);
        return paciente;
    }
}