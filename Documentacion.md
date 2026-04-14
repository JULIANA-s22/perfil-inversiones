<div align="center">

# 💼 Portafolio de Inversiones

**Plataforma de simulación y análisis de perfiles de inversión para pensión voluntaria**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=for-the-badge&logo=amazon-s3&logoColor=white)](https://aws.amazon.com/s3/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

[Funcionalidades](#-funcionalidades) · [Arquitectura](#-arquitectura) · [API Reference](#-api-reference) · [Instalación](#-instalación) · [Tests](#-tests)

</div>

---

## 📋 Descripción

API REST que permite a los usuarios simular el crecimiento de su capital bajo tres perfiles de riesgo, consultar un asistente financiero con IA, exportar reportes PDF a la nube y recibir resultados por correo electrónico. Diseñada con **arquitectura hexagonal** para mantener el dominio de negocio desacoplado de frameworks y tecnologías externas.

---

## ✨ Funcionalidades

| Módulo | Descripción |
|--------|-------------|
| 🔐 **Autenticación** | Registro y login con JWT (access + refresh token), encriptación BCrypt |
| 📊 **Simulación** | Proyección de interés compuesto para perfiles conservador (3% TEA), moderado (7% TEA) y agresivo (11% TEA) |
| 🤖 **Chatbot IA** | Asistente financiero con Gemini AI — chat libre y cuestionario de perfil de riesgo |
| 📁 **Almacenamiento** | Exportación de reportes PDF a AWS S3 mediante URLs presignadas |
| 📧 **Notificaciones** | Envío asíncrono de resultados por correo vía RabbitMQ + SMTP |
| 📜 **Historial** | Registro persistente de cada simulación ejecutada por usuario |

---

## 🏗 Arquitectura

El proyecto implementa **Clean Architecture (Hexagonal / Ports & Adapters)**, garantizando que la lógica de negocio permanezca independiente de la infraestructura.

```
src/main/java/com/portafolio/
│
├── 🟢 dominio/                        # Núcleo — cero dependencias externas
│   ├── modelo/                        # Entidades de negocio
│   │   ├── Usuario
│   │   ├── ResultadoSimulacion
│   │   ├── ResultadoPerfil
│   │   ├── ProyeccionAnual
│   │   ├── ConversacionChatbot
│   │   ├── MensajeChatbot
│   │   ├── TokenAutenticacion
│   │   └── UrlPresignada
│   └── puerto/
│       ├── entrada/                   # Casos de uso (interfaces)
│       │   ├── CasoUsoAutenticacion
│       │   ├── CasoUsoSimulacion
│       │   ├── CasoUsoChatbot
│       │   ├── CasoUsoDocumentos
│       │   └── CasoUsoHistorial
│       └── salida/                    # Puertos de salida (contratos)
│           ├── RepositorioUsuario
│           ├── RepositorioSimulacion
│           ├── GeneradorUrlPresignada
│           ├── ClienteChatbotIA
│           └── ProveedorToken
│
├── 🔵 aplicacion/                     # Orquestación — implementa casos de uso
│   └── servicio/
│       ├── ServicioAutenticacion
│       ├── ServicioSimulacion
│       ├── ServicioChatbot
│       ├── ServicioDocumentos
│       └── ServicioHistorial
│
└── 🟠 infraestructura/               # Detalles técnicos — frameworks y drivers
    ├── adaptador/
    │   ├── controlador/               # REST API + DTOs
    │   ├── persistencia/              # JPA + AWS S3
    │   ├── ia/                        # Cliente Gemini
    │   └── mensajeria/                # RabbitMQ productor/consumidor
    └── configuracion/
        ├── seguridad/                 # JWT Filter, BCrypt, Spring Security
        ├── objectstorage/             # AWS S3 Presigner
        └── mensajeria/                # RabbitMQ Queues & Exchanges
```

### Flujo de una simulación

```
Cliente (React)
    │
    ▼
ControladorSimulacion          ← @RestController
    │
    ▼
CasoUsoSimulacion (puerto)     ← Interface
    │
    ▼
ServicioSimulacion             ← Lógica de negocio pura
    │
    ├──▶ CasoUsoHistorial      → Persiste resultado en PostgreSQL
    │
    └──▶ ProductorNotificacion → Publica en RabbitMQ
                                      │
                                      ▼
                               ConsumidorNotificacion → Envía email vía SMTP
```

---

## 📡 API Reference

> Todos los endpoints (excepto autenticación) requieren header `Authorization: Bearer <token>`

### 🔐 Autenticación

| Método | Endpoint | Body | Descripción |
|--------|----------|------|-------------|
| `POST` | `/api/autenticacion/registro` | `{ nombreCompleto, correo, contrasena }` | Crea cuenta nueva |
| `POST` | `/api/autenticacion/inicio-sesion` | `{ correo, contrasena }` | Retorna `tokenAcceso` + `tokenRefresco` |
| `POST` | `/api/autenticacion/refresco` | `{ tokenRefresco }` | Renueva token de acceso |

### 📊 Simulación

| Método | Endpoint | Body | Descripción |
|--------|----------|------|-------------|
| `POST` | `/api/simulacion?enviarCorreo=false` | `{ capitalActual, aporteMensual, tiempoAnios }` | Calcula 3 perfiles + proyección anual |

<details>
<summary><strong>Ver ejemplo de respuesta</strong></summary>

```json
{
  "capitalActual": 60000000,
  "aporteMensual": 1800000,
  "tiempoAnios": 25,
  "conservador": {
    "nombre": "Conservador",
    "tasaAnual": 3.0,
    "valorFuturo": 213619805,
    "etiqueta": "ESTABILIDAD 3% TEA"
  },
  "moderado": {
    "nombre": "Moderado",
    "tasaAnual": 7.0,
    "valorFuturo": 251696029,
    "etiqueta": "EQUILIBRIO 7% TEA"
  },
  "agresivo": {
    "nombre": "Agresivo",
    "tasaAnual": 11.0,
    "valorFuturo": 298157677,
    "etiqueta": "CRECIMIENTO 11% TEA"
  },
  "proyeccionConservador": [
    { "anio": 0, "pasivo": 60000000, "proyectado": 60000000 },
    { "anio": 1, "pasivo": 81600000, "proyectado": 83442716 }
  ],
  "proyeccionModerado": [ "..." ],
  "proyeccionAgresivo": [ "..." ]
}
```

</details>

### 🤖 Chatbot IA

| Método | Endpoint | Body | Descripción |
|--------|----------|------|-------------|
| `POST` | `/api/chatbot/conversaciones/chat` | `{ usuarioId }` | Inicia conversación libre |
| `POST` | `/api/chatbot/conversaciones/cuestionario` | `{ usuarioId }` | Inicia cuestionario de perfil |
| `POST` | `/api/chatbot/conversaciones/{id}/mensajes` | `{ contenido }` | Envía mensaje al chat |
| `POST` | `/api/chatbot/conversaciones/{id}/responder` | `{ contenido }` | Responde pregunta del cuestionario |
| `GET`  | `/api/chatbot/conversaciones/{id}/mensajes` | — | Historial de mensajes |
| `GET`  | `/api/chatbot/conversaciones/usuario/{id}` | — | Lista conversaciones del usuario |

### 📁 Almacenamiento

| Método | Endpoint | Params | Descripción |
|--------|----------|--------|-------------|
| `POST` | `/api/almacenamiento/presigned-url` | `nombreArchivo`, `tipoContenido` | URL presignada para subir PDF a S3 |

### 📜 Historial

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/historial` | Simulaciones previas del usuario autenticado |

---

## 🛠 Stack Tecnológico

| Categoría | Tecnología |
|-----------|------------|
| **Runtime** | Java 21 (Amazon Corretto) |
| **Framework** | Spring Boot 3.3.5 |
| **Seguridad** | Spring Security + JWT (jjwt 0.12.6) |
| **Persistencia** | Spring Data JPA / Hibernate → PostgreSQL (Neon) |
| **Mensajería** | Spring AMQP → RabbitMQ (CloudAMQP) con SSL |
| **IA** | Google Gemini API (`gemini-2.0-flash`) |
| **Almacenamiento** | AWS SDK v2 → S3 (URLs presignadas) |
| **Correo** | Spring Mail → SMTP Gmail |
| **Build** | Gradle 8.x |
| **Testing** | JUnit 5 + Mockito + JaCoCo |
| **Contenedor** | Docker (multi-stage build) |
| **Deploy** | Render |

---

## 🚀 Instalación

### Pre-requisitos

- Java 21+
- Gradle 8+
- PostgreSQL (o cuenta en [Neon](https://neon.tech))
- Cuenta en [CloudAMQP](https://www.cloudamqp.com/) para RabbitMQ
- API Key de [Google AI Studio](https://aistudio.google.com/apikeys) para Gemini
- Bucket en AWS S3
- App Password de Gmail ([generar aquí](https://myaccount.google.com/apppasswords))

### Variables de entorno

Crear archivo `.env` en la raíz del proyecto:

```env
# ── Base de Datos ──────────────────────────────
DATABASE_URL=jdbc:postgresql://host/db?sslmode=require
DATABASE_USERNAME=usuario
DATABASE_PASSWORD=contraseña

# ── RabbitMQ ───────────────────────────────────
RABBITMQ_HOST=host.rmq.cloudamqp.com
RABBITMQ_PORT=5671
RABBITMQ_USERNAME=usuario
RABBITMQ_PASSWORD=contraseña
RABBITMQ_VHOST=vhost

# ── SMTP (Gmail) ──────────────────────────────
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=correo@gmail.com
SMTP_PASSWORD=xxxx-xxxx-xxxx-xxxx

# ── Gemini AI ─────────────────────────────────
GEMINI_API_KEY=tu-api-key
GEMINI_MODELO=gemini-2.0-flash

# ── JWT ───────────────────────────────────────
JWT_SECRET=tu-clave-secreta-segura

# ── AWS S3 ────────────────────────────────────
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_KEY=tu-secret-key
AWS_REGION=us-east-1
AWS_S3_BUCKET=nombre-del-bucket

# ── CORS ──────────────────────────────────────
CORS_ORIGINS=http://localhost:5173

# ── Servidor ──────────────────────────────────
PORT=8080
```

### Ejecución local

```bash
# Clonar repositorio
git clone https://github.com/tu-usuario/portafolio-inversiones.git
cd portafolio-inversiones

# Ejecutar
./gradlew bootRun
```

### Docker

```bash
# Construir imagen
docker build -t portafolio-inversiones .

# Ejecutar contenedor
docker run -p 8080:8080 --env-file .env portafolio-inversiones
```

---

## 🧪 Tests

Pruebas unitarias con **JUnit 5 + Mockito** y cobertura con **JaCoCo**.

| Suite | Cobertura |
|-------|-----------|
| `ServicioAutenticacionTest` | Registro, login, refresco de token, validaciones |
| `ServicioSimulacionTest` | Cálculos de interés compuesto, rangos inválidos |
| `ServicioChatbotTest` | Flujos de chat libre y cuestionario |
| `ServicioDocumentosTest` | Generación de URLs presignadas, validación de tipo |
| `ServicioHistorialTest` | Persistencia y consulta de simulaciones |

```bash
# Ejecutar tests
./gradlew test

# Generar reporte de cobertura
./gradlew jacocoTestReport
# → build/reports/jacoco/test/html/index.html
```

---

## 📐 Modelo de Negocio

### Fórmula de interés compuesto

```
VF = C × (1 + r/12)^(n×12) + A × [((1 + r/12)^(n×12) - 1) / (r/12)]
```

| Variable | Descripción |
|----------|-------------|
| `C` | Capital actual |
| `A` | Aporte mensual |
| `r` | Tasa efectiva anual (0.03, 0.07 o 0.11) |
| `n` | Horizonte en años (1–50) |
| `VF` | Valor futuro proyectado |

### Perfiles de riesgo

| Perfil | TEA | Estrategia |
|--------|-----|------------|
| 🔵 Conservador | 3% | Renta fija, CDTs, bonos del gobierno |
| 🟡 Moderado | 7% | Mix renta fija + variable, fondos balanceados |
| 🔴 Agresivo | 11% | Renta variable, acciones, ETFs de alto crecimiento |

---

<div align="center">

Desarrollado con ☕ Java 21 + Spring Boot

**[⬆ Volver arriba](#-portafolio-de-inversiones)**

</div>
