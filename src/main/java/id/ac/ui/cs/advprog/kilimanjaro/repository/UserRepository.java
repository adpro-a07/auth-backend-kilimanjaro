package id.ac.ui.cs.advprog.kilimanjaro.repository;

import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<BaseUser, Long> {
    Optional<BaseUser> findByEmail(@NotBlank @Email @Size(max = 100) String email);

    Optional<BaseUser> findById(@NotBlank @Size(max = 100) UUID id);

    @Query(value = "SELECT * FROM users WHERE role = :role AND role != 'ADMIN'", nativeQuery = true)
    List<BaseUser> findAllByRoleAndNotAdmin(@Param("role") String role);

    @Query(value = "SELECT * FROM users WHERE role = :role AND role != 'ADMIN'", nativeQuery = true)
    Page<BaseUser> findAllByRoleAndNotAdmin(@Param("role") String role, Pageable pageable);

    @Query(value = "SELECT * FROM users WHERE role != 'ADMIN'", nativeQuery = true)
    Page<BaseUser> findAllNotAdmin(Pageable pageable);

    Boolean existsByEmail(@NotBlank @Email @Size(max = 100) String email);

    Boolean existsById(@NotBlank @Size(max = 100) UUID id);

    @Query(value = "SELECT COUNT(*) FROM users WHERE role = :role AND role != 'ADMIN'", nativeQuery = true)
    long countByRoleAndNotAdmin(@Param("role") String role);

    @Query(value = "SELECT COUNT(*) FROM users WHERE role != 'ADMIN'", nativeQuery = true)
    long countNotAdmin();
}
