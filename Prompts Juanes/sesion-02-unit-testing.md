# Sesión 02 — Unit Testing con patrón AAA
**Fecha:** 2026-04-13

---

## Contexto

**Juanes:** ROL EXPERTO EN TESTING: Hola quiero hacer pruebas unitarias a la implementación de mis servicios en aplicacion.servicio, quiero con patron AAA, no se que más información requieras, es usando testImplementation 'org.springframework.boot:spring-boot-starter-test', no quiero que lo hagas y ya. Prefiero ir lento y entendiendo que quedar con deuda cognitiva.

**Respuesta clave:** `spring-boot-starter-test` ya incluye JUnit 5, Mockito y AssertJ. No se necesita ninguna dependencia adicional. Los 4 servicios se ordenan por dificultad: `ServicioSimulacion` (sin dependencias), `ServicioHistorial` (1 mock), `ServicioAutenticacion` (3 mocks), `ServicioChatbot` (3 mocks + estado).

---

## Patrón AAA explicado

**Juanes:** Vamos con el 1 (ServicioSimulacion)

**Respuesta clave:**
```
// ARRANGE  → prepara el escenario (datos de entrada, objetos necesarios)
// ACT      → ejecuta lo que quieres probar (una sola cosa)
// ASSERT   → verifica que el resultado es el esperado
```
Cada test prueba un solo comportamiento. Si falla, sabes exactamente qué rompió.

---

## assertThrows entendido

**Juanes:** Eso valida que debe lanzar IllegalArgument... cuando llame a la funcion con esos valores?

**Respuesta clave:**
```java
assertThrows(
    IllegalArgumentException.class,   // "espero que se lance ESTA excepción"
    () -> servicio.simular(-1, 1000, 10)  // "cuando ejecuto ESTO"
);
```
La lambda `() ->` le dice a `assertThrows`: ejecuta esta función, y si lanza la excepción esperada el test pasa. Si no la lanza (o lanza otra), el test falla.

---

## ServicioSimulacion — Tests escritos

**Juanes:** Listo (para escribir los tests)

**Tests Grupo A — validaciones (sin mocks, lógica pura):**
```java
@BeforeEach
void setUp() {
    servicio = new ServicioSimulacion();  // sin mocks, sin Spring
}

// assertThrows para excepciones
assertThrows(IllegalArgumentException.class,
    () -> servicio.simular(-1, 1000, 10));

// assertDoesNotThrow para bordes válidos
assertDoesNotThrow(() -> servicio.simular(capital, aporte, tiempo));
```

**Concepto nuevo — `@BeforeEach`:** Se ejecuta antes de cada `@Test`. Evita repetir `new ServicioSimulacion()` en cada test y garantiza objeto limpio por test.

**Tests Grupo B — cálculo (AssertJ):**
```java
// Lógica de negocio: a mayor tasa, mayor valor
assertThat(resultado.getAgresivo().getValorFuturo())
    .isGreaterThan(resultado.getModerado().getValorFuturo());

// Proyección tiene año 0 hasta año N = N+1 elementos
assertThat(resultado.getProyeccionConservador()).hasSize(tiempo + 1);

// Año 0 sin crecimiento = capital inicial
assertThat(anio0.getProyectado()).isEqualTo(capital);

// Valor exacto con aporte=0: valorFuturo = capital × (1 + tasa/12)^(años×12)
assertThat(resultado.getConservador().getValorFuturo()).isEqualTo(10_304L);
assertThat(resultado.getModerado().getValorFuturo()).isEqualTo(10_723L);
assertThat(resultado.getAgresivo().getValorFuturo()).isEqualTo(11_157L);
```

---

## Fix de configuración — Gradle 9 + JUnit Platform

**Error encontrado:** `Failed to load JUnit Platform` con Gradle 9.

**Solución:** Agregar al `build.gradle`:
```groovy
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```
Gradle 9+ requiere declarar el launcher explícitamente. Antes lo agregaba automático.

---

## ServicioHistorial — Primer mock

**Juanes:** Ahora quiero hacer las de que el calculo esta bien / Listo dale

**Concepto nuevo — Mockito:**
```java
@ExtendWith(MockitoExtension.class)
class ServicioHistorialTest {

    @Mock
    RepositorioSimulacion repositorio;  // objeto falso, no toca BD

    @InjectMocks
    ServicioHistorial servicio;  // crea el servicio e inyecta el mock
}
```
- `@Mock` → objeto falso que por defecto devuelve null/0/false
- `@InjectMocks` → reemplaza al `new Servicio()`, Mockito inyecta las dependencias
- `when(...).thenReturn(...)` → define qué devuelve el mock
- `verify(...)` → verifica que la interacción ocurrió

---

## ServicioAutenticacion — Múltiples mocks y Optional

**Juanes:** Ahora sigamos con Autenticación / Si dale

**Conceptos nuevos:**

