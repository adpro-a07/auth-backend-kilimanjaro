package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.InvalidCredentialsException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserAlreadyExistsException;
import id.ac.ui.cs.advprog.kilimanjaro.dto.*;
import id.ac.ui.cs.advprog.kilimanjaro.mapper.UserDataMapper;
import id.ac.ui.cs.advprog.kilimanjaro.mapper.UserDataMapperFactory;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.Customer;
import id.ac.ui.cs.advprog.kilimanjaro.model.Technician;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserDataMapperFactory mapperFactory;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenService jwtTokenService,
                           AuthenticationManager authenticationManager,
                           UserDataMapperFactory mapperFactory
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public GenericResponse<Void> registerCustomer(RegisterCustomerRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        // Validate password match
        if (!registerRequest.getPassword1().equals(registerRequest.getPassword2())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        Customer newCustomer = new Customer.CustomerBuilder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .password(passwordEncoder.encode(registerRequest.getPassword1()))
                .address(registerRequest.getAddress())
                .build();

        userRepository.save(newCustomer);

        return new GenericResponse<>(
                true,
                "Registration successful",
                null
        );
    }

    @Override
    public GenericResponse<Void> registerTechnician(RegisterTechnicianRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        // Validate password match
        if (!registerRequest.getPassword1().equals(registerRequest.getPassword2())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Check if the experience field is empty
        if (registerRequest.getExperience() == null || registerRequest.getExperience().isEmpty()) {
            throw new IllegalArgumentException("Experience is required");
        }

        Technician newTechnician = new Technician.TechnicianBuilder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .password(passwordEncoder.encode(registerRequest.getPassword1()))
                .address(registerRequest.getAddress())
                .experience(registerRequest.getExperience())
                .build();

        userRepository.save(newTechnician);

        return new GenericResponse<>(
                true,
                "Registration successful",
                null
        );
    }

    @Override
    public GenericResponse<LoginResponse> login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            String email = loginRequest.getEmail();
            JwtTokenService.TokenPair tokenPair = jwtTokenService.generateTokensFromEmail(email);

            // Retrieve user instance (domain object) from your user service
            BaseUser user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserDataMapper<BaseUser> mapper = mapperFactory.getMapper(user.getRole());
            UserResponseDto userResponse = mapper.toUserResponseDto(user);

            return new GenericResponse<>(
                    true,
                    "Login successful",
                    new LoginResponse(tokenPair.accessToken(), tokenPair.refreshToken(), userResponse)
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @Override
    public void logout(String token) {
        // Extract token from Bearer prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Invalidate the token
        jwtTokenService.invalidateToken(token);
    }
}