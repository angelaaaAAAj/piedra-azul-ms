package com.piedraazul.msagenda.repository;

import com.piedraazul.msagenda.model.Cita;
import com.piedraazul.msagenda.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByMedicoId(Long medicoId);

    List<Cita> findByPacienteId(Long pacienteId);

    List<Cita> findByEstado(EstadoCita estado);

    List<Cita> findByMedicoIdAndEstadoNot(Long medicoId, EstadoCita estado);

    List<Cita> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    boolean existsByMedicoIdAndFechaHora(Long medicoId, LocalDateTime fechaHora);
}