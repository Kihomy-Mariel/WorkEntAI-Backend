package com.workflow.backend.services;

import com.workflow.backend.exception.ResourceNotFoundException;
import com.workflow.backend.models.Usuario;
import com.workflow.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public List<Usuario> getAll() {
        return usuarioRepository.findAll();
    }

    public Usuario getById(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    public Usuario update(String id, Usuario usuario) {
        Usuario existing = getById(id);
        existing.setNombre(usuario.getNombre());
        existing.setDepartamento(usuario.getDepartamento());
        existing.setActivo(usuario.isActivo());
        return usuarioRepository.save(existing);
    }

    public void delete(String id) {
        usuarioRepository.deleteById(id);
    }
}