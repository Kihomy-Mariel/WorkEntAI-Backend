package com.workflow.backend.repositories;

import com.workflow.backend.models.Usuario;
import com.workflow.backend.enums.RolUsuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByRol(RolUsuario rol);
    List<Usuario> findByDepartamento(String departamento);
    boolean existsByEmail(String email);
}