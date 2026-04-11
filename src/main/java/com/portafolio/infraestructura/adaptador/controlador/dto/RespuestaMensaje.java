package com.portafolio.infraestructura.adaptador.controlador.dto;

import com.portafolio.dominio.modelo.MensajeChatbot;

import java.time.LocalDateTime;

public record RespuestaMensaje(
        Integer id,
        Integer conversacionId,
        String rol,
        String contenido,
        LocalDateTime enviadoEn
) {
    public static RespuestaMensaje desde(MensajeChatbot mensaje) {
        return new RespuestaMensaje(
                mensaje.getId(),
                mensaje.getConversacionId(),
                mensaje.getRol().name(),
                mensaje.getContenido(),
                mensaje.getEnviadoEn()
        );
    }
}
