package id.ac.ui.cs.advprog.kilimanjaro.util;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.ResponseStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseStatusUtilTest {

    @Test
    public void testCreateSuccessStatus() {
        ResponseStatus status = ResponseStatusUtil.createSuccessStatus();

        assertThat(status.getCode()).isZero();
        assertThat(status.getMessage()).isEqualTo("Success");
        assertThat(status.getTraceId()).isNotBlank();
        assertThat(status.getTraceId()).hasSizeGreaterThan(0);
    }

    @Test
    public void testCreateErrorStatusWithMessage() {
        String errorMessage = "An error occurred";
        ResponseStatus status = ResponseStatusUtil.createErrorStatus(errorMessage);

        assertThat(status.getCode()).isEqualTo(1);
        assertThat(status.getMessage()).isEqualTo(errorMessage);
        assertThat(status.getTraceId()).isNotBlank();
    }

    @Test
    public void testCreateErrorStatusWithException() {
        Exception ex = new RuntimeException("Something went wrong");
        ResponseStatus status = ResponseStatusUtil.createErrorStatus(ex);

        assertThat(status.getCode()).isEqualTo(1);
        assertThat(status.getMessage()).isEqualTo("Something went wrong");
        assertThat(status.getTraceId()).isNotBlank();
    }

    @Test
    public void testCreateErrorStatusWithCustomCode() {
        int errorCode = 500;
        String errorMessage = "Internal Server Error";
        ResponseStatus status = ResponseStatusUtil.createErrorStatus(errorCode, errorMessage);

        assertThat(status.getCode()).isEqualTo(errorCode);
        assertThat(status.getMessage()).isEqualTo(errorMessage);
        assertThat(status.getTraceId()).isNotBlank();
    }
}
