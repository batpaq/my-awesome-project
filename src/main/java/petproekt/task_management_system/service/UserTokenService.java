package petproekt.task_management_system.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import petproekt.task_management_system.dao.UserTokenRepository;
import petproekt.task_management_system.enm.TokenType;
import petproekt.task_management_system.entity.UserToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserTokenService {

    private final UserTokenRepository userTokenRepository;

    public UserTokenService(UserTokenRepository userTokenRepository) {
        this.userTokenRepository = userTokenRepository;
    }

    /**
     * Проверяет, действителен ли токен: не отозван, не просрочен.
     *
     * @param token             JWT токен
     * @param expirationMinutes Время жизни токена в минутах
     * @return true, если токен существует, не отозван и не просрочен
     */
    @Transactional
    public boolean isTokenValid(String token, long expirationMinutes) {
        return userTokenRepository.findByToken(token)
                .filter(userToken -> !userToken.isRevoked())
                .filter(userToken -> LocalDateTime.now().isBefore(userToken.getCreatedAt().plusMinutes(expirationMinutes)))
                .isPresent();
    }

    /**
     * Удаляет все истёкшие токены определённого типа (например, только REFRESH).
     *
     * @param expirationMinutes Время жизни токена
     * @param tokenType         Тип токена (например, REFRESH)
     */
    @Transactional
    public void removeExpiredTokens(long expirationMinutes, TokenType tokenType) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(expirationMinutes);
        List<UserToken> expiredTokens = userTokenRepository.findAllByTokenTypeAndCreatedAtBefore(tokenType, cutoff);
        userTokenRepository.deleteAll(expiredTokens);
    }

    /**
     * Отзывает токен (не удаляет физически, только помечает как недействительный).
     *
     * @param token JWT токен
     */
    @Transactional
    public void revokeToken(String token) {
        userTokenRepository.findByToken(token).ifPresent(userToken -> {
            userToken.setRevoked(true);
            userTokenRepository.save(userToken);
        });
    }

    /**
     * Отзывает все токены пользователя заданного типа (например, все REFRESH токены при logout).
     *
     * @param userId    ID пользователя
     * @param tokenType Тип токена
     */
    @Transactional
    public void revokeAllTokensByUserAndType(UUID userId, TokenType tokenType) {
        userTokenRepository.findAll().stream()
                .filter(token -> token.getUser().getId().equals(userId))
                .filter(token -> token.getTokenType() == tokenType)
                .forEach(token -> {
                    token.setRevoked(true);
                    userTokenRepository.save(token);
                });
    }

    /**
     * Заменяет старый токен новым (например, при рефреше).
     *
     * @param oldToken старый JWT
     * @param newToken новый JWT
     */
    @Transactional
    public void replaceToken(String oldToken, String newToken) {
        userTokenRepository.findByToken(oldToken).ifPresent(userToken -> {
            userToken.setToken(newToken);
            userToken.setRevoked(false);
            userToken.setCreatedAt(LocalDateTime.now());
            userTokenRepository.save(userToken);
        });
    }

    /**
     * Создаёт и сохраняет новый токен.
     */
    @Transactional
    public void saveToken(UserToken token) {
        userTokenRepository.save(token);
    }
}
