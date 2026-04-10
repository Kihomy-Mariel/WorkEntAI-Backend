package com.workflow.backend.models;

import com.workflow.backend.enums.EstadoPolitica;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "politicas")
public class Politica {

    @Id
    private String id;

    private String nombre;
    private String descripcion;
    private List<Nodo> nodos;
    private EstadoPolitica estado = EstadoPolitica.BORRADOR;
    private String creadoPorId;
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    private LocalDateTime fechaActualizacion = LocalDateTime.now();
}