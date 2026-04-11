package com.portafolio.infraestructura.adaptador.controlador;

import com.portafolio.dominio.modelo.ConversacionChatbot;
import com.portafolio.dominio.modelo.MensajeChatbot;
import com.portafolio.dominio.puerto.entrada.CasoUsoChatbot;
import com.portafolio.infraestructura.adaptador.controlador.dto.PeticionEnviarMensaje;
import com.portafolio.infraestructura.adaptador.controlador.dto.PeticionIniciarConversacion;
import com.portafolio.infraestructura.adaptador.controlador.dto.RespuestaConversacion;
import com.portafolio.infraestructura.adaptador.controlador.dto.RespuestaMensaje;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ControladorChatbot {

    private final CasoUsoChatbot casoUsoChatbot;

    @PostMapping("/conversaciones/cuestionario")
    public ResponseEntity<RespuestaConversacion> iniciarCuestionario(
            @Valid @RequestBody PeticionIniciarConversacion peticion) {
        ConversacionChatbot conversacion = casoUsoChatbot.iniciarCuestionario(peticion.usuarioId());
        return ResponseEntity.status(HttpStatus.CREATED).body(RespuestaConversacion.desde(conversacion));
    }

    @PostMapping("/conversaciones/chat")
    public ResponseEntity<RespuestaConversacion> iniciarChat(
            @Valid @RequestBody PeticionIniciarConversacion peticion) {
        ConversacionChatbot conversacion = casoUsoChatbot.iniciarChat(peticion.usuarioId());
        return ResponseEntity.status(HttpStatus.CREATED).body(RespuestaConversacion.desde(conversacion));
    }

    @GetMapping("/conversaciones/usuario/{usuarioId}")
    public ResponseEntity<List<RespuestaConversacion>> listarConversaciones(
            @PathVariable UUID usuarioId) {
        List<RespuestaConversacion> conversaciones = casoUsoChatbot.listarConversaciones(usuarioId)
                .stream()
                .map(RespuestaConversacion::desde)
                .toList();
        return ResponseEntity.ok(conversaciones);
    }

    @PostMapping("/conversaciones/{id}/responder")
    public ResponseEntity<RespuestaMensaje> responderCuestionario(
            @PathVariable Integer id,
            @Valid @RequestBody PeticionEnviarMensaje peticion) {
        MensajeChatbot respuesta = casoUsoChatbot.responderCuestionario(id, peticion.contenido());
        return ResponseEntity.status(HttpStatus.CREATED).body(RespuestaMensaje.desde(respuesta));
    }

    @PostMapping("/conversaciones/{id}/mensajes")
    public ResponseEntity<RespuestaMensaje> enviarMensajeChat(
            @PathVariable Integer id,
            @Valid @RequestBody PeticionEnviarMensaje peticion) {
        MensajeChatbot respuesta = casoUsoChatbot.enviarMensajeChat(id, peticion.contenido());
        return ResponseEntity.status(HttpStatus.CREATED).body(RespuestaMensaje.desde(respuesta));
    }

    @GetMapping("/conversaciones/{id}/mensajes")
    public ResponseEntity<List<RespuestaMensaje>> obtenerMensajes(@PathVariable Integer id) {
        List<RespuestaMensaje> mensajes = casoUsoChatbot.obtenerMensajes(id)
                .stream()
                .map(RespuestaMensaje::desde)
                .toList();
        return ResponseEntity.ok(mensajes);
    }

    @PutMapping("/conversaciones/{id}/cerrar")
    public ResponseEntity<Void> cerrarConversacion(@PathVariable Integer id) {
        casoUsoChatbot.cerrarConversacion(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarErrorArgumento(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> manejarErrorEstado(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
