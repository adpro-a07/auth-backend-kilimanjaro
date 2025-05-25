package id.ac.ui.cs.advprog.kilimanjaro.repository;

import id.ac.ui.cs.advprog.kilimanjaro.model.*;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail() {
        Customer customer = new Customer.CustomerBuilder()
                .fullName("John Doe")
                .email("test@example.com")
                .phoneNumber("123456789")
                .password("password")
                .address("123 Main St")
                .build();

        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        userRepository.save(customer);

        Optional<BaseUser> found = userRepository.findByEmail("test@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
        assertThat(found.get().getPhoneNumber()).isEqualTo("123456789");
        assertThat(found.get().getPassword()).isEqualTo("password"); // Password should be hashed
        assertThat(found.get().getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(found.get()).isInstanceOf(Customer.class);
    }

    @Test
    @DisplayName("Should return only non-admin users with specific role")
    void testFindAllByRoleAndNotAdmin() {
        Technician technician = new Technician.TechnicianBuilder()
                .fullName("Jane Doe")
                .email("tech@example.com")
                .phoneNumber("987654321")
                .password("password")
                .address("456 Elm St")
                .experience("5 years")
                .build();

        technician.setCreatedAt(LocalDateTime.now());
        technician.setUpdatedAt(LocalDateTime.now());

        Admin admin = new Admin.AdminBuilder()
                .fullName("Admin User")
                .email("admin@example.com")
                .phoneNumber("555555555")
                .password("adminpassword")
                .build();

        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        userRepository.saveAll(List.of(technician, admin));

        List<BaseUser> results = userRepository.findAllByRoleAndNotAdmin("TECHNICIAN");
        assertThat(results).hasSize(1);
        assertThat(results.getFirst()).isInstanceOf(Technician.class);
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void testExistsByEmail() {
        Customer customer = new Customer.CustomerBuilder()
                .fullName("John Doe")
                .email("exists@example.com")
                .phoneNumber("123456789")
                .password("password")
                .address("123 Main St")
                .build();

        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        userRepository.save(customer);

        boolean exists = userRepository.existsByEmail("exists@example.com");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should paginate non-admin users")
    void testFindAllNotAdminPaged() {
        for (int i = 0; i < 5; i++) {
            Customer customer = new Customer.CustomerBuilder()
                    .fullName("John Doe")
                    .email("cust" + i + "@example.com")
                    .phoneNumber("123456789")
                    .password("password")
                    .address("123 Main St")
                    .build();

            customer.setCreatedAt(LocalDateTime.now());
            customer.setUpdatedAt(LocalDateTime.now());

            userRepository.save(customer);
        }

        PageRequest page = PageRequest.of(0, 3);
        var results = userRepository.findAllNotAdmin(page);
        assertThat(results.getTotalElements()).isEqualTo(5);
        assertThat(results.getContent()).hasSize(3); // page size
    }
}
