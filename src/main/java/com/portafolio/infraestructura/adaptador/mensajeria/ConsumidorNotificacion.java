package com.portafolio.infraestructura.adaptador.mensajeria;

import com.portafolio.infraestructura.configuracion.mensajeria.ConfiguracionRabbit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;

@Slf4j @Component @RequiredArgsConstructor
public class ConsumidorNotificacion {
    private final JavaMailSender mailSender;

    @RabbitListener(queues = ConfiguracionRabbit.COLA_NOTIFICACIONES)
    public void procesarMensaje(MensajeSimulacion mensaje) {
        log.info("Mensaje recibido de la cola para: {}", mensaje.getCorreoDestino());
        try {
            NumberFormat fmt = NumberFormat.getInstance(new Locale("es", "CO"));
            String cuerpo = String.format("""
                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    PROYECCIÓN - Resumen de Simulación
                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    
                    Capital Actual: $%s COP
                    Aporte Mensual: $%s COP
                    Tiempo: %d años
                    
                    RESULTADOS POR PERFIL:
                    Conservador (3%% TEA): $%s COP
                    Moderado (7%% TEA):    $%s COP
                    Agresivo (11%% TEA):   $%s COP
                    
                    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    Proyección - Tu Futuro, Bajo Tu Control
                    """,
                    fmt.format(mensaje.getCapitalActual()), fmt.format(mensaje.getAporteMensual()),
                    mensaje.getTiempoAnios(), fmt.format(mensaje.getValorConservador()),
                    fmt.format(mensaje.getValorModerado()), fmt.format(mensaje.getValorAgresivo()));

            SimpleMailMessage correo = new SimpleMailMessage();
            correo.setTo(mensaje.getCorreoDestino());
            correo.setSubject("Proyección - Resultado de tu Simulación");
            correo.setText(cuerpo);
            mailSender.send(correo);
            log.info("Correo enviado a: {}", mensaje.getCorreoDestino());
        } catch (Exception e) {
            log.error("Error enviando correo: {}", e.getMessage());
        }
    }
}
