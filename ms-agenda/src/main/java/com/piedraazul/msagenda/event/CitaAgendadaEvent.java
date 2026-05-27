package com.piedraazul.msagenda.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

// ══════════════════════════════════════════════════════
// PATRÓN GOF: OBSERVER
// Este evento notifica a ms-auditoria cada vez que
// se agenda una cita en el sistema.
// ══════════════════════════════════════════════════════
@Data
@AllArgsConstructor
public class CitaAgendadaEvent {

    private Long citaId;
    private Long pacienteId;
    private Long medicoId;
    private String nombreMedico;
    private LocalDateTime fechaHora;
    private String estrategiaUsada;
    private LocalDateTime fechaEvento;
}