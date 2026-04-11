package com.portafolio.dominio.puerto.salida;

import com.portafolio.dominio.modelo.MensajeChatbot;

import java.util.List;

// TODO: implementar adaptador concreto con el proveedor de IA elegido
public interface ClienteChatbotIA {
    String procesarCuestionario(List<MensajeChatbot> historial, List<String> preguntasPendientes);
    String responderConsulta(List<MensajeChatbot> historial);
}
