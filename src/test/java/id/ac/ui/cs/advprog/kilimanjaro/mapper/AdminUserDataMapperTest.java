package id.ac.ui.cs.advprog.kilimanjaro.mapper;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserIdentity;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.model.Admin;
import id.ac.ui.cs.advprog.kilimanjaro.util.UserMapperUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminUserDataMapperTest {

    @Test
    void testToUserData_shouldMapCorrectly() {
        Admin admin = mock(Admin.class);

        UserIdentity identity = UserIdentity.newBuilder()
                .setEmail("admin@example.com")
                .setRole(UserRole.ADMIN)
                .build();

        try (MockedStatic<UserMapperUtil> mockedStatic = mockStatic(UserMapperUtil.class)) {
            mockedStatic.when(() -> UserMapperUtil.extractUserIdentity(admin))
                    .thenReturn(identity);

            AdminUserDataMapper mapper = new AdminUserDataMapper();
            UserData userData = mapper.toUserData(admin);

            assertNotNull(userData);
            assertEquals("admin@example.com", userData.getIdentity().getEmail());
            assertEquals(UserRole.ADMIN, userData.getIdentity().getRole());
            assertTrue(userData.getProfile().getAddress().isEmpty());
        }
    }

    @Test
    void testToUserData_includeProfileFlagFalseShouldMapWithoutUserProfile() {
        Admin admin = mock(Admin.class);

        UserIdentity identity = UserIdentity.newBuilder()
                .setEmail("admin@example.com")
                .setRole(UserRole.ADMIN)
                .build();

        try (MockedStatic<UserMapperUtil> mockedStatic = mockStatic(UserMapperUtil.class)) {
            mockedStatic.when(() -> UserMapperUtil.extractUserIdentity(admin))
                    .thenReturn(identity);

            AdminUserDataMapper mapper = new AdminUserDataMapper();
            UserData userData = mapper.toUserData(admin, false);

            assertNotNull(userData);
            assertEquals("admin@example.com", userData.getIdentity().getEmail());
            assertEquals(UserRole.ADMIN, userData.getIdentity().getRole());
            assertTrue(userData.getProfile().getAddress().isEmpty());
            assertTrue(userData.getProfile().getWorkExperience().isEmpty());
            assertEquals(0, userData.getProfile().getTotalJobsDone());
            assertEquals(0, userData.getProfile().getTotalIncome());
        }
    }

    @Test
    void testSupportsRole_shouldReturnAdminRole() {
        AdminUserDataMapper mapper = new AdminUserDataMapper();
        assertEquals("ADMIN", mapper.supportsRole().name());
    }

}
