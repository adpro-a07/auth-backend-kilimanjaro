package id.ac.ui.cs.advprog.kilimanjaro.model;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserProfile;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Technician is a type of user that performs jobs and earns income.
 * It extends the BaseUser class with additional fields for address, experience, job tracking and income.
 */
@Entity
@DiscriminatorValue("TECHNICIAN")
@Getter
@Setter
public class Technician extends BaseUser {
    @Size(max = 200)
    @Column(length = 200)
    private String address;

    @Size(max = 500)
    @Column(length = 500)
    private String experience;

    @Column()
    private Integer totalJobsDone = 0;

    @Column()
    private Long totalIncome = 0L;

    public void addJobDone(int count, long income) {
        if (count <= 0 || income < 0) {
            throw new IllegalArgumentException("Count must be positive and income must be non-negative");
        }
        this.totalJobsDone = (this.totalJobsDone == null ? 0 : this.totalJobsDone) + count;
        this.totalIncome = (this.totalIncome == null ? 0L : this.totalIncome) + income;
    }

    protected Technician() {
        super();
    }

    @Override
    public UserProfile getProfile() {
        return UserProfile.newBuilder()
                .setAddress(this.address)
                .setWorkExperience(this.experience)
                .setTotalJobsDone(this.totalJobsDone)
                .setTotalIncome(this.totalIncome)
                .build();
    }

    private Technician(TechnicianBuilder builder) {
        super(builder);
        this.address = builder.address;
        this.experience = builder.experience;
        this.totalJobsDone = 0;
        this.totalIncome = 0L;
    }

    public static class TechnicianBuilder extends BaseUserBuilder<TechnicianBuilder> {
        private String address;
        private String experience;

        public TechnicianBuilder address(String address) {
            this.address = address;
            return this;
        }

        public TechnicianBuilder experience(String experience) {
            this.experience = experience;
            return this;
        }

        @Override
        public Technician build() {
            return new Technician(this);
        }

        @Override
        protected TechnicianBuilder self() {
            return this;
        }
    }
}
