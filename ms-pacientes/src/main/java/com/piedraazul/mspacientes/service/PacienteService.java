package com.piedraazul.mspacientes.service;

import com.piedraazul.mspacientes.dto.PacienteDTO;
import com.piedraazul.mspacientes.event.PacienteCreadoEvent;
import com.piedraazul.mspacientes.model.EstadoPaciente;
import com.piedraazul.mspacientes.model.Paciente;
import com.piedraazul.mspacientes.model.PacienteBuilder;
import com.piedraazul.mspacientes.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ── Registrar paciente usando el Builder (GoF) ──
    public Paciente registrar(PacienteDTO dto, String origen) {

        if (pacienteRepository.existsByNumeroDocumento(dto.getNumeroDocumento())) {
            throw new RuntimeException("Ya existe un paciente con ese documento: "
                    + dto.getNumeroDocumento());
        }
        if (pacienteRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Ya existe un paciente con ese email: "
                    + dto.getEmail());
        }

        // Patrón Builder — construye el paciente paso a paso
        Paciente paciente = new PacienteBuilder(
                dto.getNombre(),
                dto.getApellido(),
                dto.getNumeroDocumento(),
                dto.getFechaNacimiento(),
                dto.getEmail())
                .telefono(dto.getTelefono())
                .direccion(dto.getDireccion())
                .eps(dto.getEps())
                .build();

        Paciente guardado = pacienteRepository.save(paciente);

        // Patrón Observer — publica evento para ms-auditoria
        eventPublisher.publishEvent(new PacienteCreadoEvent(
                guardado.getId(),
                guardado.getNombre() + " " + guardado.getApellido(),
                guardado.getNumeroDocumento(),
                LocalDateTime.now(),
                origen
        ));

        return guardado;
    }

    // ── Buscar por documento ──
    public Paciente buscarPorDocumento(String documento) {
        return pacienteRepository.findByNumeroDocumento(documento)
                .orElseThrow(() -> new RuntimeException(
                        "Paciente no encontrado con documento: " + documento));
    }

    // ── Listar todos ──
    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    // ── Listar por estado ──
    public List<Paciente> listarPorEstado(String estado) {
        EstadoPaciente estadoEnum = EstadoPaciente.valueOf(estado.toUpperCase());
        return pacienteRepository.findByEstado(estadoEnum);
    }

    // ── Actualizar datos ──
    public Paciente actualizar(Long id, PacienteDTO dto) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Paciente no encontrado con id: " + id));

        paciente.setNombre(dto.getNombre());
        paciente.setApellido(dto.getApellido());
        paciente.setTelefono(dto.getTelefono());
        paciente.setDireccion(dto.getDireccion());
        paciente.setEps(dto.getEps());

        return pacienteRepository.save(paciente);
    }

    // ── Cambiar estado ──
    public Paciente cambiarEstado(Long id, String estado) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Paciente no encontrado con id: " + id));

        paciente.setEstado(EstadoPaciente.valueOf(estado.toUpperCase()));
        return pacienteRepository.save(paciente);
    }
}