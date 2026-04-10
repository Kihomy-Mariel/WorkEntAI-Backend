package com.workflow.backend.models;

import com.workflow.backend.enums.EstadoTarea;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tareas")
public class Tarea {

    @Id
    private String id;

    private String tramiteId;
    private String politicaId;
    private String nodoId;
    private String funcionarioId;
    private EstadoTarea estado = EstadoTarea.PENDIENTE;
    private Map<String, Object> formularioDatos;
    private String observacion;
    private LocalDateTime fechaAsignacion = LocalDateTime.now();
    private LocalDateTime fechaCompletado;
}