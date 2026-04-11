package com.portafolio.infraestructura.adaptador.persistencia;

import com.portafolio.dominio.modelo.MensajeChatbot;
import com.portafolio.dominio.puerto.salida.RepositorioMensajeChatbot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdaptadorPersistenciaMensajeChatbot implements RepositorioMensajeChatbot {

    private final RepositorioMensajeChatbotSpring repositorioJpa;

    @Override
    public MensajeChatbot guardar(MensajeChatbot mensaje) {
        EntidadMensajeChatbotJpa entidad = EntidadMensajeChatbotJpa.desdeDominio(mensaje);
        return repositorioJpa.save(entidad).aDominio();
    }

    @Override
    public List<MensajeChatbot> buscarPorConversacionId(Integer conversacionId) {
        return repositorioJpa.findByConversacionIdOrderByEnviadoEnAsc(conversacionId).stream()
                .map(EntidadMensajeChatbotJpa::aDominio)
                .toList();
    }
}
