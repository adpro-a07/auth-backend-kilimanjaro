package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GrpcUserServiceImplTest {
    @Mock
    private UserMapperService userMapperService;

    @Mock
    private StreamObserver<ListUsersResponse> listUsersResponseObserver;

    @Mock
    private StreamObserver<GetRandomTechnicianResponse> randomTechnicianResponseObserver;

    private GrpcUserServiceImpl grpcUserServiceImpl;
    private RequestMetadata mockMetadata;
    private List<UserData> mockUsersList;
    private UserData mockTechnician;

    @BeforeEach
    void setUp() {
        grpcUserServiceImpl = new GrpcUserServiceImpl(userMapperService);
        mockMetadata = RequestMetadata.newBuilder()
                .setRequestId(UUID.randomUUID().toString())
                .setClientVersion("1.0.0")
                .build();

        // Create a list of mock users
        mockUsersList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            UserIdentity identity = UserIdentity.newBuilder()
                    .setId(UUID.randomUUID().toString())
                    .setEmail("user" + i + "@example.com")
                    .setFullName("User " + i)
                    .build();

            UserData userData = UserData.newBuilder()
                    .setIdentity(identity)
                    .build();

            mockUsersList.add(userData);
        }

        // Create a mock technician
        UserIdentity technicianIdentity = UserIdentity.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setEmail("technician@example.com")
                .setFullName("Tech Support")
                .build();

        mockTechnician = UserData.newBuilder()
                .setIdentity(technicianIdentity)
                .build();
    }

    @Test
    void listUsers_WithValidParameters_ShouldReturnUsersList() {
        // Arrange
        int pageSize = 10;
        int pageNumber = 1;
        int totalCount = 25;
        int expectedTotalPages = 3; // Math.ceil(25/10)
        UserRole role = UserRole.CUSTOMER;

        ListUsersRequest request = ListUsersRequest.newBuilder()
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setRole(role)
                .setMetadata(mockMetadata)
                .build();

        when(userMapperService.listUsers(role, pageSize, pageNumber)).thenReturn(mockUsersList);
        when(userMapperService.countUsersByRole(role)).thenReturn(totalCount);

        // Act
        grpcUserServiceImpl.listUsers(request, listUsersResponseObserver);

        // Assert
        ArgumentCaptor<ListUsersResponse> responseCaptor = ArgumentCaptor.forClass(ListUsersResponse.class);
        verify(listUsersResponseObserver).onNext(responseCaptor.capture());
        verify(listUsersResponseObserver).onCompleted();

        ListUsersResponse response = responseCaptor.getValue();
        assertEquals(0, response.getStatus().getCode());
        assertEquals(mockUsersList.size(), response.getUsersCount());
        assertEquals(mockUsersList, response.getUsersList());
        assertEquals(totalCount, response.getTotalCount());
        assertEquals(expectedTotalPages, response.getTotalPages());
        assertEquals(pageNumber, response.getCurrentPage());
    }

    @Test
    void listUsers_WithDefaultPageSize_ShouldUseDefaultValue() {
        // Arrange
        int defaultPageSize = 10;
        int pageNumber = 1;
        int totalCount = 5;
        int expectedTotalPages = 1; // Math.ceil(5/10)
        UserRole role = UserRole.CUSTOMER;

        ListUsersRequest request = ListUsersRequest.newBuilder()
                .setPageSize(0) // Should be replaced with default
                .setPageNumber(pageNumber)
                .setRole(role)
                .setMetadata(mockMetadata)
                .build();

        when(userMapperService.listUsers(role, defaultPageSize, pageNumber)).thenReturn(mockUsersList);
        when(userMapperService.countUsersByRole(role)).thenReturn(totalCount);

        // Act
        grpcUserServiceImpl.listUsers(request, listUsersResponseObserver);

        // Assert
        ArgumentCaptor<ListUsersResponse> responseCaptor = ArgumentCaptor.forClass(ListUsersResponse.class);
        verify(listUsersResponseObserver).onNext(responseCaptor.capture());
        verify(listUsersResponseObserver).onCompleted();

        ListUsersResponse response = responseCaptor.getValue();
        assertEquals(0, response.getStatus().getCode());
        assertEquals(totalCount, response.getTotalCount());
        assertEquals(expectedTotalPages, response.getTotalPages());

        // Verify that default page size was used
        verify(userMapperService).listUsers(role, defaultPageSize, pageNumber);
    }

    @Test
    void listUsers_WithNegativePageSize_ShouldUseDefaultValue() {
        // Arrange
        int defaultPageSize = 10;
        int pageNumber = 1;
        int totalCount = 5;
        UserRole role = UserRole.CUSTOMER;

        ListUsersRequest request = ListUsersRequest.newBuilder()
                .setPageSize(-5) // Should be replaced with default
                .setPageNumber(pageNumber)
                .setRole(role)
                .setMetadata(mockMetadata)
                .build();

        when(userMapperService.listUsers(role, defaultPageSize, pageNumber)).thenReturn(mockUsersList);
        when(userMapperService.countUsersByRole(role)).thenReturn(totalCount);

        // Act
        grpcUserServiceImpl.listUsers(request, listUsersResponseObserver);

        // Assert
        verify(userMapperService).listUsers(role, defaultPageSize, pageNumber);
    }

    @Test
    void listUsers_WithPerfectDivision_ShouldCalculateCorrectTotalPages() {
        // Arrange
        int pageSize = 5;
        int pageNumber = 2;
        int totalCount = 15;
        int expectedTotalPages = 3; // Math.ceil(15/5) = 3
        UserRole role = UserRole.CUSTOMER;

        ListUsersRequest request = ListUsersRequest.newBuilder()
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setRole(role)
                .setMetadata(mockMetadata)
                .build();

        when(userMapperService.listUsers(role, pageSize, pageNumber)).thenReturn(mockUsersList);
        when(userMapperService.countUsersByRole(role)).thenReturn(totalCount);

        // Act
        grpcUserServiceImpl.listUsers(request, listUsersResponseObserver);

        // Assert
        ArgumentCaptor<ListUsersResponse> responseCaptor = ArgumentCaptor.forClass(ListUsersResponse.class);
        verify(listUsersResponseObserver).onNext(responseCaptor.capture());

        ListUsersResponse response = responseCaptor.getValue();
        assertEquals(expectedTotalPages, response.getTotalPages());
    }

    @Test
    void listUsers_WithImperfectDivision_ShouldRoundUpTotalPages() {
        // Arrange
        int pageSize = 4;
        int pageNumber = 1;
        int totalCount = 10;
        int expectedTotalPages = 3; // Math.ceil(10/4) = 2.5 -> 3
        UserRole role = UserRole.CUSTOMER;

        ListUsersRequest request = ListUsersRequest.newBuilder()
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setRole(role)
                .setMetadata(mockMetadata)
                .build();

        when(userMapperService.listUsers(role, pageSize, pageNumber)).thenReturn(mockUsersList);
        when(userMapperService.countUsersByRole(role)).thenReturn(totalCount);

        // Act
        grpcUserServiceImpl.listUsers(request, listUsersResponseObserver);

        // Assert
        ArgumentCaptor<ListUsersResponse> responseCaptor = ArgumentCaptor.forClass(ListUsersResponse.class);
        verify(listUsersResponseObserver).onNext(responseCaptor.capture());

        ListUsersResponse response = responseCaptor.getValue();
        assertEquals(expectedTotalPages, response.getTotalPages());
    }

    @Test
    void listUsers_WithZeroUsers_ShouldHandleEmptyResult() {
        // Arrange
        int pageSize = 10;
        int pageNumber = 1;
        int totalCount = 0;
        int expectedTotalPages = 0; // No pages when no users
        UserRole role = UserRole.CUSTOMER;
        List<UserData> emptyList = new ArrayList<>();

        ListUsersRequest request = ListUsersRequest.newBuilder()
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setRole(role)
                .setMetadata(mockMetadata)
                .build();

        when(userMapperService.listUsers(role, pageSize, pageNumber)).thenReturn(emptyList);
        when(userMapperService.countUsersByRole(role)).thenReturn(totalCount);

        // Act
        grpcUserServiceImpl.listUsers(request, listUsersResponseObserver);

        // Assert
        ArgumentCaptor<ListUsersResponse> responseCaptor = ArgumentCaptor.forClass(ListUsersResponse.class);
        verify(listUsersResponseObserver).onNext(responseCaptor.capture());
        verify(listUsersResponseObserver).onCompleted();

        ListUsersResponse response = responseCaptor.getValue();
        assertEquals(0, response.getStatus().getCode());
        assertEquals(0, response.getUsersCount());
        assertEquals(totalCount, response.getTotalCount());
        assertEquals(expectedTotalPages, response.getTotalPages());
    }

    @Test
    void listUsers_ServiceThrowsException_ShouldHandleError() {
        // Arrange
        int pageSize = 10;
        int pageNumber = 1;
        UserRole role = UserRole.CUSTOMER;

        ListUsersRequest request = ListUsersRequest.newBuilder()
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setRole(role)
                .setMetadata(mockMetadata)
                .build();

        String errorMessage = "Database connection error";
        when(userMapperService.listUsers(role, pageSize, pageNumber))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        grpcUserServiceImpl.listUsers(request, listUsersResponseObserver);

        // Assert
        ArgumentCaptor<ListUsersResponse> responseCaptor = ArgumentCaptor.forClass(ListUsersResponse.class);
        verify(listUsersResponseObserver).onNext(responseCaptor.capture());
        verify(listUsersResponseObserver).onCompleted();

        ListUsersResponse response = responseCaptor.getValue();
        assertNotEquals(0, response.getStatus().getCode());
        assertEquals(errorMessage, response.getStatus().getMessage());
        assertEquals(0, response.getUsersCount());
    }

    @Test
    void listUsers_CountThrowsException_ShouldHandleError() {
        // Arrange
        int pageSize = 10;
        int pageNumber = 1;
        UserRole role = UserRole.CUSTOMER;

        ListUsersRequest request = ListUsersRequest.newBuilder()
                .setPageSize(pageSize)
                .setPageNumber(pageNumber)
                .setRole(role)
                .setMetadata(mockMetadata)
                .build();

        String errorMessage = "Count operation failed";
        when(userMapperService.listUsers(role, pageSize, pageNumber)).thenReturn(mockUsersList);
        when(userMapperService.countUsersByRole(role))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        grpcUserServiceImpl.listUsers(request, listUsersResponseObserver);

        // Assert
        ArgumentCaptor<ListUsersResponse> responseCaptor = ArgumentCaptor.forClass(ListUsersResponse.class);
        verify(listUsersResponseObserver).onNext(responseCaptor.capture());
        verify(listUsersResponseObserver).onCompleted();

        ListUsersResponse response = responseCaptor.getValue();
        assertNotEquals(0, response.getStatus().getCode());
        assertEquals(errorMessage, response.getStatus().getMessage());
    }

    @Test
    void getRandomTechnician_Success_ShouldReturnTechnician() {
        // Arrange
        GetRandomTechnicianRequest request = GetRandomTechnicianRequest.newBuilder()
                .setMetadata(mockMetadata)
                .build();

        when(userMapperService.getRandomTechnician()).thenReturn(mockTechnician);

        // Act
        grpcUserServiceImpl.getRandomTechnician(request, randomTechnicianResponseObserver);

        // Assert
        ArgumentCaptor<GetRandomTechnicianResponse> responseCaptor =
                ArgumentCaptor.forClass(GetRandomTechnicianResponse.class);
        verify(randomTechnicianResponseObserver).onNext(responseCaptor.capture());
        verify(randomTechnicianResponseObserver).onCompleted();

        GetRandomTechnicianResponse response = responseCaptor.getValue();
        assertEquals(0, response.getStatus().getCode());
        assertTrue(response.hasTechnician());
        assertEquals(mockTechnician, response.getTechnician());
    }

    @Test
    void getRandomTechnician_NoTechniciansAvailable_ShouldHandleException() {
        // Arrange
        GetRandomTechnicianRequest request = GetRandomTechnicianRequest.newBuilder()
                .setMetadata(mockMetadata)
                .build();

        String errorMessage = "No technicians available";
        when(userMapperService.getRandomTechnician())
                .thenThrow(new IllegalStateException(errorMessage));

        // Act
        grpcUserServiceImpl.getRandomTechnician(request, randomTechnicianResponseObserver);

        // Assert
        ArgumentCaptor<GetRandomTechnicianResponse> responseCaptor =
                ArgumentCaptor.forClass(GetRandomTechnicianResponse.class);
        verify(randomTechnicianResponseObserver).onNext(responseCaptor.capture());
        verify(randomTechnicianResponseObserver).onCompleted();

        GetRandomTechnicianResponse response = responseCaptor.getValue();
        assertNotEquals(0, response.getStatus().getCode());
        assertEquals(errorMessage, response.getStatus().getMessage());
        assertFalse(response.hasTechnician());
    }

    @Test
    void getRandomTechnician_ServiceThrowsException_ShouldHandleError() {
        // Arrange
        GetRandomTechnicianRequest request = GetRandomTechnicianRequest.newBuilder()
                .setMetadata(mockMetadata)
                .build();

        String errorMessage = "Database access error";
        when(userMapperService.getRandomTechnician())
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        grpcUserServiceImpl.getRandomTechnician(request, randomTechnicianResponseObserver);

        // Assert
        ArgumentCaptor<GetRandomTechnicianResponse> responseCaptor =
                ArgumentCaptor.forClass(GetRandomTechnicianResponse.class);
        verify(randomTechnicianResponseObserver).onNext(responseCaptor.capture());
        verify(randomTechnicianResponseObserver).onCompleted();

        GetRandomTechnicianResponse response = responseCaptor.getValue();
        assertNotEquals(0, response.getStatus().getCode());
        assertEquals(errorMessage, response.getStatus().getMessage());
    }
}