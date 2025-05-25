package id.ac.ui.cs.advprog.kilimanjaro.controller.exception;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.InvalidCredentialsException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserAlreadyExistsException;
import id.ac.ui.cs.advprog.kilimanjaro.dto.GenericResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleNoResourceFound_ShouldReturnNotFoundStatus() {
        // Arrange
        NoResourceFoundException exception = new NoResourceFoundException(HttpMethod.GET, "not found");

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleNoResourceFound(exception);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void handleValidationErrors_ShouldReturnBadRequestStatusWithFieldMessages() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        List<FieldError> fieldErrors = Arrays.asList(
                new FieldError("object", "username", "must not be blank"),
                new FieldError("object", "age", "must be positive")
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(exception.getMessage()).thenReturn("Validation failed");

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleValidationErrors(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("username: must not be blank; age: must be positive", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void handleValidationErrors_WithEmptyErrors_ShouldReturnEmptyMessage() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleValidationErrors(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("", response.getBody().getMessage());
    }

    @Test
    void handleGrpcError_ShouldReturnServiceUnavailableStatus() {
        // Arrange
        StatusRuntimeException exception = new StatusRuntimeException(Status.UNAVAILABLE.withDescription("Service down"));

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleGrpcError(exception);

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User service unavailable", response.getBody().getMessage());
    }

    @Test
    void handleMessageNotReadable_ShouldReturnBadRequestStatus() {
        // Arrange
        HttpInputMessage inputMessage = mock(HttpInputMessage.class);
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Malformed", inputMessage);

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleMessageNotReadable(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Malformed JSON request or invalid request body", response.getBody().getMessage());
    }

    @Test
    void handleMethodNotSupported_ShouldReturnMethodNotAllowedStatus() {
        // Arrange
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("DELETE");

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleMethodNotSupported(exception);

        // Assert
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("HTTP method DELETE is not supported for this endpoint", response.getBody().getMessage());
    }

    @Test
    void handleAccessDenied_ShouldReturnForbiddenStatus() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Forbidden");

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleAccessDenied(exception);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("You are not authorized to access this resource", response.getBody().getMessage());
    }

    @Test
    void handleGeneric_ShouldReturnInternalServerErrorStatus() {
        // Arrange
        RuntimeException exception = new RuntimeException("Something went wrong");

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleGeneric(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void handleIllegalArgument_ShouldReturnBadRequestStatus() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    void handleInvalidCredentials_ShouldReturnUnauthorizedStatus() {
        // Arrange
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid credentials");

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleInvalidCredentials(exception);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid email or password", response.getBody().getMessage());
    }

    @Test
    void handleUserAlreadyExists_ShouldReturnConflictStatus() {
        // Arrange
        UserAlreadyExistsException exception = new UserAlreadyExistsException("User already exists");

        // Act
        ResponseEntity<GenericResponse<Void>> response = globalExceptionHandler.handleUserAlreadyExists(exception);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Email is already in use", response.getBody().getMessage());
    }
}
