package com.piedraazul.msagenda.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

// Cliente HTTP para notificar eventos a ms-auditoria
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaClientService {

    private final RestTemplate restTemplate;

    private static final String MS_AUDITORIA_URL =
            "http://localhost:8085/api/auditoria";

    public void registrarEvento(String tipoEvento,
                                String descripcion,
                                Long entidadId,
                                String realizadoPor) {
        try {
            Map<String, String> body = Map.of(
                    "tipoEvento", tipoEvento,
                    "descripcion", descripcion,
                    "entidadId", String.valueOf(entidadId),
                    "realizadoPor", realizadoPor,
                    "microservicioOrigen", "ms-agenda"
            );
            restTemplate.postForObject(MS_AUDITORIA_URL, body, Map.class);
        } catch (Exception e) {
            log.warn("No se pudo notificar a ms-auditoria: {}", e.getMessage());
        }
    }
}