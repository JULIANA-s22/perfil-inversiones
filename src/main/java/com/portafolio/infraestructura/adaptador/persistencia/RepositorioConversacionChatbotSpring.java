package com.portafolio.infraestructura.adaptador.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RepositorioConversacionChatbotSpring extends JpaRepository<EntidadConversacionChatbotJpa, Integer> {
    List<EntidadConversacionChatbotJpa> findByUsuarioId(UUID usuarioId);
}
