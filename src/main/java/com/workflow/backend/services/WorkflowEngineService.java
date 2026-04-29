package com.workflow.backend.services;

import com.workflow.backend.enums.EstadoTarea;
import com.workflow.backend.enums.EstadoTramite;
import com.workflow.backend.exception.BusinessException;
import com.workflow.backend.exception.ResourceNotFoundException;
import com.workflow.backend.models.*;
import com.workflow.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Motor de workflow: orquesta el avance de un trámite a través de los nodos
 * definidos en la política. Aplica SRP — solo gestiona la lógica de flujo.
 */
@Service
@RequiredArgsConstructor
public class WorkflowEngineService {

    private final TareaRepository tareaRepository;
    private final TramiteRepository tramiteRepository;
    private final PoliticaRepository politicaRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;

    public void completarTarea(String tareaId, Map<String, Object> formularioDatos) {
        Tarea tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", tareaId));

        if (tarea.getEstado() == EstadoTarea.COMPLETADO) {
            throw new BusinessException("La tarea ya fue completada anteriormente");
        }

        Tramite tramite = tramiteRepository.findById(tarea.getTramiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Trámite", tarea.getTramiteId()));

        Politica politica = politicaRepository.findById(tramite.getPoliticaId())
                .orElseThrow(() -> new ResourceNotFoundException("Política", tramite.getPoliticaId()));

        // Validar campos requeridos del formulario antes de completar la tarea
        validarCamposRequeridos(getNodo(politica, tarea.getNodoId()), formularioDatos);

        // Completar tarea y calcular duración
        LocalDateTime ahora = LocalDateTime.now();
        long duracion = Duration.between(tarea.getFechaAsignacion(), ahora).toMinutes();
        tarea.setEstado(EstadoTarea.COMPLETADO);
        tarea.setFormularioDatos(formularioDatos);
        tarea.setFechaCompletado(ahora);
        tarea.setDuracionMinutos(duracion);
        if (formularioDatos.containsKey("observacion")) {
            tarea.setObservacion(formularioDatos.get("observacion").toString());
        }
        tareaRepository.save(tarea);

        // Acumular datos del formulario en el trámite
        if (tramite.getDatosFormulario() == null) tramite.setDatosFormulario(new java.util.HashMap<>());
        tramite.getDatosFormulario().putAll(formularioDatos);

        // Registrar en historial
        Nodo nodoActual = getNodo(politica, tarea.getNodoId());
        agregarHistorial(tramite, nodoActual, tarea, formularioDatos, ahora, duracion);

        // Resolver siguiente nodo
        List<String> conexiones = nodoActual.getConexiones();
        if (conexiones == null || conexiones.isEmpty()) {
            finalizarTramite(tramite, ahora);
            return;
        }

        String siguienteNodoId = resolverSiguienteNodo(nodoActual, formularioDatos, conexiones);
        Nodo siguienteNodo = getNodo(politica, siguienteNodoId);

        if ("END".equals(siguienteNodo.getTipo())) {
            finalizarTramite(tramite, ahora);
            return;
        }

        if ("PARALLEL".equals(siguienteNodo.getTipo())) {
            for (String nodoParaleloId : siguienteNodo.getConexiones()) {
                crearTarea(tramite, politica, nodoParaleloId);
            }
        } else {
            crearTarea(tramite, politica, siguienteNodoId);
        }

        // Actualizar estado del trámite
        tramite.setNodoActualId(siguienteNodoId);
        tramite.setNombreNodoActual(siguienteNodo.getNombre());
        tramite.setDepartamentoActual(siguienteNodo.getDepartamento());
        tramite.setEstado(EstadoTramite.EN_PROCESO);
        tramite.setFechaUltimaActualizacion(ahora);
        tramiteRepository.save(tramite);
    }

    private void agregarHistorial(Tramite tramite, Nodo nodo, Tarea tarea,
                                   Map<String, Object> datos, LocalDateTime fecha, long duracion) {
        if (tramite.getHistorial() == null) tramite.setHistorial(new ArrayList<>());

        String resultado = null;
        if ("DECISION".equals(nodo.getTipo())) {
            Object aprobado = datos.get("aprobado");
            resultado = (aprobado != null && Boolean.parseBoolean(aprobado.toString()))
                    ? "APROBADO" : "RECHAZADO";
        }

        HistorialTramite entrada = HistorialTramite.builder()
                .nodoId(nodo.getId())
                .nombreNodo(nodo.getNombre())
                .departamento(nodo.getDepartamento())
                .funcionarioId(tarea.getFuncionarioId())
                .nombreFuncionario(tarea.getNombreFuncionario())
                .accion("COMPLETADO")
                .observacion(tarea.getObservacion())
                .resultadoDecision(resultado)
                .duracionMinutos(duracion)
                .fecha(fecha)
                .build();

        tramite.getHistorial().add(entrada);
    }

    private String resolverSiguienteNodo(Nodo nodo, Map<String, Object> datos, List<String> conexiones) {
        if ("DECISION".equals(nodo.getTipo())) {
            Object aprobado = datos.get("aprobado");
            boolean aprobadoVal = aprobado != null && Boolean.parseBoolean(aprobado.toString());
            return aprobadoVal ? conexiones.get(0)
                    : (conexiones.size() > 1 ? conexiones.get(1) : conexiones.get(0));
        }
        return conexiones.get(0);
    }

    private void crearTarea(Tramite tramite, Politica politica, String nodoId) {
        Nodo nodo = getNodo(politica, nodoId);

        // Buscar nombre del funcionario para desnormalizar
        String nombreFuncionario = null;
        if (nodo.getResponsableId() != null) {
            nombreFuncionario = usuarioRepository.findById(nodo.getResponsableId())
                    .map(Usuario::getNombre).orElse(null);
        }

        Tarea nuevaTarea = Tarea.builder()
                .tramiteId(tramite.getId())
                .politicaId(politica.getId())
                .nodoId(nodoId)
                .nombreNodo(nodo.getNombre())
                .departamento(nodo.getDepartamento())
                .funcionarioId(nodo.getResponsableId())
                .nombreFuncionario(nombreFuncionario)
                .numeroReferenciaTramite(tramite.getNumeroReferencia())
                .nombrePolitica(politica.getNombre())
                .instrucciones(nodo.getDescripcion())
                .camposFormulario(nodo.getCamposFormulario() != null ? nodo.getCamposFormulario() : new ArrayList<>())
                .prioridad(tramite.getPrioridad())
                .fechaAsignacion(LocalDateTime.now())
                .build();

        tareaRepository.save(nuevaTarea);

        notificacionService.notificarFuncionario(
                nodo.getResponsableId(),
                "Nueva tarea asignada: " + nodo.getNombre() + " — Trámite " + tramite.getNumeroReferencia(),
                "NUEVA_TAREA",
                nuevaTarea.getId(),
                "TAREA"
        );
    }

    private void finalizarTramite(Tramite tramite, LocalDateTime ahora) {
        long duracionTotal = Duration.between(tramite.getFechaInicio(), ahora).toMinutes();
        tramite.setEstado(EstadoTramite.COMPLETADO);
        tramite.setFechaFin(ahora);
        tramite.setFechaUltimaActualizacion(ahora);
        tramite.setDuracionMinutos(duracionTotal);
        tramiteRepository.save(tramite);

        notificacionService.notificarCliente(
                tramite.getClienteId(),
                "Tu trámite " + tramite.getNumeroReferencia() + " ha sido completado exitosamente.",
                "TRAMITE_COMPLETADO",
                tramite.getId(),
                "TRAMITE"
        );
    }

    private void validarCamposRequeridos(Nodo nodo, Map<String, Object> datos) {
        if (nodo.getCamposFormulario() == null) return;
        for (Map<String, Object> campo : nodo.getCamposFormulario()) {
            boolean requerido = Boolean.TRUE.equals(campo.get("requerido"));
            String nombre = (String) campo.get("nombre");
            if (requerido && (datos == null || !datos.containsKey(nombre)
                    || datos.get(nombre) == null
                    || datos.get(nombre).toString().isBlank())) {
                throw new BusinessException("Campo requerido faltante: " + nombre);
            }
        }
    }

    private Nodo getNodo(Politica politica, String nodoId) {
        return politica.getNodos().stream()
                .filter(n -> n.getId().equals(nodoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Nodo '" + nodoId + "' no encontrado en la política"));
    }
}
