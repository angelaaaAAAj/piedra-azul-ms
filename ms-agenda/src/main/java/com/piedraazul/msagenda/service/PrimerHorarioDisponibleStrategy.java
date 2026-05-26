package com.piedraazul.msagenda.service;

import com.piedraazul.msagenda.model.Cita;
import com.piedraazul.msagenda.model.Medico;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

// Implementación concreta del Strategy:
// sugiere el primer horario disponible desde hoy
@Component("primerDisponible")
public class PrimerHorarioDisponibleStrategy implements AgendamientoStrategy {

    // Horario de atención: lunes a viernes 8am - 5pm
    private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN    = LocalTime.of(17, 0);
    private static final int INTERVALO_MINUTOS = 30;

    @Override
    public LocalDateTime sugerirHorario(Medico medico, List<Cita> citasExistentes) {
        LocalDateTime candidato = LocalDateTime.now()
                .plusHours(1)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        // Busca el primer slot libre
        for (int intentos = 0; intentos < 200; intentos++) {

            // Solo lunes a viernes (1=lunes, 5=viernes)
            int diaSemana = candidato.getDayOfWeek().getValue();
            if (diaSemana > 5) {
                candidato = candidato.plusDays(1)
                        .withHour(HORA_INICIO.getHour())
                        .withMinute(0);
                continue;
            }

            // Dentro del horario de atención
            if (candidato.toLocalTime().isBefore(HORA_INICIO)) {
                candidato = candidato.withHour(HORA_INICIO.getHour()).withMinute(0);
            }
            if (candidato.toLocalTime().isAfter(HORA_FIN.minusMinutes(INTERVALO_MINUTOS))) {
                candidato = candidato.plusDays(1)
                        .withHour(HORA_INICIO.getHour())
                        .withMinute(0);
                continue;
            }

            // Verifica que no haya cita en ese slot
            final LocalDateTime slot = candidato;
            boolean ocupado = citasExistentes.stream()
                    .anyMatch(c -> Math.abs(
                            java.time.Duration.between(c.getFechaHora(), slot).toMinutes()
                    ) < INTERVALO_MINUTOS);

            if (!ocupado) return slot;

            candidato = candidato.plusMinutes(INTERVALO_MINUTOS);
        }

        throw new RuntimeException("No hay horarios disponibles para el médico: "
                + medico.getNombre());
    }
}