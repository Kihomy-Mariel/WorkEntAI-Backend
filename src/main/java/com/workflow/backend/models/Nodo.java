package com.workflow.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nodo {

    private String id;
    private String nombre;
    private String tipo; // START, END, TASK, DECISION, PARALLEL
    private String departamento;
    private String responsableId;
    private double posX;
    private double posY;
    private List<String> conexiones; // IDs de nodos siguientes
    private Map<String, String> condiciones; // para flujos alternativos
    private Map<String, Object> formulario; // campos del formulario
}