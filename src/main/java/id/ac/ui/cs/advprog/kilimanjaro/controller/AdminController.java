package id.ac.ui.cs.advprog.kilimanjaro.controller;

import id.ac.ui.cs.advprog.kilimanjaro.dto.GenericResponse;
import id.ac.ui.cs.advprog.kilimanjaro.dto.RegisterTechnicianRequest;
import id.ac.ui.cs.advprog.kilimanjaro.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/technicians")
    public ResponseEntity<?> registerTechnician(@Valid @RequestBody RegisterTechnicianRequest registerRequest) {
        GenericResponse<Void> response = authService.registerTechnician(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }}
