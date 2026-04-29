package com.workflow.backend.ai;

import com.workflow.backend.models.Nodo;
import com.workflow.backend.models.Politica;
import com.workflow.backend.repositories.PoliticaRepository;
import com.workflow.backend.repositories.TareaRepository;
import com.workflow.backend.repositories.TramiteRepository;
import com.workflow.backend.services.AnalyticsService;
import com.workflow.backend.services.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient.Builder chatClientBuilder;
    private final TareaRepository tareaRepository;
    private final TramiteRepository tramiteRepository;
    private final PoliticaRepository politicaRepository;
    private final AnalyticsService analyticsService;
    private final NotificacionService notificacionService;

    // ── 1. Copilot de diagramas ──────────────────────────────────
    public String procesarPromptDiagrama(String prompt) {
        ChatClient chatClient = chatClientBuilder.build();
        String systemPrompt = """
            Eres un asistente experto en diseño de diagramas de actividades UML 2.5 para sistemas de workflow.
            
            Cuando el usuario pida agregar, conectar o modificar nodos, responde ÚNICAMENTE con un JSON válido
            con esta estructura exacta (sin texto adicional, sin markdown, solo el JSON):
            
            {
              "accion": "AGREGAR_NODO" | "CONECTAR_NODOS" | "ELIMINAR_NODO" | "AGREGAR_MULTIPLES" | "DESCRIPCION",
              "nodos": [
                {
                  "key": "id_unico_numerico",
                  "text": "Nombre del nodo",
                  "category": "Start" | "End" | "" | "Decision" | "Parallel",
                  "departamento": "Nombre del departamento (opcional)"
                }
              ],
              "links": [
                { "from": "id_origen", "to": "id_destino", "text": "etiqueta opcional" }
              ],
              "mensaje": "Explicación breve en español de lo que hiciste"
            }
            
            Reglas importantes:
            - category "" = nodo TASK (tarea normal)
            - category "Decision" = nodo de decisión (rombo)
            - category "Start" = inicio del flujo
            - category "End" = fin del flujo
            - category "Parallel" = ejecución paralela
            - Los IDs deben ser números únicos (ej: 100, 101, 102)
            - Si el usuario pide conectar nodos existentes, usa solo "links" sin "nodos"
            - Si no entiendes, usa accion "DESCRIPCION" y explica en "mensaje"
            
            Ejemplos:
            Usuario: "Agrega un nodo de revisión legal después del técnico"
            Respuesta: {"accion":"AGREGAR_NODO","nodos":[{"key":"100","text":"Revisión Legal","category":"","departamento":"Dept. Legal"}],"links":[],"mensaje":"Nodo de Revisión Legal agregado. Conéctalo manualmente al nodo técnico."}
            
            Usuario: "Agrega un flujo completo: inicio, verificación, aprobación, fin"
            Respuesta: {"accion":"AGREGAR_MULTIPLES","nodos":[{"key":"200","text":"Inicio","category":"Start"},{"key":"201","text":"Verificación","category":""},{"key":"202","text":"¿Aprobado?","category":"Decision"},{"key":"203","text":"Fin","category":"End"}],"links":[{"from":"200","to":"201"},{"from":"201","to":"202"},{"from":"202","to":"203","text":"Sí"}],"mensaje":"Flujo completo creado con 4 nodos."}
            """;
        return chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .content();
    }

    // ── 2. Extracción de datos de documento ──────────────────────
    public String extraerDatosDocumento(String textoDocumento, String nombreNodo) {
        ChatClient chatClient = chatClientBuilder.build();
        String systemPrompt = """
            Eres un asistente que extrae datos clave de documentos para formularios de workflow.
            Analiza el texto del documento y extrae la información relevante para el nodo: %s
            Responde SOLO con un JSON con los campos extraídos. Ejemplo:
            {
              "observacion": "texto extraído",
              "aprobado": "true o false según el contenido",
              "datos_adicionales": "cualquier dato relevante encontrado"
            }
            """.formatted(nombreNodo);
        return chatClient.prompt()
                .system(systemPrompt)
                .user("Documento a analizar:\n" + textoDocumento)
                .call()
                .content();
    }

    // ── 3. Detección de cuellos de botella ───────────────────────
    /**
     * Compares average duracionMinutos per Nodo against nodo.tiempoLimiteHoras * 60.
     * Notifies admin for each bottleneck detected.
     * Requirements: 13.2, 13.5
     */
    public Map<String, Object> detectarCuellosBottella(String politicaId) {
        // Load the Política
        Politica politica = politicaRepository.findById(politicaId)
                .orElseThrow(() -> new com.workflow.backend.exception.ResourceNotFoundException(
                        "Política no encontrada: " + politicaId));

        // Get average duracionMinutos per nodoId from AnalyticsService
        Map<String, Double> promediosPorNodo = analyticsService.promediosPorNodo(politicaId);

        List<Map<String, Object>> cuellos = new ArrayList<>();

        for (Nodo nodo : politica.getNodos()) {
            if (nodo.getTiempoLimiteHoras() == null) continue;

            double limiteMinutos = nodo.getTiempoLimiteHoras() * 60.0;
            Double avg = promediosPorNodo.get(nodo.getId());

            if (avg != null && avg > limiteMinutos) {
                // Notify admin about the bottleneck
                notificacionService.notificarAdmin(
                        "Cuello de botella detectado en nodo: " + nodo.getNombre() +
                        " (promedio: " + avg + " min, límite: " + (nodo.getTiempoLimiteHoras() * 60) + " min)",
                        "CUELLO_BOTELLA",
                        politicaId,
                        "TRAMITE"
                );

                Map<String, Object> cuello = new HashMap<>();
                cuello.put("nodoId", nodo.getId());
                cuello.put("nombreNodo", nodo.getNombre());
                cuello.put("promedioMinutos", avg);
                cuello.put("limiteMinutos", limiteMinutos);
                cuellos.add(cuello);
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("politicaId", politicaId);
        resultado.put("promediosPorNodo", promediosPorNodo);
        resultado.put("cuellosDetectados", cuellos);
        resultado.put("fechaAnalisis", LocalDateTime.now().toString());
        return resultado;
    }

    // ── 4. Generación de diagrama UML PlantUML ───────────────────
    /**
     * Genera código PlantUML de actividades UML 2.5 a partir de los nodos de una política.
     * Principio SRP: responsabilidad única de transformar el modelo de dominio a notación UML.
     */
    public String generarPlantUML(String politicaId) {
        Politica politica = politicaRepository.findById(politicaId)
                .orElseThrow(() -> new com.workflow.backend.exception.ResourceNotFoundException(
                        "Política no encontrada: " + politicaId));

        // Construir descripción textual del diagrama para que la IA genere PlantUML
        StringBuilder descripcion = new StringBuilder();
        descripcion.append("Política: ").append(politica.getNombre()).append("\n");
        descripcion.append("Nodos:\n");
        for (Nodo nodo : politica.getNodos()) {
            descripcion.append("- ID: ").append(nodo.getId())
                    .append(", Nombre: ").append(nodo.getNombre())
                    .append(", Tipo: ").append(nodo.getTipo())
                    .append(", Departamento: ").append(nodo.getDepartamento())
                    .append(", Conexiones: ").append(nodo.getConexiones())
                    .append("\n");
        }

        ChatClient chatClient = chatClientBuilder.build();
        String systemPrompt = """
            Eres un experto en UML 2.5. Genera código PlantUML de diagrama de actividades
            para el workflow descrito. Usa la sintaxis PlantUML estándar:
            - @startuml / @enduml
            - start / stop para inicio y fin
            - :Nombre Actividad; para tareas (TASK)
            - if (¿Condición?) then (Sí) / else (No) / endif para decisiones (DECISION)
            - fork / fork again / end fork para paralelos (PARALLEL)
            - Agrega swimlanes con |Departamento| cuando el departamento esté disponible
            - Responde ÚNICAMENTE con el código PlantUML, sin explicaciones ni markdown
            """;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(descripcion.toString())
                .call()
                .content();
    }

    // ── 5. Asistente de usuario (chatbot) ─────────────────────────
    public String asistente(String pregunta) {
        ChatClient chatClient = chatClientBuilder.build();
        String systemPrompt = """
            Eres WorkBot, el asistente inteligente de WorkEntAI, un sistema de gestión de 
            workflows para empresas. Ayudas a los usuarios a entender cómo usar el sistema.
            El sistema tiene 3 roles: ADMIN (crea políticas), FUNCIONARIO (atiende tareas), 
            CLIENTE (consulta trámites). Responde en español, de forma amigable y concisa.
            """;
        return chatClient.prompt()
                .system(systemPrompt)
                .user(pregunta)
                .call()
                .content();
    }
}