package id.ac.ui.cs.advprog.kilimanjaro.mapper;

import id.ac.ui.cs.advprog.kilimanjaro.auth.grpc.UserData;
import id.ac.ui.cs.advprog.kilimanjaro.dto.UserResponseDto;
import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;

public interface UserDataMapper<T extends BaseUser> {
    UserData toUserData(T user);

    UserData toUserData(T user, boolean includeProfile);

    UserResponseDto toUserResponseDto(T user);

    UserRole supportsRole();
}
