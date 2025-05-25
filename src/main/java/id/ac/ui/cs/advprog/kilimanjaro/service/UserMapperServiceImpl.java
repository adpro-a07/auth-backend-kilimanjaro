package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.AuthenticationException;
import id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions.UserNotFoundException;
import id.ac.ui.cs.advprog.kilimanjaro.mapper.UserDataMapper;
import id.ac.ui.cs.advprog.kilimanjaro.mapper.UserDataMapperFactory;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserMapperServiceImpl implements UserMapperService {
    private static final Logger logger = LoggerFactory.getLogger(UserMapperServiceImpl.class);

    private final UserRepository userRepository;
    private final UserDataMapperFactory mapperFactory;
    private final JwtTokenService jwtTokenService;
    private final Random random = new Random();

    @Autowired
    public UserMapperServiceImpl(UserRepository userRepository,
                                 UserDataMapperFactory mapperFactory,
                                 JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.mapperFactory = mapperFactory;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public UserData getUserById(UUID userId) throws UserNotFoundException {
        logger.info("Fetching user with ID: {}", userId);
        BaseUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        UserDataMapper<BaseUser> mapper = mapperFactory.getMapper(user.getRole());
        return mapper.toUserData(user);
    }

    @Override
    public UserData getUserById(UUID userId, boolean includeProfile) throws UserNotFoundException {
        logger.info("Fetching user with ID: {} and includeProfile: {}", userId, includeProfile);
        BaseUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        UserDataMapper<BaseUser> mapper = mapperFactory.getMapper(user.getRole());
        return mapper.toUserData(user, includeProfile);
    }

    @Override
    public UserData getUserByEmail(String email) throws UserNotFoundException {
        logger.info("Fetching user with email: {}", email);
        BaseUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        UserDataMapper<BaseUser> mapper = mapperFactory.getMapper(user.getRole());
        return mapper.toUserData(user);
    }

    @Override
    public UserData getUserByEmail(String email, boolean includeProfile) throws UserNotFoundException {
        logger.info("Fetching user with email: {} and includeProfile: {}", email, includeProfile);
        BaseUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        UserDataMapper<BaseUser> mapper = mapperFactory.getMapper(user.getRole());
        return mapper.toUserData(user, includeProfile);
    }

    @Override
    public UserData getUserDataFromToken(String token) throws AuthenticationException, UserNotFoundException {
        logger.info("Fetching user data from token: {}", token);
        BaseUser user = jwtTokenService.getUserFromToken(token);
        if (user == null) {
            throw new UserNotFoundException("User not found in token");
        }

        UserDataMapper<BaseUser> mapper = mapperFactory.getMapper(user.getRole());
        return mapper.toUserData(user);
    }

    @Override
    public List<UserData> listUsers(UserRole role, int pageSize, int pageNumber) {
        logger.info("Listing users with role: {}, pageSize: {}, pageNumber: {}",
                role, pageSize, pageNumber);

        // Apply default values if needed
        pageSize = (pageSize <= 0) ? 10 : pageSize;
        pageNumber = Math.max(pageNumber, 0);

        // Create sort direction
        Sort.Direction direction = Sort.Direction.ASC;
        String sortBy = "email"; // Default sort field

        // Create pageable request with sorting
        Pageable pageable= PageRequest.of(pageNumber, pageSize, direction, sortBy);

        // Get users from repository
        Page<BaseUser> userPage;
        id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole domainRole = getDomainRole(role);
        if (domainRole == null) {
            userPage = userRepository.findAllNotAdmin(pageable);
        } else {
            userPage = userRepository.findAllByRoleAndNotAdmin(domainRole.name(), pageable);
        }

        // Convert to UserData list
        return userPage.getContent().stream()
                .map(user -> {
                    UserDataMapper<BaseUser> mapper = mapperFactory.getMapper(user.getRole());
                    return mapper.toUserData(user, true); // Include profile data by default
                })
                .collect(Collectors.toList());
    }

    @Override
    public int countUsersByRole(UserRole role) {
        logger.info("Counting users with role: {}", role);

        id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole domainRole = getDomainRole(role);
        if (domainRole == null) {
            return (int) userRepository.countNotAdmin();
        } else {
            return (int) userRepository.countByRoleAndNotAdmin(domainRole.name());
        }
    }

    @Override
    public UserData getRandomTechnician() {
        logger.info("Getting random technician");

        // Get all technicians
        List<BaseUser> technicians = userRepository.findAllByRoleAndNotAdmin(
                id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole.TECHNICIAN.name()
        );

        if (technicians.isEmpty()) {
            logger.warn("No technicians found in the system");
            throw new UserNotFoundException("No technicians available");
        }

        // Select a random technician
        BaseUser randomTechnician = technicians.get(random.nextInt(technicians.size()));

        // Convert to UserData
        UserDataMapper<BaseUser> mapper = mapperFactory.getMapper(randomTechnician.getRole());
        return mapper.toUserData(randomTechnician, true); // Include profile data
    }

    private id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole getDomainRole(UserRole role) {
        try {
            if (role == null ||
                    role == UserRole.UNSPECIFIED ||
                    role == UserRole.UNRECOGNIZED ||
                    role == UserRole.ADMIN
            ) {
                return null;
            }
            return id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole.valueOf(role.name());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid role: {}", role, e);
            return null;
        }
    }
}