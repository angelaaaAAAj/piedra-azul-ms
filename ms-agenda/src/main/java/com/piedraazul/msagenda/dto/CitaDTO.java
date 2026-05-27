package com.piedraazul.msagenda.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CitaDTO {

    @NotNull(message = "El paciente es obligatorio")
    private Long pacienteId;

    @NotNull(message = "El médico es obligatorio")
    private Long medicoId;

    private String motivo;
    private String observaciones;

    // Si el paciente quiere escoger horario manualmente
    private String fechaHoraManual; // formato: "2026-06-01T10:00"

    // Estrategia de agendamiento: "primerDisponible" o "horarioCercano"
    private String estrategia = "primerDisponible";
}