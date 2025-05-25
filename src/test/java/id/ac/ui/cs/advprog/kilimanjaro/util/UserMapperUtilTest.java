package id.ac.ui.cs.advprog.kilimanjaro.util;

import com.google.protobuf.Timestamp;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserIdentity;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserMapperUtilTest {

    @Test
    void testConvertLocalDateTimeToTimestamp() {
        LocalDateTime now = LocalDateTime.of(2024, 5, 5, 12, 30, 15);
        Timestamp timestamp = UserMapperUtil.convertLocalDateTimeToTimestamp(now);

        assertNotNull(timestamp);
        assertEquals(now.getSecond(), timestamp.getSeconds() % 60);
    }

    @Test
    void testExtractUserIdentity() {
        // Arrange
        BaseUser user = mock(BaseUser.class);
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        when(user.getId()).thenReturn(id);
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getFullName()).thenReturn("Test User");
        when(user.getPhoneNumber()).thenReturn("1234567890");
        when(user.getRole()).thenReturn(UserRole.CUSTOMER);
        when(user.getCreatedAt()).thenReturn(createdAt);
        when(user.getUpdatedAt()).thenReturn(updatedAt);

        // Act
        UserIdentity identity = UserMapperUtil.extractUserIdentity(user);

        // Assert
        assertEquals(id.toString(), identity.getId());
        assertEquals("test@example.com", identity.getEmail());
        assertEquals("Test User", identity.getFullName());
        assertEquals("1234567890", identity.getPhoneNumber());
        assertEquals("CUSTOMER", identity.getRole().name());
        assertEquals(createdAt.getSecond(), identity.getCreatedAt().getSeconds() % 60);
        assertEquals(updatedAt.getSecond(), identity.getUpdatedAt().getSeconds() % 60);
    }
}
