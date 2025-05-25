package id.ac.ui.cs.advprog.kilimanjaro.dto;

import lombok.Getter;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Getter
public class GenericResponse<T> {
    private final boolean success;
    private final String message;
    private final String timestamp;
    private final T data;

    public GenericResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = formatTimestamp(Instant.now());
    }

    private String formatTimestamp(Instant timestamp) {
        return DateTimeFormatter.ISO_ZONED_DATE_TIME
                .withZone(ZoneOffset.UTC)
                .format(timestamp);
    }
}
