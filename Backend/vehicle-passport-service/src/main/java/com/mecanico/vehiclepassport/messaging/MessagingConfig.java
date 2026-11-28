package com.mecanico.vehiclepassport.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology owned by MS1. All Mecanico services share the topic exchange
 * {@value Events#EXCHANGE}; each consumer declares its own durable queue.
 */
@Configuration
public class MessagingConfig {

    public static final String REQUEST_COMPLETED_QUEUE = "vehicle-passport.request-completed";

    @Bean
    public TopicExchange mecanicoEventsExchange() {
        return new TopicExchange(Events.EXCHANGE, true, false);
    }

    @Bean
    public Queue requestCompletedQueue() {
        return QueueBuilder.durable(REQUEST_COMPLETED_QUEUE).build();
    }

    @Bean
    public Binding requestCompletedBinding(Queue requestCompletedQueue, TopicExchange mecanicoEventsExchange) {
        return BindingBuilder.bind(requestCompletedQueue).to(mecanicoEventsExchange).with(Events.REQUEST_COMPLETED);
    }

    /** JSON payloads; the target type is inferred from the listener signature (no shared classes). */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
