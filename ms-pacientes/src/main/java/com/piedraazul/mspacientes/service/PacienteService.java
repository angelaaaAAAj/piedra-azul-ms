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
import com.piedraazul.mspacientes.model.PacienteFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Paciente registrar(PacienteDTO dto, String origen) {

        if (pacienteRepository.existsByNumeroDocumento(dto.getNumeroDocumento())) {
            throw new RuntimeException("Ya existe un paciente con ese documento: "
                    + dto.getNumeroDocumento());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && pacienteRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Ya existe un paciente con ese email: "
                    + dto.getEmail());
        }

        Paciente paciente = PacienteFactory.crearDesdeDTO(dto);

        Paciente guardado = pacienteRepository.save(paciente);

        eventPublisher.publishEvent(new PacienteCreadoEvent(
                guardado.getId(),
                guardado.getNombre() + " " + guardado.getApellido(),
                guardado.getNumeroDocumento(),
                LocalDateTime.now(),
                origen
        ));

        return guardado;
    }

    public Paciente buscarPorDocumento(String documento) {
        return pacienteRepository.findByNumeroDocumento(documento)
                .orElseThrow(() -> new RuntimeException(
                        "Paciente no encontrado con documento: " + documento));
    }

    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    public List<Paciente> listarPorEstado(String estado) {
        EstadoPaciente estadoEnum = EstadoPaciente.valueOf(estado.toUpperCase());
        return pacienteRepository.findByEstado(estadoEnum);
    }

    public Paciente actualizar(Long id, PacienteDTO dto) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Paciente no encontrado con id: " + id));

        if (!paciente.getNumeroDocumento().equals(dto.getNumeroDocumento())
                && pacienteRepository.existsByNumeroDocumento(dto.getNumeroDocumento())) {
            throw new RuntimeException("Ya existe un paciente con ese documento: "
                    + dto.getNumeroDocumento());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && paciente.getEmail() != null
                && !paciente.getEmail().equals(dto.getEmail())
                && pacienteRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Ya existe un paciente con ese email: "
                    + dto.getEmail());
        }

        paciente.setNombre(dto.getNombre());
        paciente.setApellido(dto.getApellido());
        paciente.setNumeroDocumento(dto.getNumeroDocumento());
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setEmail(dto.getEmail());
        paciente.setTelefono(dto.getTelefono());
        paciente.setGenero(dto.getGenero());
        paciente.setDireccion(dto.getDireccion());
        paciente.setEps(dto.getEps());

        if (dto.getEstado() != null) {
            paciente.setEstado(dto.getEstado());
        }

        return pacienteRepository.save(paciente);
    }

    public Paciente cambiarEstado(Long id, String estado) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Paciente no encontrado con id: " + id));

        paciente.setEstado(EstadoPaciente.valueOf(estado.toUpperCase()));
        return pacienteRepository.save(paciente);
    }

    public Optional<Paciente> buscarPorId(Long id) {
        return pacienteRepository.findById(id);
    }
}