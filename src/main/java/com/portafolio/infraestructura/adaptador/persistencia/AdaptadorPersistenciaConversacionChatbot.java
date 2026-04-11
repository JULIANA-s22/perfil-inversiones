package com.portafolio.infraestructura.adaptador.persistencia;

import com.portafolio.dominio.modelo.ConversacionChatbot;
import com.portafolio.dominio.puerto.salida.RepositorioConversacionChatbot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdaptadorPersistenciaConversacionChatbot implements RepositorioConversacionChatbot {

    private final RepositorioConversacionChatbotSpring repositorioJpa;

    @Override
    public ConversacionChatbot guardar(ConversacionChatbot conversacion) {
        EntidadConversacionChatbotJpa entidad = EntidadConversacionChatbotJpa.desdeDominio(conversacion);
        return repositorioJpa.save(entidad).aDominio();
    }

    @Override
    public Optional<ConversacionChatbot> buscarPorId(Integer id) {
        return repositorioJpa.findById(id).map(EntidadConversacionChatbotJpa::aDominio);
    }

    @Override
    public List<ConversacionChatbot> buscarPorUsuarioId(UUID usuarioId) {
        return repositorioJpa.findByUsuarioId(usuarioId).stream()
                .map(EntidadConversacionChatbotJpa::aDominio)
                .toList();
    }
}
