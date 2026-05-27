package com.piedraazul.msagenda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// ══════════════════════════════════════════════════════
// Configuración del cliente HTTP para comunicación
// entre microservicios (ms-agenda → ms-pacientes)
// ══════════════════════════════════════════════════════
@Configuration
public class RestTemplateConfig {

    // PATRÓN GOF: SINGLETON
    // Spring garantiza una sola instancia del RestTemplate
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}