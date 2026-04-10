# Sesión 01 — Clean Architecture + PDF Feature
**Fecha:** 2026-04-10

---

## Arquitectura del repositorio

**Juanes:** Haz lo siguiente EN ESTE ORDEN: Mapa del repositorio, capas identificadas, flujo de una request, rol del JWT, puntos de extensión.

**Respuesta clave:** El repo tiene 3 capas nombradas `dominio`, `aplicacion`, `infraestructura`. Los puertos de salida son la Regla de Dependencia en acción — el dominio declara las interfaces, la infraestructura las implementa.

---

## Modelo vs Repositorio

**Juanes:** Entonces modelo trata al objeto en java en si, que es un espejo de la tabla en base de datos, el repositorio es el camino a la bd y es abstracto por si se cambia la BD después y finalmente se implementa en infraestructura.

**Respuesta clave:** Exacto. El modelo = el objeto Java con sus datos y comportamiento. El repositorio = contrato abstracto para llegar a la BD. La implementación = infraestructura.

---

## Métodos de negocio en el modelo

**Juanes:** Si es una cola la que gestiona de forma asíncrona lo de la generación del pdf y se manda al correo. Eso como entraría a esto del modelo?

**Respuesta clave:** La cola y el correo son servicios externos — se modelan como puertos de salida igual que el repositorio. El modelo ya soporta el ciclo asíncrono con sus estados: PENDIENTE → PROCESANDO → COMPLETADO / ERROR.

---

## Por qué los métodos van en el modelo y no en el repositorio

**Juanes:** Si me queda mas claro, lo que no me queda claro es porque esos métodos adicionales a crear, porque están en modelo y no en repositorio.

**Respuesta clave:** El repositorio solo sabe hablar con la BD. `completar()`, `iniciarProcesamiento()`, `marcarError()` son acciones que le pasan AL PDF como objeto — son comportamiento del negocio, no de la base de datos. Regla: si la acción cambia el estado del objeto → modelo. Si toca la BD → repositorio.

---

## Entradas y salidas

**Juanes:** lo que me queda faltando por entender es entradas y salidas.

**Respuesta clave:**
- **Salida** = el dominio necesita algo del exterior (BD, correo, cola)
- **Entrada** = el exterior necesita algo del dominio (HTTP le dice al dominio "registra este usuario")

```
[HTTP] → puerto.ENTRADA → [Dominio + modelo] → puerto.SALIDA → [BD / Correo / Cola]
```

---

## El front como entrada

**Juanes:** Entonces prácticamente lo que necesite un front es todo una entrada para el negocio.

**Respuesta clave:** Exacto. Todo lo que el front consume es una entrada. Cada endpoint del Controller va a tener detrás su CasoUso correspondiente.

---

## Flujo asíncrono del PDF

**Juanes:** Bueno pero empecemos desde cero y tu a través de preguntas me vas guiando a que entienda como hacer todo lo del pdf. La cola actualiza la BD por otro lado o es mejor centralizarlo aquí?

**Respuesta clave:** Todo pasa por el dominio, incluso el worker de la cola. El worker no es especial — es otro adaptador igual que el Controller. La diferencia es de dónde recibe el mensaje: Controller escucha HTTP, Worker escucha la Cola. Ambos llaman al mismo CasoUso.

---

## Cómo Spring sabe qué implementación inyectar

**Juanes:** Me queda es la duda es como el controlador sabe que la implementación del caso de uso de gestión es justo servicio gestión, o como es la magia para que eso funcione.

**Respuesta clave:** La magia la hace Spring con inyección de dependencias. `@Service` registra la clase en el contenedor de Spring. Cuando el Controller pide un `CasoUsoGestionPdf`, Spring busca qué clase lo implementa y la inyecta automáticamente. En Clean Architecture normalmente hay una sola implementación por interfaz.

---

## Redundancia con Spring

**Juanes:** Entonces uno podría tener redundancia si uno falla e inyectar otro servicio?

**Respuesta clave:** Sí, con `@Primary` y `@Qualifier`. Para redundancia real con fallback automático se usa el patrón Circuit Breaker con Resilience4j. En la práctica la redundancia se maneja más a nivel de infraestructura (réplicas de BD) que a nivel de código.

---

## Programación reactiva

**Juanes:** Y he escuchado lo de programación reactiva que es eso?

**Respuesta clave:** Es un paradigma para manejar flujos de datos asíncronos de forma no bloqueante. Con Spring MVC normal un thread se bloquea esperando la BD. Con Spring WebFlux el thread envía la query y sigue libre. Para un proyecto CRUD normal Spring MVC está perfectamente bien — WebFlux agrega complejidad que no vale si no tienes un problema real de concurrencia.

---

## Recorrido completo de lo construido

```
dominio.modelo          → DocumentoPdf, SolicitudGeneracionPdf
dominio.puerto.entrada  → CasoUsoGestionPdf
dominio.puerto.salida   → RepositorioDocumentoPdf
aplicacion.servicio     → ServicioGestionPdf
infraestructura         → EntidadDocumentoPdfJpa
                        → RepositorioDocumentoPdfSpring
                        → AdaptadorPersistenciaDocumentoPdf
                        → ControladorPdf
                        → DTOs: PeticionSolicitudPdf, RespuestaDocumentoPdf
```
