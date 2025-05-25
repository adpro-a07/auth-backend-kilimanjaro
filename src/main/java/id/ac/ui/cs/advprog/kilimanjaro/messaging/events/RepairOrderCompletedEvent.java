package id.ac.ui.cs.advprog.kilimanjaro.messaging.events;

import lombok.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class RepairOrderCompletedEvent implements Serializable {

    @NotNull(message = "Repair order ID must not be null")
    private UUID repairOrderId;

    @NotNull(message = "Technician ID must not be null")
    private UUID technicianId;

    @NotNull(message = "Amount must not be null")
    @Positive(message = "Amount must be positive")
    private Long amount;

    @NotNull(message = "CompletedAt timestamp must not be null")
    @PastOrPresent(message = "CompletedAt must be in the past or present")
    private Instant completedAt;
}