package petproekt.task_management_system.dto;

import java.util.Set;
import java.util.UUID;

public record UserInfoDto(
        UUID id,
        String username,
        String maskedPassword,
        Set<String> roles
) {}
