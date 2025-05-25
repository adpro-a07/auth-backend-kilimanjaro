package id.ac.ui.cs.advprog.kilimanjaro.messaging;

import id.ac.ui.cs.advprog.kilimanjaro.config.RabbitSubscriberConfig;
import id.ac.ui.cs.advprog.kilimanjaro.messaging.events.RepairOrderCompletedEvent;
import id.ac.ui.cs.advprog.kilimanjaro.service.TechnicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RepairCompletedListener {

    private final TechnicianService technicianService;

    @RabbitListener(queues = RabbitSubscriberConfig.QUEUE_NAME)
    public void handleRepairCompleted(RepairOrderCompletedEvent event) {
        technicianService.updateStats(
                event.getTechnicianId(),
                event.getAmount()
        );
    }
}

