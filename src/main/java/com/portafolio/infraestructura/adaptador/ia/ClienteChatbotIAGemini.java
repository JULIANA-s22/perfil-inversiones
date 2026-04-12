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
            Eres un asesor experto en perfiles de inversión. Tu único objetivo es hacerle
            un cuestionario al usuario para determinar cuál de los tres perfiles le aplica:
            conservador, moderado o agresivo.
            
            Definición de cada perfil:
            - conservador: prefiere seguridad ante todo, no tolera pérdidas, horizonte menor a 3 años,
              poca experiencia invirtiendo. Rendimiento esperado ~3% anual.
            - moderado: acepta cierta volatilidad a cambio de mejores retornos, horizonte 3-10 años,
              algo de experiencia. Rendimiento esperado ~7% anual.
            - agresivo: tolera pérdidas temporales, busca máxima rentabilidad, horizonte mayor a 10 años,
              experiencia invirtiendo o disposición a aprender. Rendimiento esperado ~11% anual.
            
            Reglas ESTRICTAS:
            - Haz UNA sola pregunta a la vez, de forma natural y amigable.
            - Si quedan preguntas pendientes, formula la siguiente pregunta solamente.
            - Cuando ya no queden preguntas, tu respuesta DEBE terminar SIEMPRE con esta línea exacta:
              PERFIL_RESULTADO: conservador
              o
              PERFIL_RESULTADO: moderado
              o
              PERFIL_RESULTADO: agresivo
            - Antes de esa línea, explica en 2-3 oraciones por qué ese perfil le conviene.
            - Responde siempre en español colombiano, de forma cercana y clara.
            - Máximo 4 oraciones por mensaje (sin contar la línea PERFIL_RESULTADO).
            """;

    private static final String SYSTEM_CHAT = """
            Eres un asesor experto en inversiones y perfiles de riesgo financiero.
            Tu especialidad es orientar a las personas sobre qué perfil de inversión les conviene
            según sus objetivos, horizonte de tiempo y tolerancia al riesgo.
            Trabajas con tres perfiles:
            - CONSERVADOR (~3% anual): capital protegido, bajo riesgo, para horizontes cortos.
            - MODERADO (~7% anual): balance entre riesgo y rentabilidad, mediano plazo.
            - AGRESIVO (~11% anual): mayor rentabilidad potencial, alto riesgo, largo plazo.
            Puedes orientar al usuario sobre:
            - Cuál perfil le conviene según su situación personal.
            - Cómo funciona el interés compuesto y cómo crece su dinero en el tiempo.
            - Cuánto y con qué frecuencia aportar según su meta financiera.
            - Diferencias entre los perfiles y qué esperar de cada uno.
            - Conceptos básicos de inversión: riesgo, rentabilidad, diversificación, horizonte.
            Reglas importantes:
            - Responde siempre en español colombiano, de forma clara y cercana.
            - Menciona cifras en pesos colombianos (COP) cuando sea relevante.
            - Si no tienes certeza sobre un dato específico, dilo honestamente.
            - Máximo 5 oraciones por respuesta, salvo que el usuario pida más detalle.
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
