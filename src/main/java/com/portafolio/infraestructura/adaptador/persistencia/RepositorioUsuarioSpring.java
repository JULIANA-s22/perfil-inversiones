package com.portafolio.infraestructura.adaptador.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RepositorioUsuarioSpring extends JpaRepository<EntidadUsuarioJpa, UUID> {
    Optional<EntidadUsuarioJpa> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
}
