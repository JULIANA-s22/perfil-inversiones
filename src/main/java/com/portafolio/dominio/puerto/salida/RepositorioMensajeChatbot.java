package com.portafolio.dominio.puerto.salida;

import com.portafolio.dominio.modelo.MensajeChatbot;

import java.util.List;

public interface RepositorioMensajeChatbot {
    MensajeChatbot guardar(MensajeChatbot mensaje);
    List<MensajeChatbot> buscarPorConversacionId(Integer conversacionId);
}
