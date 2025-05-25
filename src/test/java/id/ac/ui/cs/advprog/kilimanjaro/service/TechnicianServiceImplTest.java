package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.model.Technician;
import id.ac.ui.cs.advprog.kilimanjaro.repository.TechnicianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnicianServiceImplTest {

    @Mock
    private TechnicianRepository technicianRepository;

    @InjectMocks
    private TechnicianServiceImpl technicianService;

    private UUID validTechnicianId;
    private Technician technician;

    @BeforeEach
    void setUp() {
        validTechnicianId = UUID.randomUUID();
        technician = new Technician.TechnicianBuilder()
                .id(validTechnicianId)
                .fullName("Tech Technician")
                .email("technician@example.com")
                .phoneNumber("0877123456")
                .password("encoded_password")
                .address("Jl. Teknisi No. 10")
                .experience("5 years in electronics")
                .build();

        technician.setTotalJobsDone(10);
        technician.setTotalIncome(5000L);
    }

    @Test
    void updateStats_WhenTechnicianExists_ShouldUpdateStatsCorrectly() {
        // Arrange
        Long amount = 2000L;
        when(technicianRepository.findById(validTechnicianId)).thenReturn(Optional.of(technician));

        // Act
        technicianService.updateStats(validTechnicianId, amount);

        // Assert
        ArgumentCaptor<Technician> technicianCaptor = ArgumentCaptor.forClass(Technician.class);
        verify(technicianRepository).save(technicianCaptor.capture());

        Technician updatedTechnician = technicianCaptor.getValue();
        assertEquals(11, updatedTechnician.getTotalJobsDone());
        assertEquals(7000L, updatedTechnician.getTotalIncome());
    }

    @Test
    void updateStats_WhenTechnicianDoesNotExist_ShouldThrowIllegalArgumentException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        Long amount = 2000L;
        when(technicianRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            technicianService.updateStats(nonExistentId, amount)
        );

        assertEquals("Technician not found", exception.getMessage());
        verify(technicianRepository, never()).save(any());
    }

    @Test
    void updateStats_WithZeroAmount_ShouldOnlyIncrementJobsDone() {
        // Arrange
        Long amount = 0L;
        when(technicianRepository.findById(validTechnicianId)).thenReturn(Optional.of(technician));

        // Act
        technicianService.updateStats(validTechnicianId, amount);

        // Assert
        ArgumentCaptor<Technician> technicianCaptor = ArgumentCaptor.forClass(Technician.class);
        verify(technicianRepository).save(technicianCaptor.capture());

        Technician updatedTechnician = technicianCaptor.getValue();
        assertEquals(11L, updatedTechnician.getTotalJobsDone().longValue());  // Incremented by 1
        assertEquals(5000L, updatedTechnician.getTotalIncome());  // Unchanged since amount is 0
    }

    @Test
    void updateStats_WithNegativeAmount_ShouldDecreaseTotalIncome() {
        // Arrange
        Long amount = -1000L;
        when(technicianRepository.findById(validTechnicianId)).thenReturn(Optional.of(technician));

        // Act
        technicianService.updateStats(validTechnicianId, amount);

        // Assert
        ArgumentCaptor<Technician> technicianCaptor = ArgumentCaptor.forClass(Technician.class);
        verify(technicianRepository).save(technicianCaptor.capture());

        Technician updatedTechnician = technicianCaptor.getValue();
        assertEquals(11L, updatedTechnician.getTotalJobsDone().longValue());
        assertEquals(4000L, updatedTechnician.getTotalIncome());  // Decreased by 1000
    }

    @Test
    void updateStats_WithNullAmount_ShouldThrowNullPointerException() {
        // Arrange
        when(technicianRepository.findById(validTechnicianId)).thenReturn(Optional.of(technician));

        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            technicianService.updateStats(validTechnicianId, null)
        );

        verify(technicianRepository, never()).save(any());
    }

    @Test
    void updateStats_WithNullTechnicianId_ShouldThrowNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            technicianService.updateStats(null, 1000L)
        );

        verify(technicianRepository, never()).findById(any());
        verify(technicianRepository, never()).save(any());
    }

    @Test
    void updateStats_WithExtremeValues_ShouldHandleCorrectly() {
        // Arrange
        int initialJobsDone = 999999;
        Long initialIncome = Long.MAX_VALUE - 1000L;
        Long amount = 1000L;

        Technician techWithExtremeValues = new Technician.TechnicianBuilder()
                .id(validTechnicianId)
                .fullName("Extreme Tech")
                .email("extreme@example.com")
                .phoneNumber("0877999999")
                .password("encoded_password")
                .address("Jl. Extreme No. 999")
                .experience("20 years in electronics")
                .build();

        techWithExtremeValues.setTotalJobsDone(initialJobsDone);
        techWithExtremeValues.setTotalIncome(initialIncome);

        when(technicianRepository.findById(validTechnicianId)).thenReturn(Optional.of(techWithExtremeValues));

        // Act
        technicianService.updateStats(validTechnicianId, amount);

        // Assert
        ArgumentCaptor<Technician> technicianCaptor = ArgumentCaptor.forClass(Technician.class);
        verify(technicianRepository).save(technicianCaptor.capture());

        Technician updatedTechnician = technicianCaptor.getValue();
        assertEquals(initialJobsDone + 1, updatedTechnician.getTotalJobsDone());
        assertEquals(initialIncome + amount, updatedTechnician.getTotalIncome());
    }

    @Test
    void updateStats_VerifyRepositorySaveIsCalled() {
        // Arrange
        Long amount = 2000L;
        when(technicianRepository.findById(validTechnicianId)).thenReturn(Optional.of(technician));

        // Act
        technicianService.updateStats(validTechnicianId, amount);

        // Assert
        verify(technicianRepository, times(1)).save(any(Technician.class));
    }
}