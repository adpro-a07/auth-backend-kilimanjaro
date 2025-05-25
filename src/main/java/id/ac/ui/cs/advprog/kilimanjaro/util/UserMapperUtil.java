package id.ac.ui.cs.advprog.kilimanjaro.util;

import com.google.protobuf.Timestamp;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserIdentity;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;

import java.time.Instant;
import java.time.LocalDateTime;

public class UserMapperUtil {
    public static UserIdentity extractUserIdentity(BaseUser baseUser) {
        return UserIdentity.newBuilder()
                .setId(baseUser.getId().toString())
                .setEmail(baseUser.getEmail())
                .setFullName(baseUser.getFullName())
                .setPhoneNumber(baseUser.getPhoneNumber())
                .setRole(UserRole.valueOf(baseUser.getRole().name()))
                .setCreatedAt(convertLocalDateTimeToTimestamp(baseUser.getCreatedAt()))
                .setUpdatedAt(convertLocalDateTimeToTimestamp(baseUser.getUpdatedAt()))
                .build();
    }

    public static Timestamp convertLocalDateTimeToTimestamp(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
