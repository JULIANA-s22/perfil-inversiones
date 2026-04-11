package com.portafolio.infraestructura.adaptador.controlador.dto;

import com.portafolio.dominio.modelo.ConversacionChatbot;

import java.time.LocalDateTime;
import java.util.UUID;

public record RespuestaConversacion(
        Integer id,
        UUID usuarioId,
        LocalDateTime iniciadaEn,
        String estado
) {
    public static RespuestaConversacion desde(ConversacionChatbot conversacion) {
        return new RespuestaConversacion(
                conversacion.getId(),
                conversacion.getUsuarioId(),
                conversacion.getIniciadaEn(),
                conversacion.getEstado().name()
        );
    }
}
