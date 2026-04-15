package com.portafolio.infraestructura.adaptador.ia;

import com.portafolio.dominio.modelo.MensajeChatbot;
import com.portafolio.dominio.puerto.salida.ClienteChatbotIA;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("clienteClaude")
public class ClienteChatbotIAClaude implements ClienteChatbotIA {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODELO = "claude-haiku-4-5";
    private static final int MAX_MENSAJES_CHAT = 20;

    private final RestClient restClient;
    private final String apiKey;

    public ClienteChatbotIAClaude(
            RestClient.Builder restClientBuilder,
            @Value("${claude.api-key}") String apiKey) {
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
    }

    @Override
    public String procesarCuestionario(List<MensajeChatbot> historial, List<String> preguntasPendientes) {
        String contexto = preguntasPendientes.isEmpty()
                ? "No quedan preguntas. Resume el perfil de inversión del usuario."
                : "Próxima pregunta a hacer: " + preguntasPendientes.get(0);

        String system = PromptsChatbot.CUESTIONARIO + "\n\nContexto actual: " + contexto;
        String mensajeInicial = "Hola, quiero empezar a ahorrar en un fondo de pensión voluntaria y necesito conocer mi perfil de riesgo.";

        return llamarClaude(system, historial, mensajeInicial);
    }

    @Override
    public String responderConsulta(List<MensajeChatbot> historial) {
        List<MensajeChatbot> historialReciente = historial.size() > MAX_MENSAJES_CHAT
                ? historial.subList(historial.size() - MAX_MENSAJES_CHAT, historial.size())
                : historial;
        return llamarClaude(PromptsChatbot.CHAT, historialReciente, "Hola, tengo una consulta sobre fondos de pensión voluntaria en Colombia.");
    }

    private String llamarClaude(String systemPrompt, List<MensajeChatbot> historial, String mensajeInicial) {
        List<Map<String, Object>> messages = construirContenidos(historial, mensajeInicial);

        Map<String, Object> cuerpo = Map.of(
                "model", MODELO,
                "max_tokens", 2048,
                "system", systemPrompt,
                "messages", messages
        );

        @SuppressWarnings("rawtypes")
        Map respuesta = restClient.post()
                .uri(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .body(cuerpo)
                .retrieve()
                .body(Map.class);

        return extraerTexto(respuesta);
    }

    private List<Map<String, Object>> construirContenidos(List<MensajeChatbot> historial, String mensajeInicial) {
        List<Map<String, Object>> contenidos = new ArrayList<>();

        boolean encontroPrimerUsuario = false;
        for (MensajeChatbot mensaje : historial) {
            if (!encontroPrimerUsuario && mensaje.getRol() == MensajeChatbot.RolMensaje.ASISTENTE) {
                continue;
            }
            encontroPrimerUsuario = true;
            String rol = mensaje.getRol() == MensajeChatbot.RolMensaje.USUARIO ? "user" : "assistant";
            contenidos.add(Map.of("role", rol, "content", mensaje.getContenido()));
        }

        if (contenidos.isEmpty()) {
            contenidos.add(Map.of("role", "user", "content", mensajeInicial));
        }

        return contenidos;
    }

    @SuppressWarnings("unchecked")
    private String extraerTexto(Map<?, ?> respuesta) {
        try {
            List<Map<?, ?>> content = (List<Map<?, ?>>) respuesta.get("content");
            return (String) content.get(0).get("text");
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar respuesta de Claude: " + e.getMessage(), e);
        }
    }
}
