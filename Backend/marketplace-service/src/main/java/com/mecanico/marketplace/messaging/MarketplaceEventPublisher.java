package com.mecanico.marketplace.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Publishes marketplace events. A broker outage must never roll back an order:
 * failures are logged only.
 */
@Component
public class MarketplaceEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public MarketplaceEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void orderConfirmed(MarketplaceEvents.OrderConfirmedEvent event) {
        try {
            rabbitTemplate.convertAndSend(MarketplaceEvents.EXCHANGE, MarketplaceEvents.ORDER_CONFIRMED, event);
            log.info("Published {} for order {}", MarketplaceEvents.ORDER_CONFIRMED, event.orderId());
        } catch (AmqpException e) {
            log.warn("RabbitMQ unavailable, {} not published for order {}: {}",
                    MarketplaceEvents.ORDER_CONFIRMED, event.orderId(), e.getMessage());
        }
    }

    @Configuration
    static class MessagingConfig {
        @Bean
        TopicExchange mecanicoEventsExchange() {
            return new TopicExchange(MarketplaceEvents.EXCHANGE, true, false);
        }

        @Bean
        MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
            Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
            converter.setAlwaysConvertToInferredType(true);
            return converter;
        }
    }
}
