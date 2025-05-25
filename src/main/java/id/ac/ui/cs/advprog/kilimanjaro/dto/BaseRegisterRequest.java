package id.ac.ui.cs.advprog.kilimanjaro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class BaseRegisterRequest {
    @NotBlank(message = "Fullname is required")
    @Size(min = 3, max = 100, message = "Fullname must be between 3 and 100 characters")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Fullname should contain only letters, spaces, and basic punctuation")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(min = 8, max = 15, message = "Phone number must be between 8 and 15 characters")
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Phone number must contain only numbers with optional + prefix and be between 8-15 digits")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$",
            message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character")
    private String password1;

    @NotBlank(message = "Password confirmation is required")
    private String password2;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
    private String address;
}
