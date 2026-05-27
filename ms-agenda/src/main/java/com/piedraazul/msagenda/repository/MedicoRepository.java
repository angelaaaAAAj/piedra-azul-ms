package com.piedraazul.msagenda.repository;

import com.piedraazul.msagenda.model.Medico;
import com.piedraazul.msagenda.model.TipoEspecialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    List<Medico> findByDisponibleTrue();

    List<Medico> findByEspecialidad(TipoEspecialidad especialidad);

    List<Medico> findByEspecialidadAndDisponibleTrue(TipoEspecialidad especialidad);
}
