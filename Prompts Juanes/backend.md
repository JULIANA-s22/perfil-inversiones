# Prompts — Portafolio de Inversiones

Guía de prompts utilizados para construir el backend 

---

## Prompt 1 — Autenticación JWT con Arquitectura Hexagonal

> Necesito crear un backend en
> Java 21 con Spring Boot 3.3.5, Gradle y arquitectura hexagonal
> (puertos y adaptadores). El proyecto se llama "portafolio-inversiones" 
> y la primera funcionalidad es autenticación con JWT. Necesito: registro de usuario (nombre, correo, contraseña con BCrypt),
> inicio de sesión que retorne tokenAcceso , endpoint para refrescar token, y filtro JWT que proteja todas las rutas excepto `/api/autenticacion/**`.



## Prompt 2 — Simulación de Perfiles de Inversión

> Sobre el backend de portafolio-inversiones con arquitectura hexagonal, necesito agregar el módulo de simulación de inversión. Debe recibir por `POST /api/simulacion`: capitalActual, aporteMensual y tiempoAnios (1–50). 
> Calcula el valor futuro con interés compuesto mensual para 3 perfiles: 
> Conservador (3% TEA), Moderado (7% TEA) y Agresivo (11% TEA). Fórmula: `VF = Capital × (1+r/12)^(n×12) + Aporte × [((1+r/12)^(n×12) - 1) / (r/12)]`.
> Debe retornar los 3 resultados con nombre, tasa, valorFuturo, etiqueta, y la proyección año a año (pasivo vs proyectado) para cada perfil. El endpoint debe estar protegido con JWT y guardar cada simulación en el historial del usuario.


---

## Prompt 3 — Historial de Simulaciones

> Agrega al backend el módulo de historial de simulaciones. Necesito una entidad `RegistroSimulacion` que persista: id, correo del usuario, capitalActual, aporteMensual, tiempoAnios, valorConservador, valorModerado, valorAgresivo, y fecha de creación. Puerto de entrada `CasoUsoHistorial` con métodos `guardar()` y `obtenerHistorial(correoUsuario)`. El endpoint `GET /api/historial` retorna la lista de simulaciones del usuario autenticado. 
> Cada vez que se ejecute una simulación, automáticamente se guarda en el historial desde el `ControladorSimulacion`.



## Prompt 5 — Notificaciones por Correo con RabbitMQ

> Agrega notificaciones asíncronas por correo electrónico al backend. 
> Cuando el usuario ejecute una simulación con el parámetro `enviarCorreo=true`, 
> el controlador debe publicar un mensaje en una cola de RabbitMQ (CloudAMQP con SSL en puerto 5671). 
> Un consumidor escucha la cola y envía el correo con los resultados de la simulación.
> Necesito: `ConfiguracionRabbit` con declaración de Queue, TopicExchange, Binding, y conversor Jackson2Json. 
> `ProductorNotificacion` que publique `MensajeSimulacion`. `ConsumidorNotificacion` que consuma y envíe el correo usando la API HTTP de Resend (no SMTP, porque Railway bloquea puertos SMTP).
> El correo debe incluir capital, aporte, tiempo y los valores de los 3 perfiles formateados en pesos colombianos (COP).

---


---

## Prompt 9 — Dockerfile y Despliegue

> Crea un Dockerfile multi-stage para el backend:
> primera etapa con Amazon Corretto 21 que ejecuta `./gradlew bootJar`,
> segunda etapa que copia el JAR y expone puerto 8080. El `application.yml` 
> debe usar variables de entorno .


---


