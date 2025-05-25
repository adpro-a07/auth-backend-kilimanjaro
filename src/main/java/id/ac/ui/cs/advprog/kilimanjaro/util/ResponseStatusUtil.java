package id.ac.ui.cs.advprog.kilimanjaro.util;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.ResponseStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ResponseStatusUtil {
    /**
     * Create success status
     */
    public static ResponseStatus createSuccessStatus() {
        return ResponseStatus.newBuilder()
                .setCode(0)  // 0 = success
                .setMessage("Success")
                .setTraceId(generateTraceId())
                .build();
    }

    /**
     * Create error status with message
     */
    public static ResponseStatus createErrorStatus(String message) {
        return ResponseStatus.newBuilder()
                .setCode(1)  // Non-zero = error
                .setMessage(message)
                .setTraceId(generateTraceId())
                .build();
    }

    /**
     * Create error status from exception
     */
    public static ResponseStatus createErrorStatus(Exception e) {
        return ResponseStatus.newBuilder()
                .setCode(1)  // Non-zero = error
                .setMessage(e.getMessage())
                .setTraceId(generateTraceId())
                .build();
    }

    /**
     * Create error status with code and message
     */
    public static ResponseStatus createErrorStatus(int code, String message) {
        return ResponseStatus.newBuilder()
                .setCode(code)
                .setMessage(message)
                .setTraceId(generateTraceId())
                .build();
    }

    /**
     * Generate a random trace ID
     */
    private static String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}
