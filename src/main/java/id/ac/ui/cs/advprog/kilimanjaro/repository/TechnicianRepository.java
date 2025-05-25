package id.ac.ui.cs.advprog.kilimanjaro.repository;

import id.ac.ui.cs.advprog.kilimanjaro.model.Technician;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface TechnicianRepository extends JpaRepository<Technician, UUID> {
    @NonNull
    Optional<Technician> findById(@NonNull @NotBlank @Size(max = 100) UUID id);
}