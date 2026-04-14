package com.portafolio.aplicacion.servicio;

import com.portafolio.dominio.modelo.ConversacionChatbot;
import com.portafolio.dominio.modelo.MensajeChatbot;
import com.portafolio.dominio.puerto.salida.ClienteChatbotIA;
import com.portafolio.dominio.puerto.salida.RepositorioConversacionChatbot;
import com.portafolio.dominio.puerto.salida.RepositorioMensajeChatbot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioChatbotTest {

    @Mock
    RepositorioConversacionChatbot repositorioConversacion;

    @Mock
    RepositorioMensajeChatbot repositorioMensaje;

    @Mock
    ClienteChatbotIA clienteIA;

    @InjectMocks
    ServicioChatbot servicio;


    private ConversacionChatbot conversacionActiva() {
        return ConversacionChatbot.builder()
                .id(1).usuarioId(UUID.randomUUID())
                .estado(ConversacionChatbot.EstadoConversacion.ACTIVA)
                .build();
    }

    private ConversacionChatbot conversacionCerrada() {
        ConversacionChatbot c = conversacionActiva();
        c.cerrar();
        return c;
    }

    private MensajeChatbot mensajeAsistente(Integer convId, String contenido) {
        return MensajeChatbot.builder()
                .id(1).conversacionId(convId)
                .rol(MensajeChatbot.RolMensaje.ASISTENTE)
                .contenido(contenido)
                .build();
    }

    private MensajeChatbot mensajeUsuario(Integer convId, String contenido) {
        return MensajeChatbot.builder()
                .id(2).conversacionId(convId)
                .rol(MensajeChatbot.RolMensaje.USUARIO)
                .contenido(contenido)
                .build();
    }

    // -------------------------------------------------------------------------
    // iniciarCuestionario
    // -------------------------------------------------------------------------

    @Test
    void iniciarCuestionario_debeGuardarConversacionYPrimerMensajeDeIA() {
        // ARRANGE
        UUID usuarioId = UUID.randomUUID();
        ConversacionChatbot conversacion = conversacionActiva();

        when(repositorioConversacion.guardar(any())).thenReturn(conversacion);
        when(clienteIA.procesarCuestionario(anyList(), anyList())).thenReturn("¿Cuál es tu objetivo?");
        when(repositorioMensaje.guardar(any())).thenReturn(mensajeAsistente(1, "¿Cuál es tu objetivo?"));

        // ACT
        ConversacionChatbot resultado = servicio.iniciarCuestionario(usuarioId);

        // ASSERT
        assertThat(resultado).isEqualTo(conversacion);
        verify(clienteIA).procesarCuestionario(anyList(), anyList());
        verify(repositorioMensaje).guardar(any());
    }

    // -------------------------------------------------------------------------
    // iniciarChat
    // -------------------------------------------------------------------------

    @Test
    void iniciarChat_debeGuardarYRetornarConversacion() {
        // ARRANGE
        UUID usuarioId = UUID.randomUUID();
        ConversacionChatbot conversacion = conversacionActiva();
        when(repositorioConversacion.guardar(any())).thenReturn(conversacion);

        // ACT
        ConversacionChatbot resultado = servicio.iniciarChat(usuarioId);

        // ASSERT
        assertThat(resultado).isEqualTo(conversacion);
        verify(repositorioConversacion).guardar(any());
    }

    // -------------------------------------------------------------------------
    // responderCuestionario
    // -------------------------------------------------------------------------

    @Test
    void responderCuestionario_debeRetornarRespuestaDeIA_cuandoConversacionEstaActiva() {
        // ARRANGE
        Integer convId = 1;
        ConversacionChatbot conversacion = conversacionActiva();
        MensajeChatbot mensajeIA = mensajeAsistente(convId, "Siguiente pregunta");

        when(repositorioConversacion.buscarPorId(convId)).thenReturn(Optional.of(conversacion));
        // guardar es llamado dos veces: mensaje usuario y luego mensaje IA
        when(repositorioMensaje.guardar(any()))
                .thenReturn(mensajeUsuario(convId, "Mi respuesta"))
                .thenReturn(mensajeIA);
        when(repositorioMensaje.buscarPorConversacionId(convId)).thenReturn(List.of());
        when(clienteIA.procesarCuestionario(anyList(), anyList())).thenReturn("Siguiente pregunta");

        // ACT
        MensajeChatbot resultado = servicio.responderCuestionario(convId, "Mi respuesta");

        // ASSERT
        assertThat(resultado).isEqualTo(mensajeIA);
    }

    @Test
    void responderCuestionario_debeLanzarExcepcion_cuandoConversacionNoExiste() {
        // ARRANGE
        Integer convId = 99;
        when(repositorioConversacion.buscarPorId(convId)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> servicio.responderCuestionario(convId, "respuesta"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(convId));
    }

    @Test
    void responderCuestionario_debeLanzarExcepcion_cuandoConversacionEstaCerrada() {
        // ARRANGE
        Integer convId = 1;
        when(repositorioConversacion.buscarPorId(convId)).thenReturn(Optional.of(conversacionCerrada()));

        // ACT + ASSERT
        assertThatThrownBy(() -> servicio.responderCuestionario(convId, "respuesta"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La conversación está cerrada");
    }

    // -------------------------------------------------------------------------
    // enviarMensajeChat
    // -------------------------------------------------------------------------

    @Test
    void enviarMensajeChat_debeRetornarRespuestaDeIA_cuandoConversacionEstaActiva() {
        // ARRANGE
        Integer convId = 1;
        ConversacionChatbot conversacion = conversacionActiva();
        MensajeChatbot mensajeIA = mensajeAsistente(convId, "Respuesta IA");

        when(repositorioConversacion.buscarPorId(convId)).thenReturn(Optional.of(conversacion));
        when(repositorioMensaje.guardar(any()))
                .thenReturn(mensajeUsuario(convId, "Hola"))
                .thenReturn(mensajeIA);
        when(repositorioMensaje.buscarPorConversacionId(convId)).thenReturn(List.of());
        when(clienteIA.responderConsulta(anyList())).thenReturn("Respuesta IA");

        // ACT
        MensajeChatbot resultado = servicio.enviarMensajeChat(convId, "Hola");

        // ASSERT
        assertThat(resultado).isEqualTo(mensajeIA);
    }

    @Test
    void enviarMensajeChat_debeLanzarExcepcion_cuandoConversacionEstaCerrada() {
        // ARRANGE
        Integer convId = 1;
        when(repositorioConversacion.buscarPorId(convId)).thenReturn(Optional.of(conversacionCerrada()));

        // ACT + ASSERT
        assertThatThrownBy(() -> servicio.enviarMensajeChat(convId, "Hola"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La conversación está cerrada");
    }

    // -------------------------------------------------------------------------
    // obtenerMensajes
    // -------------------------------------------------------------------------

    @Test
    void obtenerMensajes_debeRetornarListaDelRepositorio() {
        // ARRANGE
        Integer convId = 1;
        List<MensajeChatbot> mensajes = List.of(mensajeUsuario(convId, "Hola"));
        when(repositorioMensaje.buscarPorConversacionId(convId)).thenReturn(mensajes);

        // ACT
        List<MensajeChatbot> resultado = servicio.obtenerMensajes(convId);

        // ASSERT
        assertThat(resultado).isEqualTo(mensajes);
    }

    // -------------------------------------------------------------------------
    // listarConversaciones
    // -------------------------------------------------------------------------

    @Test
    void listarConversaciones_debeRetornarListaDelRepositorio() {
        // ARRANGE
        UUID usuarioId = UUID.randomUUID();
        List<ConversacionChatbot> conversaciones = List.of(conversacionActiva());
        when(repositorioConversacion.buscarPorUsuarioId(usuarioId)).thenReturn(conversaciones);

        // ACT
        List<ConversacionChatbot> resultado = servicio.listarConversaciones(usuarioId);

        // ASSERT
        assertThat(resultado).isEqualTo(conversaciones);
    }

    // -------------------------------------------------------------------------
    // cerrarConversacion
    // -------------------------------------------------------------------------

    @Test
    void cerrarConversacion_debeCambiarEstadoYGuardar() {
        // ARRANGE
        Integer convId = 1;
        ConversacionChatbot conversacion = conversacionActiva();
        when(repositorioConversacion.buscarPorId(convId)).thenReturn(Optional.of(conversacion));
        when(repositorioConversacion.guardar(any())).thenReturn(conversacion);

        // ACT
        servicio.cerrarConversacion(convId);

        // ASSERT — verificamos que el estado cambió y que se guardó
        assertThat(conversacion.getEstado())
                .isEqualTo(ConversacionChatbot.EstadoConversacion.CERRADA);
        verify(repositorioConversacion).guardar(conversacion);
    }

    @Test
    void cerrarConversacion_debeLanzarExcepcion_cuandoNoExiste() {
        // ARRANGE
        Integer convId = 99;
        when(repositorioConversacion.buscarPorId(convId)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> servicio.cerrarConversacion(convId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(convId));
    }
}
