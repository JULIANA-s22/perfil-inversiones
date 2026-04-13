# Sesión 03 — Integración AWS S3 con Presigned URLs
**Fecha:** 2026-04-13

---

## Prompt inicial

**Juanes:** Integración de AWS S3 para subida de PDFs desde el frontend. ROL: Eres un ingeniero senior de backend con experiencia en Java/Spring Boot y AWS. Tu rol secundario es el de mentor técnico: nunca avanzas más rápido de lo que yo puedo entender. Cada decisión que tomes, la explicas antes de ejecutarla. [...] Un bloque por turno. Sin excepciones.

---

## Fase 1 — Qué vamos a construir

**Bloque 1 — El flujo completo**

```
Frontend                Backend (Spring Boot)           AWS S3
   │                           │                           │
   │── POST /api/s3/presigned ─▶│                           │
   │   (nombre del archivo)     │── solicita URL firmada ──▶│
   │                           │◀─ devuelve Presigned URL ──│
   │◀─ { url, key } ───────────│                           │
   │                           │                           │
   │─────── PUT directo ────────────────────────────────────▶│
   │        (el PDF va directo, NO pasa por el backend)      │
   │◀─────── 200 OK ──────────────────────────────────────── │
```

**Juanes:** Si para no cargar el backend sino que cada cliente sube sus fotos.

**Concepto clave — Presigned URL:** URL temporal con firma criptográfica del backend incluida. Quien la tenga puede subir un archivo directamente a S3 durante un tiempo limitado (15 min), sin credenciales de AWS. El backend actúa como guardia de seguridad — verifica permiso y da el pase. A partir de ahí el cliente trata directamente con S3.

---

**Bloque 2 — Las 4 piezas del sistema**

1. **Bucket S3** — donde viven los archivos (carpeta raíz en la nube)
2. **Credenciales IAM** — Access Key ID + Secret Access Key para que el backend hable con AWS
3. **SDK de AWS** — librería Java para conectarse a S3
4. **Endpoint REST** — el único código nuevo en Spring Boot

**Concepto clave — IAM:** Sistema de permisos de AWS. Principio de mínimo privilegio: el usuario IAM solo tiene permiso para lo estrictamente necesario.

---

**Bloque 3 — Mapa de configuración AWS**

```
1. Crear Bucket S3
        │
        ▼
2. Crear usuario IAM con permisos
        │
        ▼
3. Generar Access Keys
```

---

## Fase 2 — Configuración AWS (paso a paso)

**Bucket S3:**
- Nombre globalmente único en minúsculas
- Block Public Access: dejar activado — las Presigned URLs funcionan igual
- Region: anotar para usarla en Spring Boot

**Usuario IAM:**
- No marcar acceso a consola — es solo para el backend
- Policy: `AmazonS3FullAccess` para empezar (restringir después por bucket)

**Access Keys:**
- Caso de uso: "Application running outside AWS"
- El Secret se muestra **una sola vez** — copiarlo inmediatamente
- **Nunca** van en código ni en Git

**Variables de entorno en `.env`:**
```env
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=xxx...
AWS_REGION=us-east-1
AWS_S3_BUCKET=nombre-del-bucket
```

**`application.yml`:**
```yaml
aws:
  accessKeyId: ${AWS_ACCESS_KEY_ID}
  secretKey: ${AWS_SECRET_ACCESS_KEY}
  region: ${AWS_REGION}
  s3:
    bucket: ${AWS_S3_BUCKET}
```

---

## Fase 3 — Implementación Spring Boot

**Dependencias agregadas al `build.gradle`:**
```groovy
implementation platform('software.amazon.awssdk:bom:2.25.6')
implementation 'software.amazon.awssdk:s3'
```
El BOM define versiones compatibles de todos los módulos AWS. Solo se importa el módulo S3.

---

**Concepto — Bean (Singleton):**

**Juanes:** Es un singleton?

**Respuesta:** Exactamente. Por defecto todos los beans de Spring son singleton. `@Bean` define el objeto una vez al arrancar la app. Spring lo gestiona y lo inyecta donde se necesite — como la cafetera de una oficina, no tiene sentido que cada empleado traiga la suya.

```
App arranca → Spring crea S3Presigner (una vez)
                        │
Request 1 ──────────────┤
Request 2 ──────────────┤── todos usan el mismo objeto
Request 3 ──────────────┘
```

---

**Arquitectura hexagonal aplicada:**

**Juanes:** Pero uno parte del dominio.

```
dominio/puerto/entrada/    CasoUsoDocumentos.java       ← qué puede pedir el exterior
dominio/puerto/salida/     GeneradorUrlPresignada.java  ← qué necesita de afuera
dominio/modelo/            UrlPresignada.java           ← modelo de dominio

aplicacion/servicio/       ServicioDocumentos.java      ← lógica, usa el puerto de salida

infraestructura/adaptador/
  persistencia/            ClienteS3.java               ← implementa con AWS
  controlador/             ControladorAlmacenamiento.java
infraestructura/configuracion/
  objectstorage/           ConfiguracionS3.java         ← beans de AWS
```

