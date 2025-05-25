package id.ac.ui.cs.advprog.kilimanjaro.mapper;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserIdentity;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.model.Technician;
import id.ac.ui.cs.advprog.kilimanjaro.util.UserMapperUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TechnicianUserDataMapperTest {

    @Test
    void testToUserData_shouldMapCorrectly() {
        Technician technician = mock(Technician.class);
        when(technician.getAddress()).thenReturn("Workshop Lane");
        when(technician.getExperience()).thenReturn("5 years");
        when(technician.getTotalJobsDone()).thenReturn(42);
        when(technician.getTotalIncome()).thenReturn(1250000L);

        UserIdentity identity = UserIdentity.newBuilder()
                .setEmail("tech@example.com")
                .setRole(UserRole.TECHNICIAN)
                .build();

        try (MockedStatic<UserMapperUtil> mockedStatic = mockStatic(UserMapperUtil.class)) {
            mockedStatic.when(() -> UserMapperUtil.extractUserIdentity(technician))
                    .thenReturn(identity);

            TechnicianUserDataMapper mapper = new TechnicianUserDataMapper();
            UserData userData = mapper.toUserData(technician);

            assertNotNull(userData);
            assertEquals("tech@example.com", userData.getIdentity().getEmail());
            assertEquals(UserRole.TECHNICIAN, userData.getIdentity().getRole());
            assertEquals("Workshop Lane", userData.getProfile().getAddress());
            assertEquals("5 years", userData.getProfile().getWorkExperience());
            assertEquals(42, userData.getProfile().getTotalJobsDone());
            assertEquals(1250000L, userData.getProfile().getTotalIncome());
        }
    }

    @Test
    void testToUserData_includeProfileFlagFalseShouldMapWithoutUserProfile() {
        Technician technician = mock(Technician.class);
        when(technician.getAddress()).thenReturn("Workshop Lane");
        when(technician.getExperience()).thenReturn("5 years");
        when(technician.getTotalJobsDone()).thenReturn(42);
        when(technician.getTotalIncome()).thenReturn(1250000L);

        UserIdentity identity = UserIdentity.newBuilder()
                .setEmail("tech@example.com")
                .setRole(UserRole.TECHNICIAN)
                .build();

        try (MockedStatic<UserMapperUtil> mockedStatic = mockStatic(UserMapperUtil.class)) {
            mockedStatic.when(() -> UserMapperUtil.extractUserIdentity(technician))
                    .thenReturn(identity);

            TechnicianUserDataMapper mapper = new TechnicianUserDataMapper();
            UserData userData = mapper.toUserData(technician, false);

            assertNotNull(userData);
            assertEquals("tech@example.com", userData.getIdentity().getEmail());
            assertEquals(UserRole.TECHNICIAN, userData.getIdentity().getRole());
            assertTrue(userData.getProfile().getAddress().isEmpty());
            assertTrue(userData.getProfile().getWorkExperience().isEmpty());
            assertEquals(0, userData.getProfile().getTotalJobsDone());
            assertEquals(0, userData.getProfile().getTotalIncome());
        }
    }

    @Test
    void testSupportsRole_shouldReturnTechnicianRole() {
        TechnicianUserDataMapper mapper = new TechnicianUserDataMapper();
        assertEquals("TECHNICIAN", mapper.supportsRole().name());
    }

    @Test
    void testToUserData_includeProfileFlagTrueShouldMapWithUserProfile() {
        Technician technician = mock(Technician.class);
        when(technician.getAddress()).thenReturn("Workshop Lane 2");
        when(technician.getExperience()).thenReturn("10 years");
        when(technician.getTotalJobsDone()).thenReturn(100);
        when(technician.getTotalIncome()).thenReturn(3000000L);

        UserIdentity identity = UserIdentity.newBuilder()
                .setEmail("tech2@example.com")
                .setRole(UserRole.TECHNICIAN)
                .build();

        try (MockedStatic<UserMapperUtil> mockedStatic = mockStatic(UserMapperUtil.class)) {
            mockedStatic.when(() -> UserMapperUtil.extractUserIdentity(technician))
                    .thenReturn(identity);

            TechnicianUserDataMapper mapper = new TechnicianUserDataMapper();
            UserData userData = mapper.toUserData(technician, true);

            assertNotNull(userData);
            assertEquals("tech2@example.com", userData.getIdentity().getEmail());
            assertEquals(UserRole.TECHNICIAN, userData.getIdentity().getRole());
            assertEquals("Workshop Lane 2", userData.getProfile().getAddress());
            assertEquals("10 years", userData.getProfile().getWorkExperience());
            assertEquals(100, userData.getProfile().getTotalJobsDone());
            assertEquals(3000000L, userData.getProfile().getTotalIncome());
        }
    }

    @Test
    void testToUserResponseDto_shouldIncludeAllTechnicianFields() {
        Technician technician = mock(Technician.class);
        UUID randomUUID = UUID.randomUUID();
        when(technician.getId()).thenReturn(randomUUID);
        when(technician.getFullName()).thenReturn("Jane Technician");
        when(technician.getEmail()).thenReturn("jane.tech@example.com");
        when(technician.getPhoneNumber()).thenReturn("08234567890");
        when(technician.getRole()).thenReturn(id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole.TECHNICIAN);
        when(technician.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(technician.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(technician.getAddress()).thenReturn("Tech Street 99");
        when(technician.getExperience()).thenReturn("7 years");
        when(technician.getTotalJobsDone()).thenReturn(75);
        when(technician.getTotalIncome()).thenReturn(2000000L);

        TechnicianUserDataMapper mapper = new TechnicianUserDataMapper();
        var dto = mapper.toUserResponseDto(technician);

        assertEquals(randomUUID, dto.getId());
        assertEquals("Jane Technician", dto.getFullName());
        assertEquals("jane.tech@example.com", dto.getEmail());
        assertEquals("08234567890", dto.getPhoneNumber());
        assertEquals("TECHNICIAN", dto.getRole());
        assertEquals("Tech Street 99", dto.getAddress());
        assertEquals("7 years", dto.getExperience());
        assertEquals(75, dto.getTotalJobsDone());
        assertEquals(2000000L, dto.getTotalIncome());
    }

    @Test
    void testBuildProfile_shouldReturnUserProfileWithTechnicianFields() {
        Technician technician = mock(Technician.class);
        when(technician.getAddress()).thenReturn("Build Profile Address");
        when(technician.getExperience()).thenReturn("3 years");
        when(technician.getTotalJobsDone()).thenReturn(15);
        when(technician.getTotalIncome()).thenReturn(500000L);

        TechnicianUserDataMapper mapper = new TechnicianUserDataMapper();
        var profile = mapper.buildProfile(technician);

        assertNotNull(profile);
        assertEquals("Build Profile Address", profile.getAddress());
        assertEquals("3 years", profile.getWorkExperience());
        assertEquals(15, profile.getTotalJobsDone());
        assertEquals(500000L, profile.getTotalIncome());
    }
}
