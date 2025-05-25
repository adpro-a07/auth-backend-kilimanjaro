package id.ac.ui.cs.advprog.kilimanjaro.mapper;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.model.Admin;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.Customer;
import id.ac.ui.cs.advprog.kilimanjaro.model.Technician;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserDataMapperFactoryImplTest {

    @Mock
    private UserDataMapper<Admin> adminMapper;

    @Mock
    private UserDataMapper<Customer> customerMapper;

    @Mock
    private UserDataMapper<Technician> technicianMapper;

    @Mock
    private UserData userData;

    private UserDataMapperFactoryImpl factory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up mock mappers
        when(adminMapper.supportsRole()).thenReturn(UserRole.ADMIN);
        when(customerMapper.supportsRole()).thenReturn(UserRole.CUSTOMER);
        when(technicianMapper.supportsRole()).thenReturn(UserRole.TECHNICIAN);

        List<UserDataMapper<? extends BaseUser>> mappers = List.of(
                adminMapper, customerMapper, technicianMapper
        );
        factory = new UserDataMapperFactoryImpl(mappers);
    }

    @Test
    void testGetMapperForAdmin() {
        // When
        UserDataMapper<Admin> mapper = factory.getMapper(UserRole.ADMIN);

        // Then
        assertNotNull(mapper);
        assertEquals(adminMapper, mapper);
        verify(adminMapper, times(1)).supportsRole();
    }

    @Test
    void testGetMapperForCustomer() {
        // When
        UserDataMapper<Customer> mapper = factory.getMapper(UserRole.CUSTOMER);

        // Then
        assertNotNull(mapper);
        assertEquals(customerMapper, mapper);
        verify(customerMapper, times(1)).supportsRole();
    }

    @Test
    void testGetMapperForTechnician() {
        // When
        UserDataMapper<Technician> mapper = factory.getMapper(UserRole.TECHNICIAN);

        // Then
        assertNotNull(mapper);
        assertEquals(technicianMapper, mapper);
        verify(technicianMapper, times(1)).supportsRole();
    }

    @Test
    void testConstructorWithEmptyMapperList() {
        // When
        factory = new UserDataMapperFactoryImpl(List.of());

        // Then
        UserDataMapper<BaseUser> mapper = factory.getMapper(UserRole.ADMIN);
        assertNull(mapper);
    }

    @Test
    void testConstructorWithMultipleMappersForSameRole() {
        // Given
        @SuppressWarnings("unchecked")
        UserDataMapper<BaseUser> duplicateMapper = mock(UserDataMapper.class);
        when(duplicateMapper.supportsRole()).thenReturn(UserRole.ADMIN);

        List<UserDataMapper<? extends BaseUser>> mappersWithDuplicate =
                List.of(adminMapper, customerMapper, technicianMapper, duplicateMapper);

        // When
        factory = new UserDataMapperFactoryImpl(mappersWithDuplicate);

        // Then
        UserDataMapper<Admin> mapper = factory.getMapper(UserRole.ADMIN);
        assertNotNull(mapper);
        // The last mapper for a role should override the previous one
        assertEquals(duplicateMapper, mapper);
    }

    @Test
    void testMapperFunctionality() {
        // Given
        Admin admin = mock(Admin.class);
        when(adminMapper.toUserData(any(Admin.class), anyBoolean())).thenReturn(userData);

        // When
        UserDataMapper<Admin> mapper = factory.getMapper(UserRole.ADMIN);
        UserData result = mapper.toUserData(admin, true);

        // Then
        assertNotNull(result);
        assertEquals(userData, result);
        verify(adminMapper).toUserData(admin, true);
    }

    @Test
    void testMapperForAllRoles() {
        // Test that we can get a mapper for every role in the factory
        List<UserRole> roles = List.of(UserRole.ADMIN, UserRole.CUSTOMER, UserRole.TECHNICIAN);

        for (UserRole role : roles) {
            UserDataMapper<?> mapper = factory.getMapper(role);
            assertNotNull(mapper, "Should find a mapper for role: " + role);
        }
    }
}