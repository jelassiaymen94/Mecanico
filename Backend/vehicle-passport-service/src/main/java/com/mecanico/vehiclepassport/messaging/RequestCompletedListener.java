package com.mecanico.vehiclepassport.messaging;

import com.mecanico.vehiclepassport.service.MaintenanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** RequestCompleted (MS2) -&gt; automatic entry in the vehicle maintenance history. */
@Component
public class RequestCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(RequestCompletedListener.class);

    private final MaintenanceService maintenanceService;

    public RequestCompletedListener(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @RabbitListener(queues = MessagingConfig.REQUEST_COMPLETED_QUEUE)
    public void onRequestCompleted(Events.RequestCompletedEvent event) {
        log.info("Received {} for request {} (vehicle {})",
                Events.REQUEST_COMPLETED, event.requestId(), event.vehicleId());
        maintenanceService.recordFromCompletedRequest(event);
    }
}
