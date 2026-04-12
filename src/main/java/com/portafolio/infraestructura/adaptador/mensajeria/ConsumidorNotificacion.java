package com.portafolio.infraestructura.adaptador.mensajeria;

import com.portafolio.infraestructura.configuracion.mensajeria.ConfiguracionRabbit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
public class ConsumidorNotificacion {

    private final RestClient restClient;

    @Value("${brevo.from-email}")
    private String fromEmail;

    @Value("${brevo.from-name:Proyeccion}")
    private String fromName;

    public ConsumidorNotificacion(@Value("${brevo.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .build();
    }

    @RabbitListener(queues = ConfiguracionRabbit.COLA_NOTIFICACIONES)
    public void procesarMensaje(MensajeSimulacion mensaje) {
        log.info("Mensaje recibido de la cola para: {}", mensaje.getCorreoDestino());
        try {
            NumberFormat fmt = NumberFormat.getInstance(new Locale("es", "CO"));
            String cuerpo = String.format("""
                    Capital Actual: $%s COP
                    Aporte Mensual: $%s COP
                    Tiempo: %d años

                    RESULTADOS POR PERFIL:
                    Conservador (3%% TEA): $%s COP
                    Moderado (7%% TEA):    $%s COP
                    Agresivo (11%% TEA):   $%s COP
                    """,
                    fmt.format(mensaje.getCapitalActual()), fmt.format(mensaje.getAporteMensual()),
                    mensaje.getTiempoAnios(), fmt.format(mensaje.getValorConservador()),
                    fmt.format(mensaje.getValorModerado()), fmt.format(mensaje.getValorAgresivo()));

            Map<String, Object> body = Map.of(
                    "sender", Map.of("name", fromName, "email", fromEmail),
                    "to", new Object[]{Map.of("email", mensaje.getCorreoDestino())},
                    "subject", "Proyección - Resultado de tu Simulación",
                    "textContent", cuerpo
            );

            restClient.post()
                    .uri("/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Correo enviado via Brevo a: {}", mensaje.getCorreoDestino());
        } catch (Exception e) {
            log.error("Error enviando correo via Brevo: {}", e.getMessage());
        }
    }
}