package com.portafolio.infraestructura.adaptador.mensajeria;

import com.portafolio.infraestructura.configuracion.mensajeria.ConfiguracionRabbit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j @Component @RequiredArgsConstructor
public class ProductorNotificacion {
    private final RabbitTemplate rabbitTemplate;

    public void publicarSimulacion(MensajeSimulacion mensaje) {
        log.info("Publicando simulación en cola para: {}", mensaje.getCorreoDestino());
        rabbitTemplate.convertAndSend(ConfiguracionRabbit.EXCHANGE, ConfiguracionRabbit.ROUTING_KEY, mensaje);
    }
}
