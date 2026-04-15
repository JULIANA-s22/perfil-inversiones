package com.portafolio.infraestructura.adaptador.ia;

import com.portafolio.dominio.modelo.MensajeChatbot;
import com.portafolio.dominio.puerto.salida.ClienteChatbotIA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Primary
@Component
public class ClienteChatbotIAConFallback implements ClienteChatbotIA {

    private final ClienteChatbotIA primario;
    private final ClienteChatbotIA fallback;

    public ClienteChatbotIAConFallback(
            @Qualifier("clienteGemini") ClienteChatbotIA primario,
            @Qualifier("clienteClaude") ClienteChatbotIA fallback) {
        this.primario = primario;
        this.fallback = fallback;
    }

    @Override
    public String procesarCuestionario(List<MensajeChatbot> historial, List<String> preguntasPendientes) {
        try {
            return primario.procesarCuestionario(historial, preguntasPendientes);
        } catch (Exception e) {
            log.warn("Gemini falló en cuestionario, usando Claude Haiku como fallback: {}", e.getMessage());
            return fallback.procesarCuestionario(historial, preguntasPendientes);
        }
    }

    @Override
    public String responderConsulta(List<MensajeChatbot> historial) {
        try {
            return primario.responderConsulta(historial);
        } catch (Exception e) {
            log.warn("Gemini falló en chat, usando Claude Haiku como fallback: {}", e.getMessage());
            return fallback.responderConsulta(historial);
        }
    }
}
