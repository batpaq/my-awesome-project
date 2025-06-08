package petproekt.task_management_system.scheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import petproekt.task_management_system.enm.TokenType;
import petproekt.task_management_system.service.UserTokenService;

@Component
public class TokenCleanupTask {

    private final UserTokenService userTokenService;

    public TokenCleanupTask(UserTokenService userTokenService) {
        this.userTokenService = userTokenService;
    }

    // Каждые 6 часов запускаем очистку токенов старше 24 часов (1440 минут)
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
    public void cleanExpiredTokens() {
        userTokenService.removeExpiredTokens(1440, TokenType.REFRESH);
    }
}
