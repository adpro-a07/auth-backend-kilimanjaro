package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.InvalidCredentialsException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserAlreadyExistsException;
import id.ac.ui.cs.advprog.kilimanjaro.dto.*;
import id.ac.ui.cs.advprog.kilimanjaro.mapper.UserDataMapper;
import id.ac.ui.cs.advprog.kilimanjaro.mapper.UserDataMapperFactory;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.Customer;
import id.ac.ui.cs.advprog.kilimanjaro.model.Technician;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDataMapperFactory mapperFactory;

    @Mock
    private UserDataMapper<BaseUser> mapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterCustomerRequest registerCustomerRequest;
    private RegisterTechnicianRequest registerTechnicianRequest;
    private LoginRequest loginRequest;
    private Customer customer;
    private Technician technician;

    @BeforeEach
    void setUp() {
        registerCustomerRequest = new RegisterCustomerRequest();
        registerCustomerRequest.setFullName("Test Customer");
        registerCustomerRequest.setEmail("customer@example.com");
        registerCustomerRequest.setPassword1("Password123!"); // Updated to match new field name and pattern
        registerCustomerRequest.setPassword2("Password123!"); // Added password confirmation
        registerCustomerRequest.setPhoneNumber("123456789");
        registerCustomerRequest.setAddress("123 Customer Street");

        registerTechnicianRequest = new RegisterTechnicianRequest();
        registerTechnicianRequest.setFullName("Test Technician");
        registerTechnicianRequest.setEmail("technician@example.com");
        registerTechnicianRequest.setPassword1("Password123!"); // Updated to match new field name and pattern
        registerTechnicianRequest.setPassword2("Password123!"); // Added password confirmation
        registerTechnicianRequest.setPhoneNumber("987654321");
        registerTechnicianRequest.setAddress("456 Technician Avenue");
        registerTechnicianRequest.setExperience("5 years");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("customer@example.com");
        loginRequest.setPassword("Password123!"); // Updated password to match the new pattern

        customer = new Customer.CustomerBuilder()
                .fullName("Test Customer")
                .email("customer@example.com")
                .phoneNumber("08123456789")
                .password("encoded_password")
                .address("Jl. Merdeka No. 1")
                .build();

        technician = new Technician.TechnicianBuilder()
                .fullName("Tech Technician")
                .email("technician@example.com")
                .phoneNumber("0877123456")
                .password("encoded_password")
                .address("Jl. Teknisi No. 10")
                .experience("5 years in electronics")
                .build();
    }



    @Test
    void registerCustomer_ShouldCreateNewCustomer_WhenEmailIsUnique() {
        // Arrange
        when(userRepository.existsByEmail("customer@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_password");
        when(userRepository.save(any(Customer.class))).thenReturn(customer);

        GenericResponse<Void> response = authService.registerCustomer(registerCustomerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Registration successful", response.getMessage());
        assertTrue(response.isSuccess());
        verify(userRepository).save(any(Customer.class));
    }

    @Test
    void registerCustomer_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail("customer@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () ->
                authService.registerCustomer(registerCustomerRequest)
        );
        verify(userRepository, never()).save(any(Customer.class));
    }

    @Test
    void registerCustomer_ShouldThrowException_WhenPasswordsDoNotMatch() {
        // Arrange
        registerCustomerRequest.setPassword1("Password123!");
        registerCustomerRequest.setPassword2("DifferentPassword123!");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                authService.registerCustomer(registerCustomerRequest)
        );
        verify(userRepository, never()).save(any(Customer.class));
    }

    @Test
    void registerTechnician_ShouldCreateNewTechnician_WhenEmailIsUnique() {
        // Arrange
        when(userRepository.existsByEmail("technician@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_password");
        when(userRepository.save(any(Technician.class))).thenReturn(technician);

        GenericResponse<Void> response = authService.registerTechnician(registerTechnicianRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Registration successful", response.getMessage());
        assertTrue(response.isSuccess());
        verify(userRepository).save(any(Technician.class));
    }

    @Test
    void registerTechnician_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail("technician@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () ->
                authService.registerTechnician(registerTechnicianRequest)
        );
        verify(userRepository, never()).save(any(Technician.class));
    }

    @Test
    void registerTechnician_ShouldThrowException_WhenPasswordsDoNotMatch() {
        // Arrange
        registerTechnicianRequest.setPassword1("Password123!");
        registerTechnicianRequest.setPassword2("DifferentPassword123!");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                authService.registerTechnician(registerTechnicianRequest)
        );
        verify(userRepository, never()).save(any(Technician.class));
    }

    @Test
    void registerTechnician_ShouldThrowException_WhenExperienceIsNull() {
        // Arrange
        registerTechnicianRequest.setExperience(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                authService.registerTechnician(registerTechnicianRequest)
        );
        verify(userRepository, never()).save(any(Technician.class));
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new InvalidCredentialsException("Invalid username or password"));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () ->
                authService.login(loginRequest)
        );
    }

    @Test
    void login_ShouldReturnTokenAndUser_WhenCredentialsAreValid() {
        // Arrange
        JwtTokenService.TokenPair tokenPair =
                new JwtTokenService.TokenPair("jwt-access-token", "jwt-refresh-token");

        when(userRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtTokenService.generateTokensFromEmail(customer.getEmail())).thenReturn(tokenPair);

        when(mapperFactory.getMapper(UserRole.CUSTOMER)).thenReturn(mapper);
        UserResponseDto mockUserData = UserResponseDto.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .fullName(customer.getFullName())
                .phoneNumber(customer.getPhoneNumber())
                .role(customer.getRole().name())
                .address(customer.getAddress())
                .build();

        when(mapper.toUserResponseDto(customer)).thenReturn(mockUserData);

        // Act
        GenericResponse<LoginResponse> response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());

        LoginResponse loginResponse = response.getData();
        assertEquals("jwt-access-token", loginResponse.getAccessToken());
        assertEquals("jwt-refresh-token", loginResponse.getRefreshToken());
        assertEquals(mockUserData, loginResponse.getUser());
    }

    @Test
    void logout_ShouldInvalidateToken() {
        // Arrange
        String token = "jwt-token";

        authService.logout(token);

        verify(jwtTokenService).invalidateToken(token);
    }
}