**Regla respetada:** El dominio no sabe que existe AWS. Si mañana cambias S3 por Google Cloud Storage, solo cambias `ClienteS3` — el dominio no se toca.

---

**Archivos creados:**

`CasoUsoDocumentos.java`
```java
public interface CasoUsoDocumentos {
    UrlPresignada obtenerUrlSubida(String nombreArchivo, String tipoContenido);
}
```

`GeneradorUrlPresignada.java`
```java
public interface GeneradorUrlPresignada {
    UrlPresignada generarUrlSubida(String nombreArchivo, String tipoContenido);
}
```

`UrlPresignada.java` — por qué se creó este modelo:

**Juanes:** Me pregunto porque no hay una clase de esto dentro de modelo.

**Respuesta:** Sin el modelo, el controlador construía el `Map` con `"pdfs/" + nombreArchivo` — conocimiento del negocio viviendo en infraestructura. Con `UrlPresignada`, ese conocimiento vive en el dominio y el controlador solo serializa lo que recibe.

```java
public record UrlPresignada(String url, String key) {}
```

`ServicioDocumentos.java`
```java
@Service
@RequiredArgsConstructor
public class ServicioDocumentos implements CasoUsoDocumentos {
    private final GeneradorUrlPresignada generadorUrlPresignada;

    @Override
    public UrlPresignada obtenerUrlSubida(String nombreArchivo, String tipoContenido) {
        if (!tipoContenido.equals("application/pdf")) {
            throw new IllegalArgumentException("Solo se permiten archivos PDF");
        }
        return generadorUrlPresignada.generarUrlSubida(nombreArchivo, tipoContenido);
    }
}
```

`ClienteS3.java` — único archivo que sabe que existe AWS:
```java
@Override
public UrlPresignada generarUrlSubida(String nombreArchivo, String tipoContenido) {
    String key = "pdfs/" + nombreArchivo;

    PutObjectRequest objectRequest = PutObjectRequest.builder()
            .bucket(bucket).key(key).contentType(tipoContenido).build();

    PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(15))  // expira en 15 minutos
            .putObjectRequest(objectRequest).build();

    PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
    return new UrlPresignada(presignedRequest.url().toString(), key);
}
```

`ConfiguracionS3.java` — solo `S3Presigner`, no `S3Client` porque solo necesitamos generar URLs:
```java
@Bean
public S3Presigner s3Presigner() {
    AwsBasicCredentials credenciales = AwsBasicCredentials.create(accessKeyId, secretKey);
    return S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credenciales))
            .build();
}
```

**Endpoint resultado:**
```
POST /api/almacenamiento/presigned-url?nombreArchivo=reporte.pdf

Respuesta:
{
  "url": "https://s3.amazonaws.com/...?X-Amz-Signature=...",
  "key": "pdfs/reporte.pdf"
}
```

**Seguridad:** El endpoint queda protegido por JWT automáticamente por `.anyRequest().authenticated()` — no requiere cambios en `ConfiguracionSeguridad`.

---

## Decisiones de naming

**Juanes:** No me gusta que almacenamiento no suena a objectstorage.

- Carpeta de configuración: `objectstorage/` — concepto técnico (no la tecnología S3)
- Servicio: `ServicioDocumentos` — lenguaje del negocio, no de la tecnología
- Puerto entrada: `CasoUsoDocumentos`

**Juanes:** No me gusta que pusiste una carpeta llamada almacenamiento cuando había persistencia / dentro de configuracion pusiste un archivo sin carpeta.

Patrón corregido:
```
configuracion/
  mensajeria/        ConfiguracionRabbit.java   ← carpeta = función, archivo = tecnología
  seguridad/         ConfiguracionSeguridad.java
  objectstorage/     ConfiguracionS3.java        ✅
```

---

## Tests escritos — ServicioDocumentos

```java
@ExtendWith(MockitoExtension.class)
class ServicioDocumentosTest {

    @Mock GeneradorUrlPresignada generadorUrlPresignada;
    @InjectMocks ServicioDocumentos servicio;

    @Test
    void obtenerUrlSubida_debeRetornarUrlPresignada_cuandoTipoEsPdf() { ... }

    @Test
    void obtenerUrlSubida_debeLanzarExcepcion_cuandoTipoNoEsPdf() { ... }

    @Test
    void obtenerUrlSubida_debeDelegarAlGeneradorConLosParametrosCorrectos() { ... }
}
```

**Total tests del proyecto: 35** — BUILD SUCCESSFUL.
