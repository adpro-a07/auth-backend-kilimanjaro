package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.AuthenticationException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserNotFoundException;
import id.ac.ui.cs.advprog.kilimanjaro.mapper.UserDataMapper;
import id.ac.ui.cs.advprog.kilimanjaro.mapper.UserDataMapperFactory;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class UserMapperServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserDataMapperFactory mapperFactory;
    @Mock private JwtTokenService jwtTokenService;
    @Mock private UserDataMapper<BaseUser> mapper;

    @InjectMocks private UserMapperServiceImpl userMapperService;

    private BaseUser user;
    private UserData userData;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        user = mock(BaseUser.class);
        when(user.getRole()).thenReturn(UserRole.CUSTOMER);

        userData = UserData.newBuilder().build();
    }

    // --- getUserById(UUID) ---
    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        when(mapper.toUserData(user)).thenReturn(userData);

        UserData result = userMapperService.getUserById(userId);
        assertEquals(userData, result);
    }

    @Test
    void testGetUserById_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userMapperService.getUserById(userId));
    }

    // --- getUserById(UUID, includeProfile) ---
    @Test
    void testGetUserById_WithIncludeProfileTrue() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        when(mapper.toUserData(user, true)).thenReturn(userData);

        UserData result = userMapperService.getUserById(userId, true);
        assertEquals(userData, result);
    }

    @Test
    void testGetUserById_WithIncludeProfileFalse() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        when(mapper.toUserData(user, false)).thenReturn(userData);

        UserData result = userMapperService.getUserById(userId, false);
        assertEquals(userData, result);
    }

    @Test
    void testGetUserById_WithIncludeProfile_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userMapperService.getUserById(userId, true));
    }

    // --- getUserByEmail(String) ---
    @Test
    void testGetUserByEmail_Success() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        when(mapper.toUserData(user)).thenReturn(userData);

        UserData result = userMapperService.getUserByEmail(email);
        assertEquals(userData, result);
    }

    @Test
    void testGetUserByEmail_UserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userMapperService.getUserByEmail("missing@example.com"));
    }

    // --- getUserByEmail(String, includeProfile) ---
    @Test
    void testGetUserByEmail_WithIncludeProfileTrue() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        when(mapper.toUserData(user, true)).thenReturn(userData);

        UserData result = userMapperService.getUserByEmail(email, true);
        assertEquals(userData, result);
    }

    @Test
    void testGetUserByEmail_WithIncludeProfileFalse() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        when(mapper.toUserData(user, false)).thenReturn(userData);

        UserData result = userMapperService.getUserByEmail(email, false);
        assertEquals(userData, result);
    }

    @Test
    void testGetUserByEmail_WithIncludeProfile_UserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userMapperService.getUserByEmail("missing@example.com", true));
    }

    // --- getUserDataFromToken ---
    @Test
    void testGetUserDataFromToken_Success() throws AuthenticationException, UserNotFoundException {
        String token = "valid.token.here";
        when(jwtTokenService.getUserFromToken(token)).thenReturn(user);
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        when(mapper.toUserData(user)).thenReturn(userData);

        UserData result = userMapperService.getUserDataFromToken(token);
        assertEquals(userData, result);
    }

    @Test
    void testGetUserDataFromToken_UserNotFoundInToken() {
        String token = "invalid.token";
        when(jwtTokenService.getUserFromToken(token)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userMapperService.getUserDataFromToken(token));
    }

    @Test
    void testGetUserDataFromToken_ThrowsAuthenticationException() throws AuthenticationException {
        String token = "bad.token";
        when(jwtTokenService.getUserFromToken(token)).thenThrow(new AuthenticationException("Invalid"));

        assertThrows(AuthenticationException.class, () -> userMapperService.getUserDataFromToken(token));
    }

    // --- listUsers ---
    @Test
    void testListUsers_WithSpecificRole() {
        // Prepare test data
        BaseUser user1 = mock(BaseUser.class);
        BaseUser user2 = mock(BaseUser.class);
        when(user1.getRole()).thenReturn(UserRole.CUSTOMER);
        when(user2.getRole()).thenReturn(UserRole.CUSTOMER);

        List<BaseUser> userList = Arrays.asList(user1, user2);
        Page<BaseUser> userPage = new PageImpl<>(userList);

        // Set up mapper behavior
        UserData userData1 = UserData.newBuilder().build();
        UserData userData2 = UserData.newBuilder().build();
        when(mapper.toUserData(user1, true)).thenReturn(userData1);
        when(mapper.toUserData(user2, true)).thenReturn(userData2);
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);

        // Capture pageable parameter
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(userRepository.findAllByRoleAndNotAdmin(eq("CUSTOMER"), pageableCaptor.capture())).thenReturn(userPage);

        // Test with standard parameters
        List<UserData> result = userMapperService.listUsers(
                id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.CUSTOMER, 10, 0);

        // Verify
        assertEquals(2, result.size());
        assertEquals(userData1, result.get(0));
        assertEquals(userData2, result.get(1));

        // Verify pageable settings
        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(10, capturedPageable.getPageSize());
        assertEquals(Sort.Direction.ASC,
                Objects.requireNonNull(capturedPageable.getSort().getOrderFor("email")).getDirection());
    }

    @Test
    void testListUsers_WithNullRole() {
        // Prepare test data
        BaseUser user1 = mock(BaseUser.class);
        BaseUser user2 = mock(BaseUser.class);
        when(user1.getRole()).thenReturn(UserRole.CUSTOMER);
        when(user2.getRole()).thenReturn(UserRole.TECHNICIAN);

        List<BaseUser> userList = Arrays.asList(user1, user2);
        Page<BaseUser> userPage = new PageImpl<>(userList);

        // Set up mappers behavior
        UserDataMapper<BaseUser> customerMapper = (UserDataMapper<BaseUser>) mock(UserDataMapper.class);
        UserDataMapper<BaseUser> technicianMapper = (UserDataMapper<BaseUser>) mock(UserDataMapper.class);

        UserData userData1 = UserData.newBuilder().build();
        UserData userData2 = UserData.newBuilder().build();

        when(customerMapper.toUserData(user1, true)).thenReturn(userData1);
        when(technicianMapper.toUserData(user2, true)).thenReturn(userData2);

        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(customerMapper);
        when(mapperFactory.getMapper(UserRole.TECHNICIAN)).thenReturn(technicianMapper);

        // Capture pageable parameter
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(userRepository.findAllNotAdmin(pageableCaptor.capture())).thenReturn(userPage);

        // Test with null role
        List<UserData> result = userMapperService.listUsers(null, 10, 0);

        // Verify
        assertEquals(2, result.size());
        assertEquals(userData1, result.get(0));
        assertEquals(userData2, result.get(1));
    }

    @Test
    void testListUsers_WithUnrecognizedRole() {
        // Test with unrecognized role
        BaseUser user1 = mock(BaseUser.class);
        when(user1.getRole()).thenReturn(UserRole.CUSTOMER);
        List<BaseUser> userList = Collections.singletonList(user1);
        Page<BaseUser> userPage = new PageImpl<>(userList);

        when(mapper.toUserData(user1, true)).thenReturn(userData);
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);

        when(userRepository.findAllNotAdmin(any(Pageable.class))).thenReturn(userPage);

        List<UserData> result = userMapperService.listUsers(
                id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.UNRECOGNIZED, 10, 0);

        // Verify
        assertEquals(1, result.size());
        assertEquals(userData, result.getFirst());
    }

    @Test
    void testListUsers_WithNegativePageSize() {
        // Test with negative page size (should use default 10)
        BaseUser user1 = mock(BaseUser.class);
        when(user1.getRole()).thenReturn(UserRole.CUSTOMER);
        List<BaseUser> userList = Collections.singletonList(user1);
        Page<BaseUser> userPage = new PageImpl<>(userList);

        when(mapper.toUserData(user1, true)).thenReturn(userData);
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(userRepository.findAllByRoleAndNotAdmin(eq("CUSTOMER"), pageableCaptor.capture())).thenReturn(userPage);

        userMapperService.listUsers(
                id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.CUSTOMER, -5, 0);

        // Verify default page size is used
        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(10, capturedPageable.getPageSize());
    }

    @Test
    void testListUsers_WithNegativePageNumber() {
        // Test with negative page number (should use 0)
        BaseUser user1 = mock(BaseUser.class);
        when(user1.getRole()).thenReturn(UserRole.CUSTOMER);
        List<BaseUser> userList = Collections.singletonList(user1);
        Page<BaseUser> userPage = new PageImpl<>(userList);

        when(mapper.toUserData(user1, true)).thenReturn(userData);
        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(userRepository.findAllByRoleAndNotAdmin(eq("CUSTOMER"), pageableCaptor.capture())).thenReturn(userPage);

        userMapperService.listUsers(
                id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.CUSTOMER, 10, -3);

        // Verify page number is adjusted to 0
        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(0, capturedPageable.getPageNumber());
    }

    @Test
    void testListUsers_EmptyResults() {
        // Test empty results
        Page<BaseUser> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.findAllByRoleAndNotAdmin(eq("CUSTOMER"), any(Pageable.class))).thenReturn(emptyPage);

        List<UserData> result = userMapperService.listUsers(
                id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.CUSTOMER, 10, 0);

        assertTrue(result.isEmpty());
    }

    // --- countUsersByRole ---
    @Test
    void testCountUsersByRole_WithSpecificRole() {
        when(userRepository.countByRoleAndNotAdmin("CUSTOMER")).thenReturn(5L);

        int result = userMapperService.countUsersByRole(id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.CUSTOMER);

        assertEquals(5, result);
        verify(userRepository).countByRoleAndNotAdmin("CUSTOMER");
    }

    @Test
    void testCountUsersByRole_WithNullRole() {
        when(userRepository.countNotAdmin()).thenReturn(10L);

        int result = userMapperService.countUsersByRole(null);

        assertEquals(10, result);
        verify(userRepository).countNotAdmin();
    }

    @Test
    void testCountUsersByRole_WithUnspecifiedRole() {
        when(userRepository.countNotAdmin()).thenReturn(15L);

        int result = userMapperService.countUsersByRole(id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.UNSPECIFIED);

        assertEquals(15, result);
        verify(userRepository).countNotAdmin();
    }

    @Test
    void testCountUsersByRole_WithUnrecognizedRole() {
        when(userRepository.countNotAdmin()).thenReturn(20L);

        int result = userMapperService.countUsersByRole(id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.UNRECOGNIZED);

        assertEquals(20, result);
        verify(userRepository).countNotAdmin();
    }

    @Test
    void testCountUsersByRole_WithAdminRole() {
        when(userRepository.countNotAdmin()).thenReturn(25L);

        int result = userMapperService.countUsersByRole(id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.ADMIN);

        assertEquals(25, result);
        verify(userRepository).countNotAdmin();
    }

    // --- getRandomTechnician ---
    @Test
    void testGetRandomTechnician_Success() {
        // Prepare test data
        BaseUser tech1 = mock(BaseUser.class);
        BaseUser tech2 = mock(BaseUser.class);
        BaseUser tech3 = mock(BaseUser.class);

        when(tech1.getRole()).thenReturn(UserRole.TECHNICIAN);
        when(tech2.getRole()).thenReturn(UserRole.TECHNICIAN);
        when(tech3.getRole()).thenReturn(UserRole.TECHNICIAN);

        List<BaseUser> technicianList = Arrays.asList(tech1, tech2, tech3);

        when(userRepository.findAllByRoleAndNotAdmin("TECHNICIAN")).thenReturn(technicianList);

        UserDataMapper<BaseUser> techMapper = (UserDataMapper<BaseUser>) mock(UserDataMapper.class);
        UserData techData = UserData.newBuilder().build();

        when(mapperFactory.getMapper(UserRole.TECHNICIAN)).thenReturn(techMapper);
        when(techMapper.toUserData(any(BaseUser.class), eq(true))).thenReturn(techData);

        // Execute
        UserData result = userMapperService.getRandomTechnician();

        // Verify
        assertEquals(techData, result);
        verify(userRepository).findAllByRoleAndNotAdmin("TECHNICIAN");
        verify(techMapper).toUserData(any(BaseUser.class), eq(true));
    }

    @Test
    void testGetRandomTechnician_NoTechniciansFound() {
        when(userRepository.findAllByRoleAndNotAdmin("TECHNICIAN")).thenReturn(Collections.emptyList());

        assertThrows(UserNotFoundException.class, () -> userMapperService.getRandomTechnician());
        verify(userRepository).findAllByRoleAndNotAdmin("TECHNICIAN");
    }

    // --- getDomainRole (private method, testing indirectly through public methods) ---
    @Test
    void testGetDomainRole_IndirectlyThroughListUsers() {
        // Testing null handling by calling listUsers with null
        Page<BaseUser> userPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.findAllNotAdmin(any(Pageable.class))).thenReturn(userPage);

        userMapperService.listUsers(null, 10, 0);

        verify(userRepository).findAllNotAdmin(any(Pageable.class));
        verify(userRepository, never()).findAllByRoleAndNotAdmin(anyString(), any(Pageable.class));
    }

    @Test
    void testGetDomainRole_IndirectlyThroughCountUsersByRole() {
        // Testing ADMIN role handling through countUsersByRole
        when(userRepository.countNotAdmin()).thenReturn(5L);

        userMapperService.countUsersByRole(id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.ADMIN);

        verify(userRepository).countNotAdmin();
        verify(userRepository, never()).countByRoleAndNotAdmin(anyString());
    }

    @Test
    void testGetDomainRole_IndirectlyWithInvalidRole() {
        // Create a test that would exercise the catch block in getDomainRole
        // We can do this by using reflection to access the private method directly

        // For this test, we'll verify the behavior through a public method
        // Since UserRole is an enum, we can't easily create an invalid one for testing
        // So we'll test the valid cases through public methods

        when(userRepository.countByRoleAndNotAdmin("CUSTOMER")).thenReturn(3L);
        userMapperService.countUsersByRole(id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.CUSTOMER);
        verify(userRepository).countByRoleAndNotAdmin("CUSTOMER");

        when(userRepository.countByRoleAndNotAdmin("TECHNICIAN")).thenReturn(2L);
        userMapperService.countUsersByRole(id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole.TECHNICIAN);
        verify(userRepository).countByRoleAndNotAdmin("TECHNICIAN");
    }
}