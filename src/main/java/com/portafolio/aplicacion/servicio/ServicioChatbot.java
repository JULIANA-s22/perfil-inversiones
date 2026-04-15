package com.portafolio.aplicacion.servicio;

import com.portafolio.dominio.modelo.ConversacionChatbot;
import com.portafolio.dominio.modelo.CuestionarioPerfil;
import com.portafolio.dominio.modelo.MensajeChatbot;
import com.portafolio.dominio.puerto.entrada.CasoUsoChatbot;
import com.portafolio.dominio.puerto.salida.ClienteChatbotIA;
import com.portafolio.dominio.puerto.salida.RepositorioConversacionChatbot;
import com.portafolio.dominio.puerto.salida.RepositorioMensajeChatbot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServicioChatbot implements CasoUsoChatbot {

    private final RepositorioConversacionChatbot repositorioConversacion;
    private final RepositorioMensajeChatbot repositorioMensaje;
    private final ClienteChatbotIA clienteIA;

    @Override
    public ConversacionChatbot iniciarCuestionario(UUID usuarioId) {
        ConversacionChatbot conversacion = repositorioConversacion.guardar(ConversacionChatbot.crear(usuarioId));

        String primeraPregunta = clienteIA.procesarCuestionario(List.of(), CuestionarioPerfil.PREGUNTAS);
        repositorioMensaje.guardar(MensajeChatbot.crear(conversacion.getId(), MensajeChatbot.RolMensaje.ASISTENTE, primeraPregunta));

        return conversacion;
    }

    @Override
    public ConversacionChatbot iniciarChat(UUID usuarioId) {
        return repositorioConversacion.guardar(ConversacionChatbot.crear(usuarioId));
    }

    @Override
    public MensajeChatbot responderCuestionario(Integer conversacionId, String respuesta) {
        validarConversacionActiva(conversacionId);

        List<MensajeChatbot> historialPrevio = repositorioMensaje.buscarPorConversacionId(conversacionId);

        List<MensajeChatbot> historialConRespuesta = new ArrayList<>(historialPrevio);
        historialConRespuesta.add(MensajeChatbot.crear(conversacionId, MensajeChatbot.RolMensaje.USUARIO, respuesta));

        long preguntasRespondidas = historialConRespuesta.stream()
                .filter(m -> m.getRol() == MensajeChatbot.RolMensaje.USUARIO)
                .count();

        int inicio = (int) Math.min(preguntasRespondidas, CuestionarioPerfil.PREGUNTAS.size());
        List<String> pendientes = CuestionarioPerfil.PREGUNTAS.subList(inicio, CuestionarioPerfil.PREGUNTAS.size());

        String respuestaIA = clienteIA.procesarCuestionario(historialConRespuesta, pendientes);

        repositorioMensaje.guardar(MensajeChatbot.crear(conversacionId, MensajeChatbot.RolMensaje.USUARIO, respuesta));
        return repositorioMensaje.guardar(MensajeChatbot.crear(conversacionId, MensajeChatbot.RolMensaje.ASISTENTE, respuestaIA));
    }

    @Override
    public MensajeChatbot enviarMensajeChat(Integer conversacionId, String contenido) {
        validarConversacionActiva(conversacionId);

        List<MensajeChatbot> historialPrevio = repositorioMensaje.buscarPorConversacionId(conversacionId);

        List<MensajeChatbot> historialConMensaje = new ArrayList<>(historialPrevio);
        historialConMensaje.add(MensajeChatbot.crear(conversacionId, MensajeChatbot.RolMensaje.USUARIO, contenido));

        String respuestaIA = clienteIA.responderConsulta(historialConMensaje);

        repositorioMensaje.guardar(MensajeChatbot.crear(conversacionId, MensajeChatbot.RolMensaje.USUARIO, contenido));
        return repositorioMensaje.guardar(MensajeChatbot.crear(conversacionId, MensajeChatbot.RolMensaje.ASISTENTE, respuestaIA));
    }

    @Override
    public List<MensajeChatbot> obtenerMensajes(Integer conversacionId) {
        return repositorioMensaje.buscarPorConversacionId(conversacionId);
    }

    @Override
    public List<ConversacionChatbot> listarConversaciones(UUID usuarioId) {
        return repositorioConversacion.buscarPorUsuarioId(usuarioId);
    }

    @Override
    public void cerrarConversacion(Integer id) {
        ConversacionChatbot conversacion = repositorioConversacion.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada: " + id));
        conversacion.cerrar();
        repositorioConversacion.guardar(conversacion);
    }

    private void validarConversacionActiva(Integer conversacionId) {
        ConversacionChatbot conversacion = repositorioConversacion.buscarPorId(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada: " + conversacionId));
        if (conversacion.getEstado() == ConversacionChatbot.EstadoConversacion.CERRADA) {
            throw new IllegalStateException("La conversación está cerrada");
        }
    }
}
