package id.ac.ui.cs.advprog.kilimanjaro.service;
import com.google.protobuf.Empty;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GrpcAuthServiceImplTest {
    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserMapperService userMapperService;

    @Mock
    private StreamObserver<TokenValidationResponse> tokenValidationResponseObserver;

    @Mock
    private StreamObserver<TokenRefreshResponse> tokenRefreshResponseObserver;

    @Mock
    private StreamObserver<UserLookupResponse> userLookupResponseObserver;

    @Mock
    private StreamObserver<BatchUserLookupResponse> batchUserLookupResponseObserver;

    @Mock
    private StreamObserver<HealthCheckResponse> healthCheckResponseObserver;

    private GrpcAuthServiceImpl grpcAuthServiceImpl;
    private String mockToken;
    private String mockRefreshToken;
    private RequestMetadata mockMetadata;
    private UUID mockUserId;
    private String mockEmail;
    private UserData mockUserData;

    @BeforeEach
    void setUp() {
        grpcAuthServiceImpl = new GrpcAuthServiceImpl(jwtTokenService, userMapperService);
        mockToken = "valid.mock.token";
        mockRefreshToken = "valid.mock.refresh.token";
        mockUserId = UUID.randomUUID();
        mockEmail = "test@example.com";
        mockMetadata = RequestMetadata.newBuilder()
                .setRequestId(UUID.randomUUID().toString())
                .setClientVersion("1.0.0")
                .build();

        UserIdentity mockUserIdentity = UserIdentity.newBuilder()
                .setId(mockUserId.toString())
                .setEmail(mockEmail)
                .setFullName("Test User")
                .build();

        mockUserData = UserData.newBuilder()
                .setIdentity(mockUserIdentity)
                .build();
    }

    @Test
    void checkHealth_ShouldReturnServingStatus() {
        // Arrange
        Empty request = Empty.getDefaultInstance();

        // Act
        grpcAuthServiceImpl.checkHealth(request, healthCheckResponseObserver);

        // Assert
        ArgumentCaptor<HealthCheckResponse> responseCaptor = ArgumentCaptor.forClass(HealthCheckResponse.class);
        verify(healthCheckResponseObserver).onNext(responseCaptor.capture());
        verify(healthCheckResponseObserver).onCompleted();

        HealthCheckResponse response = responseCaptor.getValue();
        assertEquals(HealthCheckResponse.ServingStatus.SERVING_STATUS_SERVING, response.getStatus());
    }

    @Test
    void validateToken_ValidTokenWithUserData_ShouldReturnValidResponseWithUserData() {
        // Arrange
        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setToken(mockToken)
                .setIncludeUserData(true)
                .setMetadata(mockMetadata)
                .build();

        when(jwtTokenService.validateToken(mockToken, "access")).thenReturn(true);
        when(userMapperService.getUserDataFromToken(mockToken)).thenReturn(mockUserData);

        // Act
        grpcAuthServiceImpl.validateToken(request, tokenValidationResponseObserver);

        // Assert
        ArgumentCaptor<TokenValidationResponse> responseCaptor = ArgumentCaptor.forClass(TokenValidationResponse.class);
        verify(tokenValidationResponseObserver).onNext(responseCaptor.capture());
        verify(tokenValidationResponseObserver).onCompleted();

        TokenValidationResponse response = responseCaptor.getValue();
        assertTrue(response.getValid());
        assertTrue(response.hasUserData());
        assertEquals(mockUserData, response.getUserData());
        assertEquals(0, response.getStatus().getCode());
    }

    @Test
    void validateToken_ValidTokenWithoutUserData_ShouldReturnValidResponseWithoutUserData() {
        // Arrange
        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setToken(mockToken)
                .setIncludeUserData(false)
                .setMetadata(mockMetadata)
                .build();

        when(jwtTokenService.validateToken(mockToken, "access")).thenReturn(true);

        // Act
        grpcAuthServiceImpl.validateToken(request, tokenValidationResponseObserver);

        // Assert
        ArgumentCaptor<TokenValidationResponse> responseCaptor = ArgumentCaptor.forClass(TokenValidationResponse.class);
        verify(tokenValidationResponseObserver).onNext(responseCaptor.capture());
        verify(tokenValidationResponseObserver).onCompleted();

        TokenValidationResponse response = responseCaptor.getValue();
        assertTrue(response.getValid());
        assertFalse(response.hasUserData());
        assertEquals(0, response.getStatus().getCode());

        // Verify userMapperService.getUserDataFromToken was not called
        verify(userMapperService, never()).getUserDataFromToken(any());
    }

    @Test
    void validateToken_InvalidToken_ShouldReturnInvalidResponse() {
        // Arrange
        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setToken("invalid.token")
                .setIncludeUserData(true)
                .setMetadata(mockMetadata)
                .build();

        when(jwtTokenService.validateToken("invalid.token", "access")).thenReturn(false);

        // Act
        grpcAuthServiceImpl.validateToken(request, tokenValidationResponseObserver);

        // Assert
        ArgumentCaptor<TokenValidationResponse> responseCaptor = ArgumentCaptor.forClass(TokenValidationResponse.class);
        verify(tokenValidationResponseObserver).onNext(responseCaptor.capture());
        verify(tokenValidationResponseObserver).onCompleted();

        TokenValidationResponse response = responseCaptor.getValue();
        assertFalse(response.getValid());
        assertFalse(response.hasUserData());
        assertNotEquals(0, response.getStatus().getCode());

        // Verify userMapperService.getUserDataFromToken was not called
        verify(userMapperService, never()).getUserDataFromToken(any());
    }

    @Test
    void validateToken_TokenValidationThrowsException_ShouldHandleException() {
        // Arrange
        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setToken(mockToken)
                .setIncludeUserData(true)
                .setMetadata(mockMetadata)
                .build();

        when(jwtTokenService.validateToken(mockToken, "access"))
                .thenThrow(new RuntimeException("Token validation failed"));

        // Act
        grpcAuthServiceImpl.validateToken(request, tokenValidationResponseObserver);

        // Assert
        ArgumentCaptor<TokenValidationResponse> responseCaptor = ArgumentCaptor.forClass(TokenValidationResponse.class);
        verify(tokenValidationResponseObserver).onNext(responseCaptor.capture());
        verify(tokenValidationResponseObserver).onCompleted();

        TokenValidationResponse response = responseCaptor.getValue();
        assertFalse(response.getValid());
        assertNotEquals(0, response.getStatus().getCode());
        assertEquals("Token validation failed", response.getStatus().getMessage());
    }

    @Test
    void validateToken_RefreshToken_ShouldReturnInvalidResponse() {
        // Arrange
        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setToken("refresh.token")
                .setIncludeUserData(false)
                .setMetadata(mockMetadata)
                .build();

        when(jwtTokenService.validateToken("refresh.token", "access")).thenReturn(false);

        // Act
        grpcAuthServiceImpl.validateToken(request, tokenValidationResponseObserver);

        // Assert
        ArgumentCaptor<TokenValidationResponse> responseCaptor = ArgumentCaptor.forClass(TokenValidationResponse.class);
        verify(tokenValidationResponseObserver).onNext(responseCaptor.capture());
        verify(tokenValidationResponseObserver).onCompleted();

        TokenValidationResponse response = responseCaptor.getValue();
        assertFalse(response.getValid());
        assertNotEquals(0, response.getStatus().getCode());

        verify(userMapperService, never()).getUserDataFromToken(any());
    }


    @Test
    void refreshToken_ValidRefreshToken_ShouldReturnNewTokenPair() {
        // Arrange
        TokenRefreshRequest request = TokenRefreshRequest.newBuilder()
                .setRefreshToken(mockRefreshToken)
                .setMetadata(mockMetadata)
                .build();

        String newAccessToken = "new.access.token";
        String newRefreshToken = "new.refresh.token";
        JwtTokenService.TokenPair tokenPair = new JwtTokenService.TokenPair(newAccessToken, newRefreshToken);

        when(jwtTokenService.refreshToken(mockRefreshToken)).thenReturn(tokenPair);

        // Act
        grpcAuthServiceImpl.refreshToken(request, tokenRefreshResponseObserver);

        // Assert
        ArgumentCaptor<TokenRefreshResponse> responseCaptor = ArgumentCaptor.forClass(TokenRefreshResponse.class);
        verify(tokenRefreshResponseObserver).onNext(responseCaptor.capture());
        verify(tokenRefreshResponseObserver).onCompleted();

        TokenRefreshResponse response = responseCaptor.getValue();
        assertEquals(newAccessToken, response.getAccessToken());
        assertEquals(newRefreshToken, response.getRefreshToken());
        assertEquals(0, response.getStatus().getCode());
    }

    @Test
    void refreshToken_InvalidRefreshToken_ShouldHandleException() {
        // Arrange
        TokenRefreshRequest request = TokenRefreshRequest.newBuilder()
                .setRefreshToken("invalid.refresh.token")
                .setMetadata(mockMetadata)
                .build();

        when(jwtTokenService.refreshToken("invalid.refresh.token"))
                .thenThrow(new IllegalArgumentException("Invalid refresh token"));

        // Act
        grpcAuthServiceImpl.refreshToken(request, tokenRefreshResponseObserver);

        // Assert
        ArgumentCaptor<TokenRefreshResponse> responseCaptor = ArgumentCaptor.forClass(TokenRefreshResponse.class);
        verify(tokenRefreshResponseObserver).onNext(responseCaptor.capture());
        verify(tokenRefreshResponseObserver).onCompleted();

        TokenRefreshResponse response = responseCaptor.getValue();
        assertNotEquals(0, response.getStatus().getCode());
        assertEquals("Invalid refresh token", response.getStatus().getMessage());
    }

    @Test
    void lookupUser_ByUserId_ShouldReturnUserData() {
        // Arrange
        UserLookupRequest request = UserLookupRequest.newBuilder()
                .setUserId(mockUserId.toString())
                .setMetadata(mockMetadata)
                .build();

        when(userMapperService.getUserById(mockUserId)).thenReturn(mockUserData);

        // Act
        grpcAuthServiceImpl.lookupUser(request, userLookupResponseObserver);

        // Assert
        ArgumentCaptor<UserLookupResponse> responseCaptor = ArgumentCaptor.forClass(UserLookupResponse.class);
        verify(userLookupResponseObserver).onNext(responseCaptor.capture());
        verify(userLookupResponseObserver).onCompleted();

        UserLookupResponse response = responseCaptor.getValue();
        assertEquals(0, response.getStatus().getCode());
        assertEquals(mockUserData, response.getUserData());
    }

    @Test
    void lookupUser_ByEmail_ShouldReturnUserData() {
        // Arrange
        UserLookupRequest request = UserLookupRequest.newBuilder()
                .setEmail(mockEmail)
                .setMetadata(mockMetadata)
                .build();

        when(userMapperService.getUserByEmail(mockEmail)).thenReturn(mockUserData);

        // Act
        grpcAuthServiceImpl.lookupUser(request, userLookupResponseObserver);

        // Assert
        ArgumentCaptor<UserLookupResponse> responseCaptor = ArgumentCaptor.forClass(UserLookupResponse.class);
        verify(userLookupResponseObserver).onNext(responseCaptor.capture());
        verify(userLookupResponseObserver).onCompleted();

        UserLookupResponse response = responseCaptor.getValue();
        assertEquals(0, response.getStatus().getCode());
        assertEquals(mockUserData, response.getUserData());
    }

    @Test
    void lookupUser_UserNotFound_ShouldHandleException() {
        // Arrange
        UserLookupRequest request = UserLookupRequest.newBuilder()
                .setUserId(mockUserId.toString())
                .setMetadata(mockMetadata)
                .build();

        when(userMapperService.getUserById(mockUserId))
                .thenThrow(new IllegalArgumentException("User not found"));

        // Act
        grpcAuthServiceImpl.lookupUser(request, userLookupResponseObserver);

        // Assert
        ArgumentCaptor<UserLookupResponse> responseCaptor = ArgumentCaptor.forClass(UserLookupResponse.class);
        verify(userLookupResponseObserver).onNext(responseCaptor.capture());
        verify(userLookupResponseObserver).onCompleted();

        UserLookupResponse response = responseCaptor.getValue();
        assertNotEquals(0, response.getStatus().getCode());
        assertEquals("User not found", response.getStatus().getMessage());
    }

    @Test
    void lookupUser_InvalidIdentifier_ShouldHandleException() {
        // Arrange - create a request with no identifier set
        UserLookupRequest request = UserLookupRequest.newBuilder()
                .setMetadata(mockMetadata)
                .build();

        // Act
        grpcAuthServiceImpl.lookupUser(request, userLookupResponseObserver);

        // Assert
        ArgumentCaptor<UserLookupResponse> responseCaptor = ArgumentCaptor.forClass(UserLookupResponse.class);
        verify(userLookupResponseObserver).onNext(responseCaptor.capture());
        verify(userLookupResponseObserver).onCompleted();

        UserLookupResponse response = responseCaptor.getValue();
        assertNotEquals(0, response.getStatus().getCode());
        assertEquals("Invalid identifier type", response.getStatus().getMessage());
    }

    @Test
    void batchLookupUsers_AllUsersFound_ShouldReturnAllUsersData() {
        // Arrange
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        String email1 = "user1@example.com";

        UserIdentifier identifier1 = UserIdentifier.newBuilder().setUserId(userId1.toString()).build();
        UserIdentifier identifier2 = UserIdentifier.newBuilder().setUserId(userId2.toString()).build();
        UserIdentifier identifier3 = UserIdentifier.newBuilder().setEmail(email1).build();

        BatchUserLookupRequest request = BatchUserLookupRequest.newBuilder()
                .addAllIdentifiers(Arrays.asList(identifier1, identifier2, identifier3))
                .setIncludeProfile(true)
                .setMetadata(mockMetadata)
                .build();

        UserIdentity userIdentity1 = UserIdentity.newBuilder()
                .setId(userId1.toString())
                .setEmail("user1@example.com")
                .setFullName("User One")
                .build();
        UserData userData1 = UserData.newBuilder()
                .setIdentity(userIdentity1)
                .build();

        UserIdentity userIdentity2 = UserIdentity.newBuilder()
                .setId(userId2.toString())
                .setEmail("user2@example.com")
                .setFullName("User Two")
                .build();
        UserData userData2 = UserData.newBuilder()
                .setIdentity(userIdentity2)
                .build();

        UserIdentity userIdentity3 = UserIdentity.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setEmail(email1)
                .setFullName("User Three")
                .build();
        UserData userData3 = UserData.newBuilder()
                .setIdentity(userIdentity3)
                .build();

        when(userMapperService.getUserById(eq(userId1), eq(true))).thenReturn(userData1);
        when(userMapperService.getUserById(eq(userId2), eq(true))).thenReturn(userData2);
        when(userMapperService.getUserByEmail(eq(email1), eq(true))).thenReturn(userData3);

        // Act
        grpcAuthServiceImpl.batchLookupUsers(request, batchUserLookupResponseObserver);

        // Assert
        ArgumentCaptor<BatchUserLookupResponse> responseCaptor = ArgumentCaptor.forClass(BatchUserLookupResponse.class);
        verify(batchUserLookupResponseObserver).onNext(responseCaptor.capture());
        verify(batchUserLookupResponseObserver).onCompleted();

        BatchUserLookupResponse response = responseCaptor.getValue();
        assertEquals(0, response.getStatus().getCode());
        assertEquals(3, response.getResultsCount());
        assertEquals(3, response.getTotalFound());
        assertEquals(0, response.getTotalNotFound());

        // Verify each result
        for (UserLookupResult result : response.getResultsList()) {
            assertTrue(result.getFound());
            if (result.getRequestedIdentifier().hasUserId() &&
                    result.getRequestedIdentifier().getUserId().equals(userId1.toString())) {
                assertEquals(userData1, result.getUserData());
            } else if (result.getRequestedIdentifier().hasUserId() &&
                    result.getRequestedIdentifier().getUserId().equals(userId2.toString())) {
                assertEquals(userData2, result.getUserData());
            } else if (result.getRequestedIdentifier().hasEmail() &&
                    result.getRequestedIdentifier().getEmail().equals(email1)) {
                assertEquals(userData3, result.getUserData());
            } else {
                fail("Unexpected identifier in result");
            }
        }
    }

    @Test
    void batchLookupUsers_SomeUsersNotFound_ShouldReportCorrectCounts() {
        // Arrange
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        String email1 = "user1@example.com";

        UserIdentifier identifier1 = UserIdentifier.newBuilder().setUserId(userId1.toString()).build();
        UserIdentifier identifier2 = UserIdentifier.newBuilder().setUserId(userId2.toString()).build();
        UserIdentifier identifier3 = UserIdentifier.newBuilder().setEmail(email1).build();

        BatchUserLookupRequest request = BatchUserLookupRequest.newBuilder()
                .addAllIdentifiers(Arrays.asList(identifier1, identifier2, identifier3))
                .setIncludeProfile(false)
                .setMetadata(mockMetadata)
                .build();

        UserIdentity userIdentity1 = UserIdentity.newBuilder()
                .setId(userId1.toString())
                .setEmail("user1@example.com")
                .setFullName("User One")
                .build();

        UserData userData1 = UserData.newBuilder()
                .setIdentity(userIdentity1)
                .build();

        when(userMapperService.getUserById(eq(userId1), eq(false))).thenReturn(userData1);
        when(userMapperService.getUserById(eq(userId2), eq(false)))
                .thenThrow(new IllegalArgumentException("User not found"));
        when(userMapperService.getUserByEmail(eq(email1), eq(false)))
                .thenThrow(new IllegalArgumentException("User not found"));

        // Act
        grpcAuthServiceImpl.batchLookupUsers(request, batchUserLookupResponseObserver);

        // Assert
        ArgumentCaptor<BatchUserLookupResponse> responseCaptor = ArgumentCaptor.forClass(BatchUserLookupResponse.class);
        verify(batchUserLookupResponseObserver).onNext(responseCaptor.capture());
        verify(batchUserLookupResponseObserver).onCompleted();

        BatchUserLookupResponse response = responseCaptor.getValue();
        assertEquals(0, response.getStatus().getCode());
        assertEquals(3, response.getResultsCount());
        assertEquals(1, response.getTotalFound());
        assertEquals(2, response.getTotalNotFound());

        // Count found and not found results
        int foundCount = 0;
        int notFoundCount = 0;
        for (UserLookupResult result : response.getResultsList()) {
            if (result.getFound()) {
                foundCount++;
                assertEquals(userData1, result.getUserData());
            } else {
                notFoundCount++;
                assertTrue(result.hasError());
                assertEquals("User not found", result.getError().getDescription());
            }
        }

        assertEquals(1, foundCount);
        assertEquals(2, notFoundCount);
    }

    @Test
    void batchLookupUsers_EmptyIdentifiersList_ShouldReturnEmptyResults() {
        // Arrange
        BatchUserLookupRequest request = BatchUserLookupRequest.newBuilder()
                .setIncludeProfile(true)
                .setMetadata(mockMetadata)
                .build();

        // Act
        grpcAuthServiceImpl.batchLookupUsers(request, batchUserLookupResponseObserver);

        // Assert
        ArgumentCaptor<BatchUserLookupResponse> responseCaptor = ArgumentCaptor.forClass(BatchUserLookupResponse.class);
        verify(batchUserLookupResponseObserver).onNext(responseCaptor.capture());
        verify(batchUserLookupResponseObserver).onCompleted();

        BatchUserLookupResponse response = responseCaptor.getValue();
        assertEquals(0, response.getStatus().getCode());
        assertEquals(0, response.getResultsCount());
        assertEquals(0, response.getTotalFound());
        assertEquals(0, response.getTotalNotFound());
    }

    @Test
    void batchLookupUsers_ProcessingError_ShouldHandleException() {
        // Arrange
        BatchUserLookupRequest request = BatchUserLookupRequest.newBuilder()
                .addIdentifiers(UserIdentifier.newBuilder().setUserId("invalid-uuid").build())
                .setIncludeProfile(true)
                .setMetadata(mockMetadata)
                .build();

        // Act
        grpcAuthServiceImpl.batchLookupUsers(request, batchUserLookupResponseObserver);

        // Assert
        ArgumentCaptor<BatchUserLookupResponse> responseCaptor = ArgumentCaptor.forClass(BatchUserLookupResponse.class);
        verify(batchUserLookupResponseObserver).onNext(responseCaptor.capture());
        verify(batchUserLookupResponseObserver).onCompleted();

        BatchUserLookupResponse response = responseCaptor.getValue();
        assertEquals(0, response.getStatus().getCode());
    }
}