```java
// 3 mocks simultáneos
@Mock RepositorioUsuario repositorioUsuario;
@Mock ProveedorToken proveedorToken;
@Mock EncriptadorContrasena encriptadorContrasena;

// Optional en mocks — usuario encontrado
when(repositorioUsuario.buscarPorCorreo("juan@test.com"))
    .thenReturn(Optional.of(usuario));

// Optional en mocks — usuario no encontrado
when(repositorioUsuario.buscarPorCorreo("noexiste@test.com"))
    .thenReturn(Optional.empty());

// Booleanos en mocks
when(repositorioUsuario.existePorCorreo(correo)).thenReturn(false);
when(encriptadorContrasena.coincide(contrasena, hash)).thenReturn(true);

// assertThatThrownBy — versión fluida con verificación del mensaje
assertThatThrownBy(() -> servicio.registrar(correo, "1234", "Juan"))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining(correo);

// anyString() — no importa qué string se pase
when(encriptadorContrasena.coincide(anyString(), anyString())).thenReturn(false);
```

---

## ServicioChatbot — thenReturn encadenado y métodos privados

**Juanes:** Listo ahora hazlo con el servicio que falta porfa

**Concepto nuevo — thenReturn encadenado:**
```java
// El repositorio es llamado dos veces seguidas con el mismo método
when(repositorioMensaje.guardar(any()))
    .thenReturn(mensajeUsuario(convId, "Mi respuesta"))  // 1ra llamada
    .thenReturn(mensajeIA);                               // 2da llamada
```

**Concepto nuevo — métodos privados:**

No se pueden testear directamente. Se prueban a través del método público que los llama.

```
test llama a → responderCuestionario()
                   └─ que internamente llama a → validarConversacionActiva()
                                                      └─ que lanza la excepción
```

**Analogía del usuario:** *"Es como tener las llaves de una casa que cuando la abro suena una alarma, pero si quiero probar la alarma tengo que entrar a la casa. Y si la casa tiene dos puertas, tengo que probar que la alarma suena cuando entro por cualquiera de las dos."*

**Fixtures reutilizables:**
```java
private ConversacionChatbot conversacionActiva() {
    return ConversacionChatbot.builder()
            .id(1).usuarioId(UUID.randomUUID())
            .estado(ConversacionChatbot.EstadoConversacion.ACTIVA)
            .build();
}
```
Evitan repetir bloques `builder(...)` en cada test. Si cambia el modelo, solo se actualiza el fixture.

**verify con objeto exacto vs any():**
```java
verify(repositorioConversacion).guardar(conversacion);  // más preciso
verify(repositorioConversacion).guardar(any());          // más permisivo
```

---

## Coverage con JaCoCo

**Juanes:** Ahora quiero revisar el coverage de la aplicación

**Configuración agregada al `build.gradle`:**
```groovy
plugins {
    id 'jacoco'
}

tasks.named('test') {
    useJUnitPlatform()
    finalizedBy jacocoTestReport
}

jacocoTestReport {
    dependsOn test
    reports {
        html.required = true
    }
}
```

**Archivo generado:** `build/reports/jacoco/test/html/index.html`

**Resultado final:**

| Servicio | Instrucciones | Ramas |
|---|---|---|
| `ServicioAutenticacion` | 100% | 100% |
| `ServicioHistorial` | 100% | n/a |
| `ServicioSimulacion` | 100% | 100% |
| `ServicioChatbot` | 95% | 50% ← falso negativo de JaCoCo con lambdas |

**Por qué el total del proyecto es bajo (27%):** JaCoCo cuenta también controladores, DTOs, configuración de seguridad, repositorios JPA — código de infraestructura que no tiene lógica de negocio propia y no se testea unitariamente. El número relevante es el de `aplicacion.servicio`.

**Regla real:** Testea unitariamente lo que tiene lógica de negocio propia. La infraestructura se cubre con tests de integración o no se cubre.

**Lombok config agregado** (`lombok.config`):
```
config.stopBubbling = true
lombok.addLombokGeneratedAnnotation = true
```

---

## Resumen de lo construido

| Servicio | Tests | Conceptos introducidos |
|---|---|---|
| `ServicioSimulacion` | 9 | AAA puro, assertThrows, assertDoesNotThrow, @BeforeEach, valor exacto |
| `ServicioHistorial` | 4 | @Mock, @InjectMocks, when/thenReturn, verify |
| `ServicioAutenticacion` | 8 | Múltiples mocks, Optional, booleanos, assertThatThrownBy, anyString() |
| `ServicioChatbot` | 11 | Fixtures, thenReturn encadenado, métodos privados por puerta pública |
| **Total** | **32** | |

**Pirámide de testing en empresas reales:**
```
        /\
       /  \  Tests E2E (pocos, lentos, costosos)
      /----\
     /      \  Tests de integración (@WebMvcTest, @DataJpaTest)
    /--------\
   /          \  Tests unitarios ← lo construido en esta sesión
  /____________\  (muchos, rápidos, baratos — base obligatoria)
```
