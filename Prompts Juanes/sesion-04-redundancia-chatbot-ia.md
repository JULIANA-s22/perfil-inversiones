# Sesión 04 — Redundancia en el Chatbot IA: Retry + Fallback Claude Haiku
**Fecha:** 2026-04-14

---

## Prompt inicial

**Juanes:** Como puedo hacer redundancia en caso de que falle que haya otro intento o un backoff

**Aclaración:** No es para Rabbit, es para lo de la api de Gemini.

---

## Fase 1 — Retry con backoff exponencial en Gemini

**Juanes:** Hagámoslo manual

**Concepto clave — Backoff exponencial:** En lugar de reintentar inmediatamente (lo que puede saturar la API), se espera un tiempo que se duplica en cada intento fallido.

```
Intento 1 falla → espera 1s → Intento 2 falla → espera 2s → Intento 3 falla → lanza excepción
```

```java
private static final int MAX_INTENTOS = 3;
private static final long BACKOFF_INICIAL_MS = 1000;

for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
    try {
        return llamadaAGemini();
    } catch (Exception e) {
        ultimoError = e;
        if (intento < MAX_INTENTOS) {
            long espera = BACKOFF_INICIAL_MS * (1L << (intento - 1)); // 1s, 2s
            Thread.sleep(espera);
        }
    }
}
throw new RuntimeException("Gemini no respondió tras " + MAX_INTENTOS + " intentos", ultimoError);
```

`1L << (intento - 1)` — bit shift: intento 1 → 1, intento 2 → 2. Forma eficiente de calcular potencias de 2.

---

## Fase 2 — Fallback a Claude Haiku

**Juanes:** Hagamos también que en caso que no funcione Gemini por lo que sea lo lance a Claude con Haiku.

**Primera implementación incorrecta:** Se puso código de Claude dentro de `ClienteChatbotIAGemini`.

**Juanes:** Pero está super mal, porque dentro de un cliente Gemini pusiste algo de Claude.

**Problema identificado:** Violación del principio de responsabilidad única. Un cliente de Gemini no debe saber que existe Claude.

---

## Fase 3 — Refactoring: separación en 3 clases

**Arquitectura resultante:**

```
ClienteChatbotIA (puerto/interfaz)
        │
        ├── ClienteChatbotIAGemini        @Component("clienteGemini")   — solo Gemini + retry
        ├── ClienteChatbotIAClaude        @Component("clienteClaude")   — solo Claude Haiku
        └── ClienteChatbotIAConFallback   @Primary @Component           — orquesta el fallback
```

**PromptsChatbot** — clase package-private para evitar duplicar los prompts entre ambos clientes:
```java
final class PromptsChatbot {
    private PromptsChatbot() {}
    static final String CUESTIONARIO = "...";
    static final String CHAT = "...";
}
```

**Concepto clave — @Primary + @Qualifier:** Cuando Spring tiene múltiples beans del mismo tipo, necesita saber cuál inyectar. `@Primary` marca el predeterminado (el fallback orquestador). `@Qualifier("nombre")` inyecta uno específico.

```java
@Slf4j
@Primary
@Component
public class ClienteChatbotIAConFallback implements ClienteChatbotIA {

    public ClienteChatbotIAConFallback(
            @Qualifier("clienteGemini") ClienteChatbotIA primario,
            @Qualifier("clienteClaude") ClienteChatbotIA fallback) { ... }

    @Override
    public String procesarCuestionario(...) {
        try {
            return primario.procesarCuestionario(...);
        } catch (Exception e) {
            log.warn("Gemini falló en cuestionario, usando Claude Haiku como fallback: {}", e.getMessage());
            return fallback.procesarCuestionario(...);
        }
    }
}
```

**Flujo completo:**
```
Request → ClienteChatbotIAConFallback (@Primary)
               │
               ├── intenta Gemini (3 reintentos + backoff)
               │        │
               │        ├── OK → devuelve respuesta
               │        └── falla 3 veces → lanza excepción
               │
               └── catch → Claude Haiku (sin reintentos, directo)
```

---

## Fase 4 — Diferencias de API entre Gemini y Claude

| | Gemini | Claude Haiku |
|---|---|---|
| URL | `generativelanguage.googleapis.com/v1beta/models/{modelo}:generateContent?key={key}` | `api.anthropic.com/v1/messages` |
| Auth | Query param `?key=` | Header `x-api-key` |
| Rol asistente | `model` | `assistant` |
| System prompt | `systemInstruction.parts[].text` | Campo `system` top-level |
| Modelo | `gemini-3-flash-preview` | `claude-haiku-4-5` |
| Header extra | — | `anthropic-version: 2023-06-01` |

---

## Fase 5 — Mejora de prompts

**Problema detectado:** Claude Haiku en el cuestionario daba `PERFIL_RESULTADO` antes de terminar las preguntas y luego seguía haciendo preguntas de su propia inventiva.

**Causa raíz:** Haiku ignoraba el contexto inyectado `"Próxima pregunta a hacer: X"` y formulaba sus propias preguntas.

**Solución — Reglas explícitas en el prompt:**

```
REGLA 1 — USA SIEMPRE LA PREGUNTA DEL CONTEXTO (CRÍTICO):
- Si dice "Próxima pregunta a hacer: [pregunta]", DEBES hacer ESA pregunta exacta.
  NO inventes preguntas propias. NO cambies el tema. NO hagas una pregunta diferente.
- Si dice "No quedan preguntas. Resume el perfil.", DEBES dar el perfil en este turno.

REGLA 4 — PROHIBICIONES ABSOLUTAS:
- PROHIBIDO dar PERFIL_RESULTADO si el contexto dice "Próxima pregunta".
- PROHIBIDO hacer preguntas si el contexto dice "No quedan preguntas".
- PROHIBIDO inventar o improvisar preguntas que no vengan del contexto.
```

**Resultado:** 7 preguntas aplicadas en orden, `PERFIL_RESULTADO` solo al final, sin continuación posterior.

---

## Archivos modificados / creados

```
infraestructura/adaptador/ia/
  PromptsChatbot.java                  NUEVO  — constantes de prompts compartidas
  ClienteChatbotIAGemini.java          MODIFICADO — solo Gemini, con retry 3 intentos
  ClienteChatbotIAClaude.java          NUEVO  — solo Claude Haiku
  ClienteChatbotIAConFallback.java     NUEVO  — @Primary, orquesta fallback
```

**`application.yml` — claves agregadas:**
```yaml
gemini:
  api-key: AIzaSy...
  modelo: gemini-3-flash-preview

claude:
  api-key: sk-ant-api03-...
```

---

## Pruebas realizadas

- Cuestionario completo (7 preguntas) con Gemini ✅
- Chat de 10 mensajes con Gemini ✅
- Cuestionario completo forzando fallback a Claude Haiku (clave Gemini inválida) ✅
- Chat forzando fallback a Claude Haiku ✅
