package id.ac.ui.cs.advprog.kilimanjaro.model;


import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserProfile;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {
    @Test
    void testAdminBuilderCreatesAdminSuccessfully() {
        Admin admin = new Admin.AdminBuilder()
                .fullName("Super Admin")
                .email("admin@example.com")
                .phoneNumber("0811223344")
                .password("verySecurePassword")
                .build();

        assertNull(admin.getId());
        assertEquals("Super Admin", admin.getFullName());
        assertEquals("admin@example.com", admin.getEmail());
        assertEquals("0811223344", admin.getPhoneNumber());
        assertEquals("verySecurePassword", admin.getPassword());
        assertEquals("ADMIN", admin.getRole().name());
    }

    @Test
    void testAdminBuilderThrowsExceptionWhenFullNameIsMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Admin.AdminBuilder()
                        .email("admin@example.com")
                        .phoneNumber("0811223344")
                        .password("verySecurePassword")
                        .build()
        );
        assertEquals("All fields must be non-null", exception.getMessage());
    }

    @Test
    void testAdminBuilderThrowsExceptionWhenEmailIsMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Admin.AdminBuilder()
                        .fullName("Super Admin")
                        .phoneNumber("0811223344")
                        .password("verySecurePassword")
                        .build()
        );
        assertEquals("All fields must be non-null", exception.getMessage());
    }

    @Test
    void testAdminBuilderThrowsExceptionWhenPhoneNumberIsMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Admin.AdminBuilder()
                        .fullName("Super Admin")
                        .email("admin@example.com")
                        .password("verySecurePassword")
                        .build()
        );
        assertEquals("All fields must be non-null", exception.getMessage());
    }

    @Test
    void testAdminBuilderThrowsExceptionWhenPasswordIsMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Admin.AdminBuilder()
                        .fullName("Super Admin")
                        .email("admin@example.com")
                        .phoneNumber("0811223344")
                        .build()
        );
        assertEquals("All fields must be non-null", exception.getMessage());
    }

    @Test
    void testAdminBuilderAllowsSettingCustomId() {
        UUID customId = UUID.randomUUID();
        Admin admin = new Admin.AdminBuilder()
                .id(customId)
                .fullName("Custom ID Admin")
                .email("customid@example.com")
                .phoneNumber("0800000000")
                .password("customPass")
                .build();

        assertEquals(customId, admin.getId());
    }

    @Test
    void testGetProfileReturnsUserProfileInstance() {
        Admin admin = new Admin.AdminBuilder()
                .fullName("Super Admin")
                .email("admin@example.com")
                .phoneNumber("0811223344")
                .password("verySecurePassword")
                .build();

        UserProfile profile = admin.getProfile();

        assertNotNull(profile, "UserProfile should not be null");
        assertEquals("", profile.getAddress());
        assertEquals("", profile.getWorkExperience());
        assertEquals(0, profile.getTotalIncome());
        assertEquals(0, profile.getTotalJobsDone());
    }
}
