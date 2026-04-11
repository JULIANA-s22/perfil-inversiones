package com.portafolio.infraestructura.adaptador.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioMensajeChatbotSpring extends JpaRepository<EntidadMensajeChatbotJpa, Integer> {
    List<EntidadMensajeChatbotJpa> findByConversacionIdOrderByEnviadoEnAsc(Integer conversacionId);
}
