package com.workflow.backend.models;

import com.workflow.backend.enums.EstadoTramite;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tramites")
public class Tramite {

    @Id
    private String id;

    private String politicaId;
    private String clienteId;
    private String nodoActualId;
    private EstadoTramite estado = EstadoTramite.NUEVO;
    private List<HistorialTramite> historial;
    private Map<String, Object> datosFormulario;
    private LocalDateTime fechaInicio = LocalDateTime.now();
    private LocalDateTime fechaFin;
}