package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.model.Admin;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import id.ac.ui.cs.advprog.kilimanjaro.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void testLoadUserByUsername_UserExists_ReturnsUserDetails() {
        // Arrange
        Admin mockUser = new Admin.AdminBuilder()
                .fullName("Super Admin")
                .email("admin@example.com")
                .phoneNumber("0811223344")
                .password("verySecurePassword")
                .build();

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(mockUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@example.com");

        // Assert
        assertEquals("admin@example.com", userDetails.getUsername());
        assertEquals("verySecurePassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_" + UserRole.ADMIN.name())));
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }


    @Test
    void testLoadUserByUsername_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("missing@example.com")
        );
    }
}
