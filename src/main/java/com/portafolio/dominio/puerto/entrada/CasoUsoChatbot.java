package com.portafolio.dominio.puerto.entrada;

import com.portafolio.dominio.modelo.ConversacionChatbot;
import com.portafolio.dominio.modelo.MensajeChatbot;

import java.util.List;
import java.util.UUID;

public interface CasoUsoChatbot {
    ConversacionChatbot iniciarCuestionario(UUID usuarioId);
    ConversacionChatbot iniciarChat(UUID usuarioId);
    MensajeChatbot responderCuestionario(Integer conversacionId, String respuesta);
    MensajeChatbot enviarMensajeChat(Integer conversacionId, String contenido);
    List<MensajeChatbot> obtenerMensajes(Integer conversacionId);
    List<ConversacionChatbot> listarConversaciones(UUID usuarioId);
    void cerrarConversacion(Integer id);
}
