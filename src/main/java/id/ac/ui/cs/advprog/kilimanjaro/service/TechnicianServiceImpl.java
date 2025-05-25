package id.ac.ui.cs.advprog.kilimanjaro.service;

import id.ac.ui.cs.advprog.kilimanjaro.model.Technician;
import id.ac.ui.cs.advprog.kilimanjaro.repository.TechnicianRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TechnicianServiceImpl implements TechnicianService {
    private final TechnicianRepository technicianRepository;

    @Override
    @Async
    @Transactional
    public void updateStats(UUID technicianId, Long amount) {
        if (technicianId == null) {
            throw new NullPointerException("Technician ID cannot be null");
        }

        Optional<Technician> technician = technicianRepository.findById(technicianId);

        if (technician.isEmpty()) {
            throw new IllegalArgumentException("Technician not found");
        }

        Technician tech = technician.get();

        tech.setTotalJobsDone(tech.getTotalJobsDone() + 1);
        tech.setTotalIncome(tech.getTotalIncome() + amount);

        technicianRepository.save(tech);
    }
}
