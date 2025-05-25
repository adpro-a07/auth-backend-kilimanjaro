package id.ac.ui.cs.advprog.kilimanjaro.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UserResponseDto {
    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private String address;
    private String experience;
    private Integer totalJobsDone;
    private Long totalIncome;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
