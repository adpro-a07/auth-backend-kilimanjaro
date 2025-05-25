package id.ac.ui.cs.advprog.kilimanjaro.mapper;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserIdentity;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.dto.UserResponseDto;
import id.ac.ui.cs.advprog.kilimanjaro.model.Customer;
import id.ac.ui.cs.advprog.kilimanjaro.util.UserMapperUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerUserDataMapperTest {

    @Test
    void testToUserData_shouldMapCorrectly() {
        Customer customer = mock(Customer.class);
        when(customer.getAddress()).thenReturn("Jl. Testing 123");

        UserIdentity fakeIdentity = UserIdentity.newBuilder()
                .setEmail("customer@example.com")
                .setRole(UserRole.CUSTOMER)
                .build();

        try (MockedStatic<UserMapperUtil> mockedStatic = mockStatic(UserMapperUtil.class)) {
            mockedStatic.when(() -> UserMapperUtil.extractUserIdentity(customer))
                    .thenReturn(fakeIdentity);

            CustomerUserDataMapper mapper = new CustomerUserDataMapper();
            UserData userData = mapper.toUserData(customer);

            assertNotNull(userData);
            assertEquals("customer@example.com", userData.getIdentity().getEmail());
            assertEquals(UserRole.CUSTOMER, userData.getIdentity().getRole());
            assertEquals("Jl. Testing 123", userData.getProfile().getAddress());
        }
    }

    @Test
    void testToUserData_shouldThrowNullPointerExceptionIfAddressIsNull() {
        Customer customer = mock(Customer.class);
        // Simulate that getAddress() returns null.
        when(customer.getAddress()).thenReturn(null);

        UserIdentity identity = UserIdentity.newBuilder()
                .setEmail("customer@example.com")
                .setRole(UserRole.CUSTOMER)
                .build();

        try (MockedStatic<UserMapperUtil> mockedStatic = mockStatic(UserMapperUtil.class)) {
            mockedStatic.when(() -> UserMapperUtil.extractUserIdentity(customer))
                    .thenReturn(identity);

            // Since getAddress() returns null, we expect a NullPointerException
            assertThrows(NullPointerException.class, () ->
                new CustomerUserDataMapper().toUserData(customer)
            );
        }
    }

    @Test
    void testToUserData_shouldNotAllowNullAddress() {
        Customer customer = mock(Customer.class);
        // Simulate an empty address instead of null.
        when(customer.getAddress()).thenReturn(""); // Address is not null but empty.

        UserIdentity identity = UserIdentity.newBuilder()
                .setEmail("customer@example.com")
                .setRole(UserRole.CUSTOMER)
                .build();

        try (MockedStatic<UserMapperUtil> mockedStatic = mockStatic(UserMapperUtil.class)) {
            mockedStatic.when(() -> UserMapperUtil.extractUserIdentity(customer))
                    .thenReturn(identity);

            // Here, we are checking that the address is not null, even if it is empty.
            CustomerUserDataMapper mapper = new CustomerUserDataMapper();
            UserData userData = mapper.toUserData(customer);

            // Address should not be null; however, it could be empty or handled by your code.
            assertNotNull(userData.getProfile().getAddress());
            assertTrue(userData.getProfile().getAddress().isEmpty()); // It can be empty but not null.
        }
    }

    @Test
    void testToUserData_includeProfileFlagFalseShouldMapWithoutUserProfile() {
        Customer customer = mock(Customer.class);
        when(customer.getAddress()).thenReturn("Jl. Testing 123");

        UserIdentity fakeIdentity = UserIdentity.newBuilder()
                .setEmail("customer@example.com")
                .setRole(UserRole.CUSTOMER)
                .build();

        try (MockedStatic<UserMapperUtil> mockedStatic = mockStatic(UserMapperUtil.class)) {
            mockedStatic.when(() -> UserMapperUtil.extractUserIdentity(customer))
                    .thenReturn(fakeIdentity);

            CustomerUserDataMapper mapper = new CustomerUserDataMapper();
            UserData userData = mapper.toUserData(customer, false);

            assertNotNull(userData);
            assertEquals("customer@example.com", userData.getIdentity().getEmail());
            assertEquals(UserRole.CUSTOMER, userData.getIdentity().getRole());
            assertTrue(userData.getProfile().getAddress().isEmpty());
            assertTrue(userData.getProfile().getWorkExperience().isEmpty());
            assertEquals(0, userData.getProfile().getTotalJobsDone());
            assertEquals(0, userData.getProfile().getTotalIncome());
        }
    }

    @Test
    void testSupportsRole_shouldReturnCustomerRole() {
        CustomerUserDataMapper mapper = new CustomerUserDataMapper();
        assertEquals("CUSTOMER", mapper.supportsRole().name());
    }

    @Test
    void testToUserData_includeProfileFlagTrueShouldMapWithUserProfile() {
        Customer customer = mock(Customer.class);
        when(customer.getAddress()).thenReturn("Jl. Testing 456");

        UserIdentity identity = UserIdentity.newBuilder()
                .setEmail("customer2@example.com")
                .setRole(UserRole.CUSTOMER)
                .build();

        try (MockedStatic<UserMapperUtil> mockedStatic = mockStatic(UserMapperUtil.class)) {
            mockedStatic.when(() -> UserMapperUtil.extractUserIdentity(customer))
                    .thenReturn(identity);

            CustomerUserDataMapper mapper = new CustomerUserDataMapper();
            UserData userData = mapper.toUserData(customer, true);

            assertNotNull(userData);
            assertEquals("customer2@example.com", userData.getIdentity().getEmail());
            assertEquals(UserRole.CUSTOMER, userData.getIdentity().getRole());
            assertEquals("Jl. Testing 456", userData.getProfile().getAddress());
        }
    }

    @Test
    void testToUserResponseDto_shouldIncludeAddress() {
        Customer customer = mock(Customer.class);
        UUID randomUUID = UUID.randomUUID();
        when(customer.getId()).thenReturn(randomUUID);
        when(customer.getFullName()).thenReturn("John Doe");
        when(customer.getEmail()).thenReturn("john@example.com");
        when(customer.getPhoneNumber()).thenReturn("08123456789");
        when(customer.getRole()).thenReturn(id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole.CUSTOMER);
        when(customer.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(customer.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(customer.getAddress()).thenReturn("Jl. Customer Address");

        CustomerUserDataMapper mapper = new CustomerUserDataMapper();
        UserResponseDto dto = mapper.toUserResponseDto(customer);

        assertEquals(randomUUID, dto.getId());
        assertEquals("John Doe", dto.getFullName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("08123456789", dto.getPhoneNumber());
        assertEquals("CUSTOMER", dto.getRole());
        assertEquals("Jl. Customer Address", dto.getAddress());
    }

    @Test
    void testBuildProfile_shouldReturnUserProfileWithAddress() {
        Customer customer = mock(Customer.class);
        when(customer.getAddress()).thenReturn("Jl. Profile Address");

        CustomerUserDataMapper mapper = new CustomerUserDataMapper();
        var profile = mapper.buildProfile(customer);

        assertNotNull(profile);
        assertEquals("Jl. Profile Address", profile.getAddress());
    }
}
