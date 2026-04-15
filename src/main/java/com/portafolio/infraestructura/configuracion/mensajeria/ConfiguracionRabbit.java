package com.portafolio.infraestructura.configuracion.mensajeria;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionRabbit {
    public static final String COLA_NOTIFICACIONES = "cola.notificaciones.simulacion";
    public static final String EXCHANGE = "exchange.simulacion";
    public static final String ROUTING_KEY = "simulacion.correo";

    @Bean
    public Queue colaNotificaciones() {
        return new Queue(COLA_NOTIFICACIONES, true);
    }

    @Bean
    public TopicExchange exchangeSimulacion() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue colaNotificaciones, TopicExchange exchangeSimulacion) {
        return BindingBuilder.bind(colaNotificaciones).to(exchangeSimulacion).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter convertidorJson() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(convertidorJson());
        return template;
    }
}