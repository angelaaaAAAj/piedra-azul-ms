package com.piedraazul.mspacientes.controller;

import com.piedraazul.mspacientes.dto.PacienteDTO;
import com.piedraazul.mspacientes.model.Paciente;
import com.piedraazul.mspacientes.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    // ── POST /api/pacientes/registro ──
    // Registro autónomo por el paciente (HU-09)
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody PacienteDTO dto) {
        try {
            Paciente paciente = pacienteService.registrar(dto, "AUTONOMO");
            return ResponseEntity.status(HttpStatus.CREATED).body(paciente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── POST /api/pacientes/registro/recepcionista ──
    // Registro por recepcionista (HU-02)
    @PostMapping("/registro/recepcionista")
    public ResponseEntity<?> registrarPorRecepcionista(@Valid @RequestBody PacienteDTO dto) {
        try {
            Paciente paciente = pacienteService.registrar(dto, "RECEPCIONISTA");
            return ResponseEntity.status(HttpStatus.CREATED).body(paciente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/pacientes ──
    // Lista todos los pacientes
    @GetMapping
    public ResponseEntity<List<Paciente>> listarTodos() {
        return ResponseEntity.ok(pacienteService.listarTodos());
    }

    // ── GET /api/pacientes/documento/{documento} ──
    // Busca un paciente por número de documento
    @GetMapping("/documento/{documento}")
    public ResponseEntity<?> buscarPorDocumento(@PathVariable String documento) {
        try {
            return ResponseEntity.ok(pacienteService.buscarPorDocumento(documento));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/pacientes/estado/{estado} ──
    // Lista pacientes por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> listarPorEstado(@PathVariable String estado) {
        try {
            return ResponseEntity.ok(pacienteService.listarPorEstado(estado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Estado no válido: " + estado));
        }
    }

    // ── PUT /api/pacientes/{id} ──
    // Actualiza datos del paciente
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody PacienteDTO dto) {
        try {
            return ResponseEntity.ok(pacienteService.actualizar(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── PATCH /api/pacientes/{id}/estado ──
    // Cambia el estado del paciente
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                           @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(
                    pacienteService.cambiarEstado(id, body.get("estado")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    // ── GET /api/pacientes/{id} ──
    // Busca paciente por ID (usado por ms-agenda)
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return pacienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}