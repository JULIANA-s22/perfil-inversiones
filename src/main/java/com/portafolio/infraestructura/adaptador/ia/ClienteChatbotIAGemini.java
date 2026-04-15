package com.portafolio.infraestructura.adaptador.ia;

import com.portafolio.dominio.modelo.MensajeChatbot;
import com.portafolio.dominio.puerto.salida.ClienteChatbotIA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("clienteGemini")
public class ClienteChatbotIAGemini implements ClienteChatbotIA {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final int MAX_INTENTOS = 3;
    private static final long BACKOFF_INICIAL_MS = 1000;

    private final RestClient restClient;
    private final String apiKey;
    private final String modelo;

    public ClienteChatbotIAGemini(
            RestClient.Builder restClientBuilder,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.modelo:gemini-3-flash-preview}") String modelo) {
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
        this.modelo = modelo;
    }

    @Override
    public String procesarCuestionario(List<MensajeChatbot> historial, List<String> preguntasPendientes) {
        String contexto = preguntasPendientes.isEmpty()
                ? "No quedan preguntas. Resume el perfil de inversión del usuario."
                : "Próxima pregunta a hacer: " + preguntasPendientes.get(0);

        String system = PromptsChatbot.CUESTIONARIO + "\n\nContexto actual: " + contexto;
        String mensajeInicial = "Hola, quiero empezar a ahorrar en un fondo de pensión voluntaria y necesito conocer mi perfil de riesgo.";

        return llamarGemini(system, historial, mensajeInicial);
    }

    private static final int MAX_MENSAJES_CHAT = 20;

    @Override
    public String responderConsulta(List<MensajeChatbot> historial) {
        List<MensajeChatbot> historialReciente = historial.size() > MAX_MENSAJES_CHAT
                ? historial.subList(historial.size() - MAX_MENSAJES_CHAT, historial.size())
                : historial;
        return llamarGemini(PromptsChatbot.CHAT, historialReciente, "Hola, tengo una consulta sobre fondos de pensión voluntaria en Colombia.");
    }

    private String llamarGemini(String systemPrompt, List<MensajeChatbot> historial, String mensajeInicial) {
        List<Map<String, Object>> contents = construirContenidos(historial, mensajeInicial);

        Map<String, Object> cuerpo = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", contents,
                "generationConfig", Map.of("temperature", 0.7, "maxOutputTokens", 2048)
        );

        String url = BASE_URL + modelo + ":generateContent?key=" + apiKey;

        Exception ultimoError = null;
        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            try {
                @SuppressWarnings("rawtypes")
                Map respuesta = restClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(cuerpo)
                        .retrieve()
                        .body(Map.class);
                return extraerTexto(respuesta);
            } catch (Exception e) {
                ultimoError = e;
                log.warn("Gemini intento {}/{} fallido: {}", intento, MAX_INTENTOS, e.getMessage());
                if (intento < MAX_INTENTOS) {
                    long espera = BACKOFF_INICIAL_MS * (1L << (intento - 1));
                    try {
                        Thread.sleep(espera);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrumpido", ie);
                    }
                }
            }
        }
        throw new RuntimeException("Gemini no respondió tras " + MAX_INTENTOS + " intentos", ultimoError);
    }

    private List<Map<String, Object>> construirContenidos(List<MensajeChatbot> historial, String mensajeInicial) {
        List<Map<String, Object>> contenidos = new ArrayList<>();

        boolean encontroPrimerUsuario = false;
        for (MensajeChatbot mensaje : historial) {
            if (!encontroPrimerUsuario && mensaje.getRol() == MensajeChatbot.RolMensaje.ASISTENTE) {
                continue;
            }
            encontroPrimerUsuario = true;
            String rol = mensaje.getRol() == MensajeChatbot.RolMensaje.USUARIO ? "user" : "model";
            contenidos.add(Map.of("role", rol, "parts", List.of(Map.of("text", mensaje.getContenido()))));
        }

        if (contenidos.isEmpty()) {
            contenidos.add(Map.of("role", "user", "parts", List.of(Map.of("text", mensajeInicial))));
        }

        return contenidos;
    }

    @SuppressWarnings("unchecked")
    private String extraerTexto(Map<?, ?> respuesta) {
        try {
            List<Map<?, ?>> candidates = (List<Map<?, ?>>) respuesta.get("candidates");
            Map<?, ?> content = (Map<?, ?>) candidates.get(0).get("content");
            List<Map<?, ?>> parts = (List<Map<?, ?>>) content.get("parts");
            for (Map<?, ?> part : parts) {
                if (!Boolean.TRUE.equals(part.get("thought"))) {
                    return (String) part.get("text");
                }
            }
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la respuesta de Gemini: " + e.getMessage(), e);
        }
    }
}
