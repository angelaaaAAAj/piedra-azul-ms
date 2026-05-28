package com.piedraazul.mspacientes;

import com.piedraazul.mspacientes.model.EstadoPaciente;
import com.piedraazul.mspacientes.model.Paciente;
import com.piedraazul.mspacientes.model.PacienteBuilder;
import com.piedraazul.mspacientes.repository.PacienteRepository;
import com.piedraazul.mspacientes.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PacienteService pacienteService;

    // ── Test 1: Builder construye paciente correctamente ──
    @Test
    void testBuilder_ConstruyePacienteConCamposObligatorios() {
        Paciente paciente = new PacienteBuilder(
                "Ana", "Lopez", "999888",
                LocalDate.of(1990, 5, 15), "ana@email.com")
                .build();

        assertNotNull(paciente);
        assertEquals("Ana", paciente.getNombre());
        assertEquals("Lopez", paciente.getApellido());
        assertEquals("999888", paciente.getNumeroDocumento());
        assertEquals(EstadoPaciente.ACTIVO, paciente.getEstado());
    }

    // ── Test 2: Builder con campos opcionales ──
    @Test
    void testBuilder_ConstruyePacienteConCamposOpcionales() {
        Paciente paciente = new PacienteBuilder(
                "Ana", "Lopez", "999888",
                LocalDate.of(1990, 5, 15), "ana@email.com")
                .telefono("3001234567")
                .eps("Sura")
                .direccion("Calle 10 # 5-20")
                .build();

        assertNotNull(paciente);
        assertEquals("3001234567", paciente.getTelefono());
        assertEquals("Sura", paciente.getEps());
        assertEquals("Calle 10 # 5-20", paciente.getDireccion());
    }

    // ── Test 3: Registro falla si documento ya existe ──
    @Test
    void testRegistrar_DocumentoDuplicado_LanzaExcepcion() {
        when(pacienteRepository.existsByNumeroDocumento("999888"))
                .thenReturn(true);

        var dto = new com.piedraazul.mspacientes.dto.PacienteDTO();
        dto.setNombre("Ana");
        dto.setApellido("Lopez");
        dto.setNumeroDocumento("999888");
        dto.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        dto.setEmail("ana@email.com");

        assertThrows(RuntimeException.class, () ->
                pacienteService.registrar(dto, "AUTONOMO"));
    }

    // ── Test 4: Registro exitoso guarda en repositorio ──
    @Test
    void testRegistrar_Exitoso_GuardaEnRepositorio() {
        when(pacienteRepository.existsByNumeroDocumento(any())).thenReturn(false);
        when(pacienteRepository.existsByEmail(any())).thenReturn(false);

        Paciente pacienteMock = new Paciente();
        pacienteMock.setId(1L);
        pacienteMock.setNombre("Ana");
        pacienteMock.setEstado(EstadoPaciente.ACTIVO);
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(pacienteMock);

        var dto = new com.piedraazul.mspacientes.dto.PacienteDTO();
        dto.setNombre("Ana");
        dto.setApellido("Lopez");
        dto.setNumeroDocumento("999888");
        dto.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        dto.setEmail("ana@email.com");

        Paciente resultado = pacienteService.registrar(dto, "AUTONOMO");

        assertNotNull(resultado);
        assertEquals("Ana", resultado.getNombre());
        verify(pacienteRepository, times(1)).save(any(Paciente.class));
    }
}
