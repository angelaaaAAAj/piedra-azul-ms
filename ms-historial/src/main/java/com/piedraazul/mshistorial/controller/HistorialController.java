package com.piedraazul.mshistorial.controller;

import com.piedraazul.mshistorial.dto.HistorialDTO;
import com.piedraazul.mshistorial.model.CambioAgenda;
import com.piedraazul.mshistorial.model.HistorialClinico;
import com.piedraazul.mshistorial.service.HistorialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/historial")
@RequiredArgsConstructor
public class HistorialController {

    private final HistorialService historialService;

    // -- POST /api/historial --
    // Registra una entrada en el historial clínico
    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody HistorialDTO dto) {
        try {
            HistorialClinico historial = historialService.registrar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(historial);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // -- POST /api/historial/reagendamiento --
    // Registra un cambio de agenda con Memento (HU-04b)
    @PostMapping("/reagendamiento")
    public ResponseEntity<?> registrarReagendamiento(
            @RequestBody Map<String, String> body) {
        try {
            CambioAgenda cambio = historialService.registrarReagendamiento(
                    Long.parseLong(body.get("citaId")),
                    Long.parseLong(body.get("pacienteId")),
                    Long.parseLong(body.get("medicoId")),
                    LocalDateTime.parse(body.get("fechaAnterior")),
                    LocalDateTime.parse(body.get("fechaNueva")),
                    body.get("motivoCambio"),
                    body.get("cambiadoPor")
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(cambio);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // -- GET /api/historial/paciente/{pacienteId} --
    // Consulta historial clínico de un paciente
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<HistorialClinico>> listarPorPaciente(
            @PathVariable Long pacienteId) {
        return ResponseEntity.ok(historialService.listarPorPaciente(pacienteId));
    }

    // -- GET /api/historial/cita/{citaId} --
    // Consulta historial de una cita específica
    @GetMapping("/cita/{citaId}")
    public ResponseEntity<List<HistorialClinico>> listarPorCita(
            @PathVariable Long citaId) {
        return ResponseEntity.ok(historialService.listarPorCita(citaId));
    }

    // -- GET /api/historial/cambios/cita/{citaId} --
    // Consulta cambios de agenda de una cita (Memento)
    @GetMapping("/cambios/cita/{citaId}")
    public ResponseEntity<List<CambioAgenda>> listarCambiosPorCita(
            @PathVariable Long citaId) {
        return ResponseEntity.ok(historialService.listarCambiosPorCita(citaId));
    }

    // -- GET /api/historial/cambios/paciente/{pacienteId} --
    // Consulta todos los reagendamientos de un paciente
    @GetMapping("/cambios/paciente/{pacienteId}")
    public ResponseEntity<List<CambioAgenda>> listarCambiosPorPaciente(
            @PathVariable Long pacienteId) {
        return ResponseEntity.ok(
                historialService.listarCambiosPorPaciente(pacienteId));
    }
}