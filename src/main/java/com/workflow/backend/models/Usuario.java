package com.workflow.backend.models;

import com.workflow.backend.enums.RolUsuario;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    private String nombre;

    @Indexed(unique = true)
    private String email;

    private String password;

    private RolUsuario rol;

    private String departamento;

    private boolean activo = true;

    private LocalDateTime fechaCreacion = LocalDateTime.now();
}