package id.ac.ui.cs.advprog.kilimanjaro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterTechnicianRequest extends BaseRegisterRequest {
    @NotBlank(message = "Experience is required")
    @Size(min = 5, max = 500, message = "Experience must be between 5 and 500 characters")
    private String experience;
}