package id.ac.ui.cs.advprog.kilimanjaro.mapper;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserProfile;
import id.ac.ui.cs.advprog.kilimanjaro.model.Admin;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class AdminUserDataMapper extends AbstractUserDataMapper<Admin> implements UserDataMapper<Admin> {
    @Override
    public UserRole supportsRole() {
        return UserRole.ADMIN;
    }

    protected UserProfile buildProfile(Admin user) {
        return UserProfile.newBuilder()
                .build();
    }
}
