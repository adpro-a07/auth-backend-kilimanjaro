package id.ac.ui.cs.advprog.kilimanjaro.controller;

import id.ac.ui.cs.advprog.kilimanjaro.dto.GenericResponse;
import id.ac.ui.cs.advprog.kilimanjaro.dto.LoginResponse;
import id.ac.ui.cs.advprog.kilimanjaro.dto.LoginRequest;
import id.ac.ui.cs.advprog.kilimanjaro.dto.RegisterCustomerRequest;
import id.ac.ui.cs.advprog.kilimanjaro.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody RegisterCustomerRequest registerRequest) {
        GenericResponse<Void> response = authService.registerCustomer(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        GenericResponse<LoginResponse> response = authService.login(loginRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
