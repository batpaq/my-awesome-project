package petproekt.task_management_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import petproekt.task_management_system.dao.UserRepository;
import petproekt.task_management_system.dao.UserTokenRepository;
import petproekt.task_management_system.dto.AuthRequest;
import petproekt.task_management_system.dto.AuthResponse;
import petproekt.task_management_system.dto.RegisterRequest;
import petproekt.task_management_system.entity.UserApp;
import petproekt.task_management_system.entity.UserToken;
import petproekt.task_management_system.enm.TokenType;
import petproekt.task_management_system.exception.UserAlreadyExistsException;
import petproekt.task_management_system.exception.UserNotFoundException;
import petproekt.task_management_system.security.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * Регистрация нового пользователя
     */
    @Override
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        UserApp user = UserApp.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        return "Пользователь успешно зарегистрирован";
    }

    /**
     * Аутентификация пользователя и генерация токенов
     */
    @Override
    @Transactional
    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Неверные имя пользователя или пароль");
        }

        UserApp user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        // Отзываем все предыдущие refresh токены
        List<UserToken> oldTokens = userTokenRepository.findAllByUserAndTokenTypeAndRevokedFalse(user, TokenType.REFRESH);
        oldTokens.forEach(t -> t.setRevoked(true));
        userTokenRepository.saveAll(oldTokens);

        // Генерация новых токенов
        String accessToken = jwtUtil.generateToken(user.getUsername(), TokenType.ACCESS);
        String refreshToken = jwtUtil.generateToken(user.getUsername(), TokenType.REFRESH);

        // Сохраняем новые токены в базе
        userTokenRepository.save(UserToken.builder()
                .token(accessToken)
                .user(user)
                .tokenType(TokenType.ACCESS)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build());

        userTokenRepository.save(UserToken.builder()
                .token(refreshToken)
                .user(user)
                .tokenType(TokenType.REFRESH)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build());

        return new AuthResponse(accessToken, refreshToken);
    }

    /**
     * Выход из системы: отзыв refresh токена
     */
    @Override
    @Transactional
    public void logout(String token) {
        UserToken userToken = userTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Неверный токен для выхода из системы"));

        if (userToken.getTokenType() != TokenType.REFRESH) {
            throw new IllegalArgumentException("Для выхода необходимо использовать refresh токен");
        }

        userToken.setRevoked(true);
        userTokenRepository.save(userToken);
    }

    /**
     * Обновление access токена с использованием refresh токена
     */
    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken, TokenType.REFRESH)) {
            throw new IllegalArgumentException("Неверный refresh токен");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken, TokenType.REFRESH);
        UserApp user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        UserToken storedToken = userTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Токен не найден"));

        if (storedToken.isRevoked()) {
            throw new IllegalArgumentException("Refresh токен уже отозван");
        }

        // Проверка истечения срока действия refresh токена
        long expirationMinutes = jwtUtil.getRefreshTokenExpiration() / 60000;
        if (storedToken.getCreatedAt().plusMinutes(expirationMinutes).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh токен истёк");
        }

        // Генерация и сохранение нового access токена
        String newAccessToken = jwtUtil.generateToken(username, TokenType.ACCESS);

        userTokenRepository.save(UserToken.builder()
                .token(newAccessToken)
                .user(user)
                .tokenType(TokenType.ACCESS)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build());

        return new AuthResponse(newAccessToken, refreshToken);
    }
}
