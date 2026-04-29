package com.workflow.backend.controllers;

import com.workflow.backend.models.Usuario;
import com.workflow.backend.repositories.UsuarioRepository;
import com.workflow.backend.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Usuario>> getAll() {
        return ResponseEntity.ok(usuarioService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getById(@PathVariable String id) {
        return ResponseEntity.ok(usuarioService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> update(@PathVariable String id, @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.update(id, usuario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Guarda el token FCM del dispositivo móvil para notificaciones push.
     * Llamado automáticamente por la app Flutter al hacer login.
     */
    @PostMapping("/{id}/fcm-token")
    public ResponseEntity<Void> saveFcmToken(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token != null && !token.isBlank()) {
            usuarioRepository.findById(id).ifPresent(u -> {
                u.setFcmToken(token);
                usuarioRepository.save(u);
            });
        }
        return ResponseEntity.ok().build();
    }
}