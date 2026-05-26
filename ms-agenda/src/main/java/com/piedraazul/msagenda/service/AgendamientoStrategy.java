package com.piedraazul.msagenda.service;

import com.piedraazul.msagenda.model.Cita;
import com.piedraazul.msagenda.model.Medico;

import java.time.LocalDateTime;
import java.util.List;

// ══════════════════════════════════════════════════════
// PATRÓN GOF: STRATEGY
// Propósito: definir una familia de algoritmos para
// sugerir horarios de citas, encapsularlos e
// intercambiarlos sin modificar el Service.
// HU-10 — Agendamiento autónomo con sugerencia de horario
// ══════════════════════════════════════════════════════
public interface AgendamientoStrategy {
    LocalDateTime sugerirHorario(Medico medico, List<Cita> citasExistentes);
}