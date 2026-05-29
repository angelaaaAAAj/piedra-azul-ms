package com.piedraazul.ui.agenda;

public class Agenda {

    private Long id;
    private String paciente;
    private String fecha;
    private String hora;
    private String estado;

    public Agenda() {}

    public Agenda(Long id, String paciente, String fecha, String hora, String estado) {
        this.id = id;
        this.paciente = paciente;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPaciente() { return paciente; }
    public void setPaciente(String paciente) { this.paciente = paciente; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}