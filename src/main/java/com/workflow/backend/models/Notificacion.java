package com.workflow.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notificaciones")
public class Notificacion {

    @Id
    private String id;

    private String usuarioId;
    private String mensaje;
    private String tipo; // NUEVA_TAREA, TRAMITE_COMPLETADO, CUELLO_BOTELLA
    private boolean leida = false;
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}