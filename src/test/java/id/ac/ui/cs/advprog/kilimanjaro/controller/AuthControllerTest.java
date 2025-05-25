package id.ac.ui.cs.advprog.kilimanjaro.controller;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.InvalidCredentialsException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserAlreadyExistsException;
import id.ac.ui.cs.advprog.kilimanjaro.dto.*;
import id.ac.ui.cs.advprog.kilimanjaro.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterCustomerRequest validCustomerRegisterRequest;
    private LoginRequest validLoginRequest;
    private GenericResponse<LoginResponse> successLoginResponse;
    private GenericResponse<Void> successRegistrationResponse;

    @BeforeEach
    void setUp() {
        validCustomerRegisterRequest = new RegisterCustomerRequest();
        validCustomerRegisterRequest.setFullName("John Doe");
        validCustomerRegisterRequest.setEmail("john@example.com");
        validCustomerRegisterRequest.setPhoneNumber("08123456789");
        validCustomerRegisterRequest.setPassword1("Password123!");
        validCustomerRegisterRequest.setPassword2("Password123!");
        validCustomerRegisterRequest.setAddress("123 Main St");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("john@example.com");
        validLoginRequest.setPassword("Password123!");

        UserResponseDto userResponseDto = UserResponseDto.builder()
                .id(UUID.randomUUID())
                .email("john@example.com")
                .fullName("John Doe")
                .phoneNumber("08123456789")
                .role("CUSTOMER")
                .address("123 Main St")
                .build();


        LoginResponse loginResponseData = new LoginResponse(
                "jwt-access-token",
                "jwt-refresh-token",
                userResponseDto
        );

        successLoginResponse = new GenericResponse<>(
                true,
                "Login successful",
                loginResponseData
        );

        successRegistrationResponse = new GenericResponse<>(
                true,
                "Registration successful",
                null
        );
    }

    @Test
    void registerCustomer_WithValidRequest_ReturnsCreated() {
        when(authService.registerCustomer(validCustomerRegisterRequest)).thenReturn(successRegistrationResponse);

        ResponseEntity<?> response = authController.registerCustomer(validCustomerRegisterRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(successRegistrationResponse, response.getBody());
        verify(authService, times(1)).registerCustomer(validCustomerRegisterRequest);
    }

    @Test
    void registerCustomer_WhenUserExists_ThrowsException() {
        when(authService.registerCustomer(validCustomerRegisterRequest))
                .thenThrow(new UserAlreadyExistsException("Email already in use"));

        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class, () ->
                authController.registerCustomer(validCustomerRegisterRequest));

        assertEquals("Email already in use", ex.getMessage());
        verify(authService, times(1)).registerCustomer(validCustomerRegisterRequest);
    }

    @Test
    void registerCustomer_WithPasswordMismatch_ThrowsException() {
        validCustomerRegisterRequest.setPassword2("DifferentPassword123!");
        when(authService.registerCustomer(validCustomerRegisterRequest))
                .thenThrow(new IllegalArgumentException("Passwords do not match"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                authController.registerCustomer(validCustomerRegisterRequest));

        assertEquals("Passwords do not match", ex.getMessage());
        verify(authService, times(1)).registerCustomer(validCustomerRegisterRequest);
    }

    @Test
    void loginUser_WithValidCredentials_ReturnsOk() {
        when(authService.login(validLoginRequest)).thenReturn(successLoginResponse);

        ResponseEntity<?> response = authController.loginUser(validLoginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successLoginResponse, response.getBody());
        verify(authService, times(1)).login(validLoginRequest);
    }

    @Test
    void loginUser_WithInvalidCredentials_ThrowsException() {
        when(authService.login(validLoginRequest))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () ->
                authController.loginUser(validLoginRequest));

        assertEquals("Invalid credentials", ex.getMessage());
        verify(authService, times(1)).login(validLoginRequest);
    }

    @Test
    void logoutUser_WithValidToken_ReturnsNoContent() {
        String validToken = "jwt-access-token";
        doNothing().when(authService).logout(validToken);

        ResponseEntity<?> response = authController.logoutUser(validToken);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(authService, times(1)).logout(validToken);
    }

    @Test
    void logoutUser_WithEmptyToken_StillCallsService() {
        String emptyToken = "";
        doNothing().when(authService).logout(emptyToken);

        ResponseEntity<?> response = authController.logoutUser(emptyToken);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(authService, times(1)).logout(emptyToken);
    }
}
