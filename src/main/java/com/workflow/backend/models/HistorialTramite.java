package com.workflow.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialTramite {

    private String nodoId;
    private String nombreNodo;
    private String funcionarioId;
    private String accion;
    private String observacion;
    private LocalDateTime fecha = LocalDateTime.now();
}