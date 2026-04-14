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

    @Value("${resend.from:Proyeccion <onboarding@resend.dev>}")
    private String fromEmail;

    public ConsumidorNotificacion(@Value("${resend.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
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
                    "from", fromEmail,
                    "to", new String[]{mensaje.getCorreoDestino()},
                    "subject", "Proyección - Resultado de tu Simulación",
                    "text", cuerpo
            );

            restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Correo enviado via Resend a: {}", mensaje.getCorreoDestino());
        } catch (Exception e) {
            log.error("Error enviando correo via Resend: {}", e.getMessage());
        }
    }
}