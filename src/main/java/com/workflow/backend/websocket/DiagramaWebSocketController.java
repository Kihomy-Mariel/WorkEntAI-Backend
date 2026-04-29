package com.workflow.backend.websocket;

import com.workflow.backend.dto.DiagramaCambioDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class DiagramaWebSocketController {

    @MessageMapping("/politica/{id}/editar")
    @SendTo("/topic/politica/{id}")
    public DiagramaCambioDTO procesarCambio(@DestinationVariable String id, DiagramaCambioDTO cambio) {
        return cambio;
    }
}
