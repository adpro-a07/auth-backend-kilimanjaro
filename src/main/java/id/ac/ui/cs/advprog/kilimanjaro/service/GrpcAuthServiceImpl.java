package id.ac.ui.cs.advprog.kilimanjaro.service;

import com.google.protobuf.Empty;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.*;
import id.ac.ui.cs.advprog.kilimanjaro.util.ResponseStatusUtil;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GrpcService
public class GrpcAuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(GrpcAuthServiceImpl.class);

    private final JwtTokenService jwtTokenService;
    private final UserMapperService userMapperService;

    @Autowired
    public GrpcAuthServiceImpl(JwtTokenService jwtTokenService, UserMapperService userMapperService) {
        this.jwtTokenService = jwtTokenService;
        this.userMapperService = userMapperService;
    }

    @Override
    public void validateToken(TokenValidationRequest request,
                              StreamObserver<TokenValidationResponse> responseObserver) {
        logger.info("Received token validation request: {}", request.getMetadata().getRequestId());

        TokenValidationResponse.Builder responseBuilder = TokenValidationResponse.newBuilder();
        String token = request.getToken();
        boolean includeUserData = request.getIncludeUserData();

        try {
            // Just validate access tokens for now (security reasons)
            boolean isValid = jwtTokenService.validateToken(token, "access");
            responseBuilder.setValid(isValid);

            if (isValid && includeUserData) {
                UserData userData = userMapperService.getUserDataFromToken(token);
                responseBuilder.setUserData(userData);
            }

            if (isValid) {
                responseBuilder.setStatus(ResponseStatusUtil.createSuccessStatus());
            } else {
                throw new IllegalArgumentException("Invalid token");
            }
        } catch (Exception e) {
            logger.error("Error validating token", e);
            responseBuilder.setValid(false);
            responseBuilder.setStatus(ResponseStatusUtil.createErrorStatus(e));
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void refreshToken(TokenRefreshRequest request, StreamObserver<TokenRefreshResponse> responseObserver) {
        logger.info("Received token refresh request: {}", request.getMetadata().getRequestId());

        TokenRefreshResponse.Builder responseBuilder = TokenRefreshResponse.newBuilder();
        String refreshToken = request.getRefreshToken();

        try {
            JwtTokenService.TokenPair newTokenPair = jwtTokenService.refreshToken(refreshToken);

            responseBuilder.setAccessToken(newTokenPair.accessToken())
                    .setRefreshToken(newTokenPair.refreshToken())
                    .setStatus(ResponseStatusUtil.createSuccessStatus());
        } catch (Exception e) {
            logger.error("Error refreshing token", e);
            responseBuilder.setStatus(ResponseStatusUtil.createErrorStatus(e));
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void lookupUser(UserLookupRequest request, StreamObserver<UserLookupResponse> responseObserver) {
        logger.info("Received user lookup request: {}", request.getMetadata().getRequestId());

        UserLookupResponse.Builder responseBuilder = UserLookupResponse.newBuilder();

        try {
            UserData userData = switch (request.getIdentifierCase()) {
                case USER_ID -> userMapperService.getUserById(UUID.fromString(request.getUserId()));
                case EMAIL -> userMapperService.getUserByEmail(request.getEmail());
                default -> throw new IllegalArgumentException("Invalid identifier type");
            };

            responseBuilder.setUserData(userData)
                    .setStatus(ResponseStatusUtil.createSuccessStatus());
        } catch (Exception e) {
            logger.error("Error looking up user", e);
            responseBuilder.setStatus(ResponseStatusUtil.createErrorStatus(e));
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void batchLookupUsers(BatchUserLookupRequest request,
                                 StreamObserver<BatchUserLookupResponse> responseObserver) {
        logger.info("Received batch user lookup request: {}", request.getMetadata().getRequestId());

        BatchUserLookupResponse.Builder responseBuilder = BatchUserLookupResponse.newBuilder();
        List<UserLookupResult> results = new ArrayList<>();
        int totalFound = 0;
        int totalNotFound = 0;

        boolean includeProfile = request.getIncludeProfile();

        try {
            for (UserIdentifier identifier : request.getIdentifiersList()) {
                UserLookupResult.Builder resultBuilder = UserLookupResult.newBuilder()
                        .setRequestedIdentifier(identifier);

                try {
                    UserData userData = switch (identifier.getIdentifierCase()) {
                        case USER_ID -> userMapperService.getUserById(UUID.fromString(identifier.getUserId()),
                                includeProfile);
                        case EMAIL -> userMapperService.getUserByEmail(identifier.getEmail(),
                                includeProfile);
                        default -> throw new IllegalArgumentException("Invalid identifier type");
                    };

                    resultBuilder.setFound(true)
                            .setUserData(userData);
                    totalFound++;
                } catch (Exception e) {
                    logger.debug("User not found for identifier: {}", identifier, e);
                    resultBuilder.setFound(false)
                            .setError(ErrorDetail.newBuilder()
                                    .setDescription(e.getMessage())
                                    .build());
                    totalNotFound++;
                }

                results.add(resultBuilder.build());
            }

            responseBuilder.addAllResults(results)
                    .setTotalFound(totalFound)
                    .setTotalNotFound(totalNotFound)
                    .setStatus(ResponseStatusUtil.createSuccessStatus());
        } catch (Exception e) {
            logger.error("Error processing batch user lookup", e);
            responseBuilder.setStatus(ResponseStatusUtil.createErrorStatus(e));
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void checkHealth(Empty request, StreamObserver<HealthCheckResponse> responseObserver) {
        logger.debug("Received health check request");

        HealthCheckResponse response = HealthCheckResponse.newBuilder()
                .setStatus(HealthCheckResponse.ServingStatus.SERVING_STATUS_SERVING)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
