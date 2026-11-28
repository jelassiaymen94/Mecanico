package com.mecanico.vehiclepassport.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes MS1 events. A broker outage must never break the business flow:
 * failures are logged and the in-app notification remains the source of truth.
 */
@Component
public class PassportEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PassportEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public PassportEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void documentExpiring(Events.DocumentExpiringEvent event) {
        try {
            rabbitTemplate.convertAndSend(Events.EXCHANGE, Events.DOCUMENT_EXPIRING, event);
            log.info("Published {} for document {} (vehicle {}, J-{})",
                    Events.DOCUMENT_EXPIRING, event.documentId(), event.vehicleId(), event.daysLeft());
        } catch (AmqpException e) {
            log.warn("RabbitMQ unavailable, {} not published for document {}: {}",
                    Events.DOCUMENT_EXPIRING, event.documentId(), e.getMessage());
        }
    }
}
