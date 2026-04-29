package com.workflow.backend.dto;

import com.workflow.backend.models.Nodo;

public record DiagramaCambioDTO(
        String tipo,
        String editorId,
        Nodo nodo,
        String desdeId,
        String hastaId,
        double posX,
        double posY
) {}
