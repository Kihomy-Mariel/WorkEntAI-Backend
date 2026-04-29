package com.workflow.backend;

import com.workflow.backend.repositories.NotificacionRepository;
import com.workflow.backend.repositories.TareaRepository;
import com.workflow.backend.repositories.TramiteRepository;
import com.workflow.backend.repositories.UsuarioRepository;
import com.workflow.backend.repositories.PoliticaRepository;
import com.workflow.backend.repositories.DepartamentoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifica que el contexto de Spring arranca correctamente.
 * Usa @MockBean para evitar conexiones reales a MongoDB y servicios externos.
 */
@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private TareaRepository tareaRepository;

    @MockBean
    private TramiteRepository tramiteRepository;

    @MockBean
    private PoliticaRepository politicaRepository;

    @MockBean
    private NotificacionRepository notificacionRepository;

    @MockBean
    private DepartamentoRepository departamentoRepository;

    @MockBean
    private ChatClient.Builder chatClientBuilder;

    @Test
    void contextLoads() {
        // El contexto debe cargar sin errores
    }
}
