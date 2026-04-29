package com.workflow.backend.services;

import com.workflow.backend.enums.EstadoPolitica;
import com.workflow.backend.exception.BusinessException;
import com.workflow.backend.exception.ResourceNotFoundException;
import com.workflow.backend.models.Politica;
import com.workflow.backend.repositories.PoliticaRepository;
import com.workflow.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PoliticaService {

    private final PoliticaRepository politicaRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Politica> getAll() {
        return politicaRepository.findAll();
    }

    public List<Politica> getActivas() {
        return politicaRepository.findByEstado(EstadoPolitica.ACTIVA);
    }

    public Politica getById(String id) {
        return politicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Política", id));
    }

    public Politica create(Politica politica) {
        LocalDateTime ahora = LocalDateTime.now();
        politica.setEstado(EstadoPolitica.BORRADOR);
        politica.setFechaCreacion(ahora);
        politica.setFechaActualizacion(ahora);
        politica.setVersion(1);
        politica.setTramitesActivos(0);
        politica.setTramitesCompletados(0);
        if (politica.getNodos() == null) politica.setNodos(new ArrayList<>());

        // Desnormalizar nombre del creador
        if (politica.getCreadoPorId() != null) {
            usuarioRepository.findById(politica.getCreadoPorId())
                    .ifPresent(u -> politica.setNombreCreadoPor(u.getNombre()));
        }

        return politicaRepository.save(politica);
    }

    public Politica update(String id, Politica datos) {
        Politica existente = getById(id);

        if (existente.getEstado() == EstadoPolitica.ACTIVA) {
            throw new BusinessException("No se puede editar una política activa. Desactívela primero.");
        }

        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setCategoria(datos.getCategoria());
        existente.setOrganizacion(datos.getOrganizacion());
        existente.setTiempoEstimadoDias(datos.getTiempoEstimadoDias());
        existente.setNodos(datos.getNodos() != null ? datos.getNodos() : existente.getNodos());
        existente.setFechaActualizacion(LocalDateTime.now());
        existente.setVersion(existente.getVersion() + 1);

        return politicaRepository.save(existente);
    }

    public Politica activar(String id) {
        Politica politica = getById(id);

        if (politica.getNodos() == null || politica.getNodos().isEmpty()) {
            throw new BusinessException("No se puede activar una política sin nodos definidos");
        }

        boolean tieneStart = politica.getNodos().stream()
                .anyMatch(n -> "START".equals(n.getTipo()));
        boolean tieneEnd = politica.getNodos().stream()
                .anyMatch(n -> "END".equals(n.getTipo()));

        if (!tieneStart || !tieneEnd) {
            throw new BusinessException("La política debe tener al menos un nodo START y un nodo END");
        }

        politica.setEstado(EstadoPolitica.ACTIVA);
        politica.setFechaActivacion(LocalDateTime.now());
        politica.setFechaActualizacion(LocalDateTime.now());
        return politicaRepository.save(politica);
    }

    public Politica desactivar(String id) {
        Politica politica = getById(id);
        politica.setEstado(EstadoPolitica.BORRADOR);
        politica.setFechaActualizacion(LocalDateTime.now());
        return politicaRepository.save(politica);
    }

    public void delete(String id) {
        Politica politica = getById(id);
        if (politica.getTramitesActivos() != null && politica.getTramitesActivos() > 0) {
            throw new BusinessException("No se puede eliminar una política con trámites activos");
        }
        politicaRepository.deleteById(id);
    }
}
