package com.workflow.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nodo {

    private String id;
    private String nombre;

    // Descripción de qué hace este nodo / qué debe hacer el funcionario
    private String descripcion;

    // Tipo: START, END, TASK, DECISION, PARALLEL
    private String tipo;

    private String departamento;

    // ID del funcionario responsable por defecto
    private String responsableId;

    // Nombre del responsable (desnormalizado)
    private String nombreResponsable;

    // Tiempo máximo permitido en horas antes de considerarse cuello de botella
    private Integer tiempoLimiteHoras;

    // Posición en el canvas del editor
    private double posX;
    private double posY;

    // IDs de nodos siguientes
    @Builder.Default
    private List<String> conexiones = new ArrayList<>();

    // Condiciones para flujos alternativos (DECISION)
    // Ej: {"true": "nodoAprobadoId", "false": "nodoRechazadoId"}
    @Builder.Default
    private Map<String, String> condiciones = new HashMap<>();

    // Definición de campos del formulario que debe llenar el funcionario
    // Ej: [{"nombre": "aprobado", "tipo": "boolean", "requerido": true, "etiqueta": "¿Aprobado?"}]
    @Builder.Default
    private List<Map<String, Object>> camposFormulario = new ArrayList<>();

    // Documentos requeridos en este nodo
    @Builder.Default
    private List<String> documentosRequeridos = new ArrayList<>();

    // Color del nodo en el diagrama (para UX)
    private String color;
}