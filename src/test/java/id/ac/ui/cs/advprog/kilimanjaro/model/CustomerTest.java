package id.ac.ui.cs.advprog.kilimanjaro.model;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserProfile;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void testUserBuilderCreatesUserSuccessfully() {
        Customer customer = new Customer.CustomerBuilder()
                .fullName("John Doe")
                .email("john@example.com")
                .phoneNumber("08123456789")
                .password("securePassword")
                .address("Jl. Merdeka No. 1")
                .build();

        assertNull(customer.getId());
        assertEquals("John Doe", customer.getFullName());
        assertEquals("john@example.com", customer.getEmail());
        assertEquals("08123456789", customer.getPhoneNumber());
        assertEquals("securePassword", customer.getPassword());
        assertEquals("Jl. Merdeka No. 1", customer.getAddress());
        assertEquals("CUSTOMER", customer.getRole().name());
    }

    @Test
    void testUserBuilderThrowsExceptionWhenFullNameIsMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Customer.CustomerBuilder()
                        .email("john@example.com")
                        .phoneNumber("08123456789")
                        .password("securePassword")
                        .address("Jl. Merdeka No. 1")
                        .build()
        );
        assertEquals("All fields must be non-null", exception.getMessage());
    }

    @Test
    void testUserBuilderThrowsExceptionWhenEmailIsMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Customer.CustomerBuilder()
                        .fullName("John Doe")
                        .phoneNumber("08123456789")
                        .password("securePassword")
                        .address("Jl. Merdeka No. 1")
                        .build()
        );
        assertEquals("All fields must be non-null", exception.getMessage());
    }

    @Test
    void testUserBuilderThrowsExceptionWhenPhoneNumberIsMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Customer.CustomerBuilder()
                        .fullName("John Doe")
                        .email("john@example.com")
                        .password("securePassword")
                        .address("Jl. Merdeka No. 1")
                        .build()
        );
        assertEquals("All fields must be non-null", exception.getMessage());
    }

    @Test
    void testUserBuilderThrowsExceptionWhenPasswordIsMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Customer.CustomerBuilder()
                        .fullName("John Doe")
                        .email("john@example.com")
                        .phoneNumber("08123456789")
                        .address("Jl. Merdeka No. 1")
                        .build()
        );
        assertEquals("All fields must be non-null", exception.getMessage());
    }

    @Test
    void testUserBuilderCanSetCustomId() {
        UUID customId = UUID.randomUUID();
        Customer customer = new Customer.CustomerBuilder()
                .id(customId)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .phoneNumber("08987654321")
                .password("anotherPassword")
                .address("Jl. Sudirman No. 2")
                .build();

        assertEquals(customId, customer.getId());
    }

    @Test
    void testGetProfileReturnsUserProfileInstance() {
        UUID customId = UUID.randomUUID();
        Customer customer = new Customer.CustomerBuilder()
                .id(customId)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .phoneNumber("08987654321")
                .password("anotherPassword")
                .address("Jl. Sudirman No. 2")
                .build();

        UserProfile profile = customer.getProfile();

        assertNotNull(profile, "UserProfile should not be null");
        assertEquals(profile.getAddress(), customer.getAddress(), "Address should match");
        assertEquals("", profile.getWorkExperience());
        assertEquals(0, profile.getTotalIncome());
        assertEquals(0, profile.getTotalJobsDone());
    }
}
