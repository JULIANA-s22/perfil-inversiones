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

@Component
public class ClienteChatbotIAGemini implements ClienteChatbotIA {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    private static final String SYSTEM_CUESTIONARIO = """
            Eres un asesor especializado en fondos de pensión voluntaria privada en Colombia.
            Tu plataforma ayuda a las personas a construir su pensión voluntaria eligiendo un portafolio
            adaptado a su perfil de riesgo, dentro del marco de los fondos privados de pensión voluntaria
            disponibles en Colombia (como los que ofrecen Protección, Porvenir, Colfondos y Old Mutual).
            Tu objetivo es realizar un cuestionario para determinar el perfil de riesgo del usuario
            de forma conversacional y cercana.
            Reglas importantes:
            - Haz UNA sola pregunta a la vez, de forma natural y amigable.
            - Contextualiza las preguntas en el mundo de la pensión voluntaria: aportes mensuales,
              horizonte de retiro, tolerancia a la volatilidad del portafolio, etc.
            - Si quedan preguntas pendientes, haz la siguiente.
            - Si ya no hay preguntas pendientes, analiza las respuestas y determina el perfil
              (CONSERVADOR, MODERADO o AGRESIVO) explicando brevemente qué tipo de portafolio
              de pensión voluntaria le conviene y por qué.
            - Responde siempre en español colombiano, de forma cercana y clara.
            - Sé conciso: máximo 3 oraciones por mensaje.
            """;

    private static final String SYSTEM_CHAT = """
            Eres un asesor experto en fondos de pensión voluntaria privada en Colombia.
            Tu especialidad son los fondos de pensión voluntaria (FPV) que ofrecen entidades como
            Protección, Porvenir, Colfondos y Old Mutual, y cómo usarlos como vehículo de ahorro
            e inversión a largo plazo para el retiro.
            Puedes orientar al usuario sobre:
            - Diferencias entre pensión obligatoria y pensión voluntaria en Colombia.
            - Beneficios tributarios del ahorro en FPV (deducción de renta hasta el 30% del ingreso).
            - Tipos de portafolios disponibles (conservador, moderado, agresivo) y sus rendimientos históricos.
            - Cuánto y con qué frecuencia aportar según el objetivo de retiro.
            - Cómo funciona la liquidez y las condiciones de retiro de los fondos voluntarios.
            - Comparación con otros instrumentos de ahorro colombianos (CDTs, acciones, finca raíz).
            Reglas importantes:
            - Responde siempre en español colombiano, de forma clara y cercana.
            - Si no tienes certeza sobre un dato específico (como tasas actuales), dilo honestamente
              y sugiere al usuario verificar con la entidad administradora.
            - Cuando corresponda, menciona cifras en pesos colombianos (COP).
            - Máximo 5 oraciones por respuesta, salvo que el usuario pida una explicación detallada.
            """;

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

        String system = SYSTEM_CUESTIONARIO + "\n\nContexto actual: " + contexto;
        String mensajeInicial = "Hola, quiero empezar a ahorrar en un fondo de pensión voluntaria y necesito conocer mi perfil de riesgo.";

        return llamarGemini(system, historial, mensajeInicial);
    }

    @Override
    public String responderConsulta(List<MensajeChatbot> historial) {
        return llamarGemini(SYSTEM_CHAT, historial, "Hola, tengo una consulta sobre fondos de pensión voluntaria en Colombia.");
    }

    private String llamarGemini(String systemPrompt, List<MensajeChatbot> historial, String mensajeInicial) {
        List<Map<String, Object>> contents = construirContenidos(historial, mensajeInicial);

        Map<String, Object> cuerpo = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", contents,
                "generationConfig", Map.of("temperature", 0.7, "maxOutputTokens", 1024)
        );

        String url = BASE_URL + modelo + ":generateContent?key=" + apiKey;

        @SuppressWarnings("rawtypes")
        Map respuesta = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(Map.class);

        return extraerTexto(respuesta);
    }

    private List<Map<String, Object>> construirContenidos(List<MensajeChatbot> historial, String mensajeInicial) {
        List<Map<String, Object>> contenidos = new ArrayList<>();

        // Gemini requiere que el primer mensaje sea "user", no "model".
        // Si el historial arranca con mensajes de ASISTENTE los saltamos.
        boolean encontroPrimerUsuario = false;
        for (MensajeChatbot mensaje : historial) {
            if (!encontroPrimerUsuario && mensaje.getRol() == MensajeChatbot.RolMensaje.ASISTENTE) {
                continue;
            }
            encontroPrimerUsuario = true;
            String rol = mensaje.getRol() == MensajeChatbot.RolMensaje.USUARIO ? "user" : "model";
            contenidos.add(crearMensaje(rol, mensaje.getContenido()));
        }

        // Si no hay mensajes de usuario en el historial, inyectamos el mensaje inicial
        if (contenidos.isEmpty()) {
            contenidos.add(crearMensaje("user", mensajeInicial));
        }

        return contenidos;
    }

    private Map<String, Object> crearMensaje(String rol, String texto) {
        return Map.of("role", rol, "parts", List.of(Map.of("text", texto)));
    }

    @SuppressWarnings("unchecked")
    private String extraerTexto(Map<?, ?> respuesta) {
        try {
            List<Map<?, ?>> candidates = (List<Map<?, ?>>) respuesta.get("candidates");
            Map<?, ?> content = (Map<?, ?>) candidates.get(0).get("content");
            List<Map<?, ?>> parts = (List<Map<?, ?>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la respuesta de Gemini: " + e.getMessage(), e);
        }
    }
}
