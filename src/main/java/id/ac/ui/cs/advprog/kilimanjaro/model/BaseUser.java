package id.ac.ui.cs.advprog.kilimanjaro.model;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserProfile;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BaseUser is an abstract class that represents a user in the system.
 * It contains common attributes and methods for all user types.
 */
@Getter
@Setter
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseUser {
    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(max = 15)
    @Column(nullable = false)
    private String phoneNumber;

    @NotBlank
    @Size(min = 8, max = 100)
    @Column(nullable = false)
    private String password;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Default constructor for JPA
    protected BaseUser() {}

    protected BaseUser(BaseUserBuilder<?> builder) {
        if (builder.fullName == null || builder.email == null
                || builder.phoneNumber == null || builder.password == null) {
            throw new IllegalArgumentException("All fields must be non-null");
        }
        this.id = builder.id;
        this.fullName = builder.fullName;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.password = builder.password;
    }

    @Transient
    public UserRole getRole() {
        return UserRole.valueOf(this.getClass()
                .getAnnotation(DiscriminatorValue.class)
                .value()
                .toUpperCase());
    }

    public abstract UserProfile getProfile();

    /**
     * Abstract Builder pattern implementation for BaseUser.
     * @param <T> The type of builder (for subclass builders)
     */
    public static abstract class BaseUserBuilder<T extends BaseUserBuilder<T>> {
        private UUID id;
        private String fullName;
        private String email;
        private String phoneNumber;
        private String password;

        public BaseUserBuilder() {}

        public T id(UUID id) {
            this.id = id;
            return self();
        }

        public T fullName(String fullName) {
            this.fullName = fullName;
            return self();
        }

        public T email(String email) {
            this.email = email;
            return self();
        }

        public T phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return self();
        }

        public T password(String password) {
            this.password = password;
            return self();
        }

        protected abstract T self();

        public abstract BaseUser build();
    }
}