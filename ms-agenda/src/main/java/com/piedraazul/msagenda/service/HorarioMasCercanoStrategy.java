package com.piedraazul.msagenda.service;

import com.piedraazul.msagenda.model.Cita;
import com.piedraazul.msagenda.model.Medico;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

// Segunda implementación del Strategy:
// sugiere el horario más cercano a la hora preferida del paciente
@Component("horarioCercano")
public class HorarioMasCercanoStrategy implements AgendamientoStrategy {

    private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN    = LocalTime.of(17, 0);
    private static final int INTERVALO_MINUTOS = 30;

    @Override
    public LocalDateTime sugerirHorario(Medico medico, List<Cita> citasExistentes) {
        // Sugiere el horario más cercano a las 10am del día siguiente hábil
        LocalDateTime candidato = LocalDateTime.now()
                .plusDays(1)
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        // Si es fin de semana avanza al lunes
        int diaSemana = candidato.getDayOfWeek().getValue();
        if (diaSemana == 6) candidato = candidato.plusDays(2);
        if (diaSemana == 7) candidato = candidato.plusDays(1);

        for (int intentos = 0; intentos < 100; intentos++) {
            final LocalDateTime slot = candidato;
            boolean ocupado = citasExistentes.stream()
                    .anyMatch(c -> Math.abs(
                            java.time.Duration.between(c.getFechaHora(), slot).toMinutes()
                    ) < INTERVALO_MINUTOS);

            if (!ocupado) return slot;
            candidato = candidato.plusMinutes(INTERVALO_MINUTOS);

            if (candidato.toLocalTime().isAfter(HORA_FIN)) {
                candidato = candidato.plusDays(1)
                        .withHour(HORA_INICIO.getHour())
                        .withMinute(0);
            }
        }

        throw new RuntimeException("No hay horarios disponibles para el médico: "
                + medico.getNombre());
    }
}