package com.piedraazul.msagenda.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

// ══════════════════════════════════════════════════════
// Cliente HTTP que se comunica con ms-pacientes
// para verificar que un paciente existe antes
// de agendar una cita
// ══════════════════════════════════════════════════════
@Slf4j
@Service
@RequiredArgsConstructor
public class PacienteClientService {

    private final RestTemplate restTemplate;

    private static final String MS_PACIENTES_URL = "http://localhost:8082";

    // Verifica si un paciente existe en ms-pacientes
    public boolean existePaciente(Long pacienteId) {
        try {
            String url = MS_PACIENTES_URL + "/api/pacientes/" + pacienteId;
            restTemplate.getForObject(url, Map.class);
            return true;
        } catch (Exception e) {
            log.warn("Paciente no encontrado en ms-pacientes: {}", pacienteId);
            return false;
        }
    }

    // Obtiene datos del paciente desde ms-pacientes
    public Map getPaciente(Long pacienteId) {
        try {
            String url = MS_PACIENTES_URL + "/api/pacientes/" + pacienteId;
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("Error al obtener paciente {}: {}", pacienteId, e.getMessage());
            return null;
        }
    }
}