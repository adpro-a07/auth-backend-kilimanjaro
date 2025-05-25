package id.ac.ui.cs.advprog.kilimanjaro.mapper;

import id.ac.ui.cs.advprog.kilimanjaro.model.BaseUser;
import id.ac.ui.cs.advprog.kilimanjaro.model.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserDataMapperFactoryImpl implements UserDataMapperFactory{
    private final Map<UserRole, UserDataMapper<? extends BaseUser>> mapperMap = new HashMap<>();

    @Autowired
    public UserDataMapperFactoryImpl(List<UserDataMapper<? extends BaseUser>> mappers) {
        for (UserDataMapper<? extends BaseUser> mapper : mappers) {
            mapperMap.put(mapper.supportsRole(), mapper);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BaseUser> UserDataMapper<T> getMapper(UserRole role) {
        return (UserDataMapper<T>) mapperMap.get(role);
    }
}
