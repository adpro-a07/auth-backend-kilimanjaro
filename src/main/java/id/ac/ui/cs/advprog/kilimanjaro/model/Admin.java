package id.ac.ui.cs.advprog.kilimanjaro.model;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserProfile;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends BaseUser {
    protected Admin() {
        super(); // Required by JPA for reflection
    }

    @Override
    public UserProfile getProfile() {
        return UserProfile.newBuilder().build();
    }

    private Admin(AdminBuilder builder) {
        super(builder);
    }

    public static class AdminBuilder extends BaseUser.BaseUserBuilder<Admin.AdminBuilder> {
        @Override
        public Admin build() {
            return new Admin(this);
        }

        @Override
        protected Admin.AdminBuilder self() {
            return this;
        }
    }
}
