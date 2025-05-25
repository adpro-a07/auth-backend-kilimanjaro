package id.ac.ui.cs.advprog.kilimanjaro.mapper;

import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;

public interface UserDataMapperFactory {
    <T extends BaseUser> UserDataMapper<T> getMapper(UserRole role);
}
