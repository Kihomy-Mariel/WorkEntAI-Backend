package com.workflow.backend.repositories;

import com.workflow.backend.models.Tarea;
import com.workflow.backend.enums.EstadoTarea;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TareaRepository extends MongoRepository<Tarea, String> {
    List<Tarea> findByFuncionarioId(String funcionarioId);
    List<Tarea> findByTramiteId(String tramiteId);
    List<Tarea> findByFuncionarioIdAndEstado(String funcionarioId, EstadoTarea estado);
    List<Tarea> findByEstado(EstadoTarea estado);
}