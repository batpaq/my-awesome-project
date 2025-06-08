package petproekt.task_management_system.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import petproekt.task_management_system.dto.UserInfoDto;
import petproekt.task_management_system.entity.UserApp;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserInfoMapper {

    @Mapping(target = ".", source = ".") // авто-маппинг остальных полей
    @Mapping(target = "roles", expression = "java(mapRoles(user))")
    @Mapping(target = "maskedPassword", expression = "java(maskPassword(user.getPassword()))")
    UserInfoDto toUserInfoDto(UserApp user);

    default Set<String> mapRoles(UserApp user) {
        return user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());
    }

    default String maskPassword(String password) {
        if (password == null || password.length() <= 4) {
            return "****";
        }
        int maskLength = password.length() - 4;
        StringBuilder sb = new StringBuilder();
        sb.append(password, 0, 2);
        for (int i = 0; i < maskLength; i++) {
            sb.append('*');
        }
        sb.append(password.substring(password.length() - 2));
        return sb.toString();
    }
}
