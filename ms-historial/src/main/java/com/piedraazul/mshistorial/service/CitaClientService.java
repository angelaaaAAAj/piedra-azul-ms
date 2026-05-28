package com.piedraazul.mshistorial.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

// ══════════════════════════════════════════════════════
// Cliente HTTP que se comunica con ms-agenda
// para verificar que una cita existe antes de
// registrar en el historial clínico
// ══════════════════════════════════════════════════════
@Slf4j
@Service
@RequiredArgsConstructor
public class CitaClientService {

    private final RestTemplate restTemplate;

    private static final String MS_AGENDA_URL = "http://localhost:8083";

    public boolean existeCita(Long citaId) {
        try {
            String url = MS_AGENDA_URL + "/api/citas/" + citaId;
            restTemplate.getForObject(url, Map.class);
            return true;
        } catch (Exception e) {
            log.warn("Cita no encontrada en ms-agenda: {}", citaId);
            return false;
        }
    }
}