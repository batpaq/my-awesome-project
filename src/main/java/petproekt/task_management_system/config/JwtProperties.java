package petproekt.task_management_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Время жизни access токена в миллисекундах
     */
    private long accessTokenExpiration;

    /**
     * Время жизни refresh токена в миллисекундах
     */
    private long refreshTokenExpiration;

    /**
     * Секрет для access токена (необязательно)
     */
    private String accessTokenSecret;

    /**
     * Секрет для refresh токена (необязательно)
     */
    private String refreshTokenSecret;
}
