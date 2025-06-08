package petproekt.task_management_system.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import petproekt.task_management_system.entity.UserApp;
import petproekt.task_management_system.entity.UserToken;
import petproekt.task_management_system.enm.TokenType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с токенами пользователей.
 * Позволяет выполнять поиск, удаление и обновление токенов в базе данных.
 */
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    /**
     * Ищет токен по его значению.
     *
     * @param token JWT токен
     * @return Optional с найденным токеном или пустой, если токен не найден
     */
    Optional<UserToken> findByToken(String token);

    /**
     * Возвращает все токены пользователя указанного типа.
     *
     * @param user пользователь
     * @param tokenType тип токена (например, ACCESS или REFRESH)
     * @return список токенов
     */
    List<UserToken> findAllByUserAndTokenType(UserApp user, TokenType tokenType);

    /**
     * Возвращает все токены, связанные с пользователем.
     *
     * @param user пользователь
     * @return список всех токенов пользователя
     */
    List<UserToken> findAllByUser(UserApp user);

    /**
     * Ищет токены указанного типа, созданные до определённого времени (используется для очистки старых токенов).
     *
     * @param tokenType тип токена
     * @param cutoff дата-время, до которого токены считаются устаревшими
     * @return список устаревших токенов
     */
    List<UserToken> findAllByTokenTypeAndCreatedAtBefore(TokenType tokenType, LocalDateTime cutoff);

    /**
     * Возвращает все НЕотозванные токены пользователя определённого типа (активные токены).
     *
     * @param user пользователь
     * @param tokenType тип токена
     * @return список активных токенов
     */
    List<UserToken> findAllByUserAndTokenTypeAndRevokedFalse(UserApp user, TokenType tokenType);

    /**
     * Удаляет все токены, созданные раньше указанной даты (используется для автоматической очистки).
     *
     * @param cutoff дата-время, до которой токены будут удалены
     */
    @Modifying
    @Query("DELETE FROM UserToken ut WHERE ut.createdAt < :cutoff")
    void deleteAllByCreatedAtBefore(LocalDateTime cutoff);

    /**
     * Удаляет токен по его значению (используется при отзыве токена).
     *
     * @param token значение JWT токена
     */
    @Modifying
    @Query("DELETE FROM UserToken ut WHERE ut.token = :token")
    void deleteByToken(String token);
}
