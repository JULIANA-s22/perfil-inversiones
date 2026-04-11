package com.portafolio.dominio.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversacionChatbot {

    private Integer id;
    private UUID usuarioId;
    @Builder.Default
    private LocalDateTime iniciadaEn = LocalDateTime.now();
    @Builder.Default
    private EstadoConversacion estado = EstadoConversacion.ACTIVA;

    public enum EstadoConversacion {
        ACTIVA, CERRADA
    }

    public static ConversacionChatbot crear(UUID usuarioId) {
        return ConversacionChatbot.builder()
                .usuarioId(usuarioId)
                .build();
    }

    public void cerrar() {
        this.estado = EstadoConversacion.CERRADA;
    }
}
