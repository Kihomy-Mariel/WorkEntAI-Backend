package com.workflow.backend.models;

import com.workflow.backend.enums.EstadoPolitica;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "politicas")
public class Politica {

    @Id
    private String id;

    private String nombre;

    // Descripción detallada del proceso que representa
    private String descripcion;

    // Categoría del negocio (ej: "Servicios Públicos", "Bancario", "Salud")
    private String categoria;

    // Organización o empresa a la que pertenece
    private String organizacion;

    // Tiempo estimado total del proceso en días
    private Integer tiempoEstimadoDias;

    // Nodos del diagrama de actividades
    @Builder.Default
    private List<Nodo> nodos = new ArrayList<>();

    private EstadoPolitica estado = EstadoPolitica.BORRADOR;

    // ID del admin que la creó
    private String creadoPorId;

    // Nombre del creador (desnormalizado)
    private String nombreCreadoPor;

    // Versión de la política (para control de cambios)
    private Integer version = 1;

    // Cantidad de trámites activos bajo esta política
    private Integer tramitesActivos = 0;

    // Cantidad total de trámites completados
    private Integer tramitesCompletados = 0;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaActivacion;
}