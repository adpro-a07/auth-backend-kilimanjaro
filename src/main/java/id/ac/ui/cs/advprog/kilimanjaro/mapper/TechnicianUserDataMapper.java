package id.ac.ui.cs.advprog.kilimanjaro.mapper;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserProfile;
import id.ac.ui.cs.advprog.kilimanjaro.dto.UserResponseDto;
import id.ac.ui.cs.advprog.kilimanjaro.model.Technician;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class TechnicianUserDataMapper extends AbstractUserDataMapper<Technician> implements UserDataMapper<Technician> {
    @Override
    public UserRole supportsRole() {
        return UserRole.TECHNICIAN;
    }

    @Override
    public UserResponseDto toUserResponseDto(Technician user) {
        UserResponseDto baseUserResponseDto = super.toUserResponseDto(user);

        baseUserResponseDto.setAddress(user.getAddress());
        baseUserResponseDto.setExperience(user.getExperience());
        baseUserResponseDto.setTotalJobsDone(user.getTotalJobsDone());
        baseUserResponseDto.setTotalIncome(user.getTotalIncome());

        return baseUserResponseDto;
    }

    protected UserProfile buildProfile(Technician user) {
        return UserProfile.newBuilder()
                .setAddress(user.getAddress())
                .setWorkExperience(user.getExperience())
                .setTotalJobsDone(user.getTotalJobsDone())
                .setTotalIncome(user.getTotalIncome())
                .build();
    }
}
