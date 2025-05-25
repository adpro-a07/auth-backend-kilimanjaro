package id.ac.ui.cs.advprog.kilimanjaro.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private UserResponseDto user;

    public LoginResponse(String accessToken, String refreshToken, UserResponseDto user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }
}
