package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.*;
import id.ac.ui.cs.advprog.kilimanjaro.util.ResponseStatusUtil;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService
public class GrpcUserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(GrpcUserServiceImpl.class);

    private final UserMapperService userMapperService;

    @Autowired
    public GrpcUserServiceImpl(UserMapperService userMapperService) {
        this.userMapperService = userMapperService;
    }

    @Override
    public void listUsers(ListUsersRequest request,
                          StreamObserver<ListUsersResponse> responseObserver) {
        logger.info("Received list users request: {}", request.getMetadata().getRequestId());

        ListUsersResponse.Builder responseBuilder = ListUsersResponse.newBuilder();

        try {
            // Extract pagination and sorting parameters
            UserRole role = request.getRole();
            int pageSize = request.getPageSize();
            int pageNumber = request.getPageNumber();

            // Apply default values if needed
            pageSize = (pageSize <= 0) ? 10 : pageSize; // Default page size

            // Get paginated users from service
            List<UserData> users = userMapperService.listUsers(
                    role,
                    pageSize,
                    pageNumber
            );

            // Get total count for pagination metadata
            int totalCount = userMapperService.countUsersByRole(role);
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);

            responseBuilder.addAllUsers(users)
                    .setTotalCount(totalCount)
                    .setTotalPages(totalPages)
                    .setCurrentPage(pageNumber)
                    .setStatus(ResponseStatusUtil.createSuccessStatus());

        } catch (Exception e) {
            logger.error("Error listing users", e);
            responseBuilder.setStatus(ResponseStatusUtil.createErrorStatus(e));
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getRandomTechnician(GetRandomTechnicianRequest request,
                                    StreamObserver<GetRandomTechnicianResponse> responseObserver) {
        logger.info("Received random technician request: {}", request.getMetadata().getRequestId());

        GetRandomTechnicianResponse.Builder responseBuilder = GetRandomTechnicianResponse.newBuilder();

        try {
            // Get a random technician from service
            UserData technician = userMapperService.getRandomTechnician();

            responseBuilder.setTechnician(technician)
                    .setStatus(ResponseStatusUtil.createSuccessStatus());

        } catch (Exception e) {
            logger.error("Error getting random technician", e);
            responseBuilder.setStatus(ResponseStatusUtil.createErrorStatus(e));
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}