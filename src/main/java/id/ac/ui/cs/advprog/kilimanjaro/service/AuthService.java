package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.dto.*;

public interface AuthService {
    GenericResponse<Void> registerCustomer(RegisterCustomerRequest registerRequest);
    GenericResponse<Void> registerTechnician(RegisterTechnicianRequest registerRequest);
    GenericResponse<LoginResponse> login(LoginRequest loginRequest);
    void logout(String token);
}
