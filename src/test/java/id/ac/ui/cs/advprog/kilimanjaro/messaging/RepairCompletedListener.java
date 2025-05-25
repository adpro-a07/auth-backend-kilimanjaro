package id.ac.ui.cs.advprog.kilimanjaro.messaging;

import id.ac.ui.cs.advprog.kilimanjaro.messaging.events.RepairOrderCompletedEvent;
import id.ac.ui.cs.advprog.kilimanjaro.service.TechnicianService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairCompletedListenerTest {

    @Mock
    private TechnicianService technicianService;

    @InjectMocks
    private RepairCompletedListener repairCompletedListener;

    private UUID technicianId;
    private Long amount;
    private RepairOrderCompletedEvent event;

    @BeforeEach
    void setUp() {
        technicianId = UUID.randomUUID();
        amount = 5000L;
        event = RepairOrderCompletedEvent.builder()
                .repairOrderId(UUID.randomUUID())
                .technicianId(technicianId)
                .amount(amount)
                .completedAt(Instant.now())
                .build();
    }

    @Test
    void handleRepairCompleted_ShouldCallTechnicianServiceUpdateStats() {
        // Act
        repairCompletedListener.handleRepairCompleted(event);

        // Assert
        verify(technicianService, times(1)).updateStats(technicianId, amount);
    }

    @Test
    void handleRepairCompleted_WithZeroAmount_ShouldStillCallUpdateStats() {
        // Arrange
        RepairOrderCompletedEvent zeroAmountEvent = RepairOrderCompletedEvent.builder()
                .repairOrderId(UUID.randomUUID())
                .technicianId(technicianId)
                .amount(0L)
                .completedAt(Instant.now())
                .build();

        // Act
        repairCompletedListener.handleRepairCompleted(zeroAmountEvent);

        // Assert
        verify(technicianService, times(1)).updateStats(technicianId, 0L);
    }

    @Test
    void handleRepairCompleted_WithNegativeAmount_ShouldStillCallUpdateStats() {
        // Arrange
        RepairOrderCompletedEvent negativeAmountEvent = RepairOrderCompletedEvent.builder()
                .repairOrderId(UUID.randomUUID())
                .technicianId(technicianId)
                .amount(-500L)
                .completedAt(Instant.now())
                .build();

        // Act
        repairCompletedListener.handleRepairCompleted(negativeAmountEvent);

        // Assert
        verify(technicianService, times(1)).updateStats(technicianId, -500L);
    }

    @Test
    void handleRepairCompleted_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        doThrow(new IllegalArgumentException("Technician not found")).when(technicianService)
                .updateStats(technicianId, amount);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            repairCompletedListener.handleRepairCompleted(event)
        );

        assertEquals("Technician not found", exception.getMessage());
        verify(technicianService, times(1)).updateStats(technicianId, amount);
    }

    @Test
    void handleRepairCompleted_WithNullTechnicianId_ShouldPropagateException() {
        // Arrange
        RepairOrderCompletedEvent nullTechnicianEvent = RepairOrderCompletedEvent.builder()
                .repairOrderId(UUID.randomUUID())
                .technicianId(null)
                .amount(amount)
                .completedAt(Instant.now())
                .build();

        doThrow(new NullPointerException()).when(technicianService).updateStats(null, amount);

        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            repairCompletedListener.handleRepairCompleted(nullTechnicianEvent)
        );

        verify(technicianService, times(1)).updateStats(null, amount);
    }

    @Test
    void handleRepairCompleted_WithNullAmount_ShouldPropagateException() {
        // Arrange
        RepairOrderCompletedEvent nullAmountEvent = RepairOrderCompletedEvent.builder()
                .repairOrderId(UUID.randomUUID())
                .technicianId(technicianId)
                .amount(null)
                .completedAt(Instant.now())
                .build();
        doThrow(new NullPointerException()).when(technicianService).updateStats(technicianId, null);

        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            repairCompletedListener.handleRepairCompleted(nullAmountEvent)
        );

        verify(technicianService, times(1)).updateStats(technicianId, null);
    }

    @Test
    void handleRepairCompleted_WithNullEvent_ShouldThrowNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            repairCompletedListener.handleRepairCompleted(null)
        );

        // Verify that technicianService is never called with null event
        verify(technicianService, never()).updateStats(any(), any());
    }

    @Test
    void handleRepairCompleted_WithMaxLongAmount_ShouldCallUpdateStats() {
        // Arrange
        RepairOrderCompletedEvent maxAmountEvent = RepairOrderCompletedEvent.builder()
                .repairOrderId(UUID.randomUUID())
                .technicianId(technicianId)
                .amount(Long.MAX_VALUE)
                .completedAt(Instant.now())
                .build();

        // Act
        repairCompletedListener.handleRepairCompleted(maxAmountEvent);

        // Assert
        verify(technicianService, times(1)).updateStats(technicianId, Long.MAX_VALUE);
    }
}