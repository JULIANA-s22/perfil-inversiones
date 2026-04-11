package com.portafolio.dominio.puerto.salida;

import com.portafolio.dominio.modelo.ConversacionChatbot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioConversacionChatbot {
    ConversacionChatbot guardar(ConversacionChatbot conversacion);
    Optional<ConversacionChatbot> buscarPorId(Integer id);
    List<ConversacionChatbot> buscarPorUsuarioId(UUID usuarioId);
}
