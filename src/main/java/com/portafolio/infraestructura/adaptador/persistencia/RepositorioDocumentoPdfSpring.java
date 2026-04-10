package com.portafolio.infraestructura.adaptador.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RepositorioDocumentoPdfSpring extends JpaRepository<EntidadDocumentoPdfJpa, Integer> {
    List<EntidadDocumentoPdfJpa> findByUsuarioId(UUID usuarioId);
}
