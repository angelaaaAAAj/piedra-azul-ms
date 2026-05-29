package com.piedraazul.ui.agenda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Agenda {

    private Long id;
    private Long pacienteId;
    private Medico medico;
    private String fechaHora;
    private String motivo;
    private String estado;
    private String observaciones;

    public Agenda() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getNombreMedico() {
        return medico != null ? medico.getNombre() + " " + medico.getApellido() : "-";
    }

    // Clase interna para deserializar el médico anidado
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Medico {
        private Long id;
        private String nombre;
        private String apellido;
        private String especialidad;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public String getEspecialidad() { return especialidad; }
        public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

        @Override
        public String toString() {
            return nombre + " " + apellido + " [" + especialidad + "]";
        }
    }
}