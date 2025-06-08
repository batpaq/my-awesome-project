package petproekt.task_management_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import petproekt.task_management_system.config.JwtProperties;
import petproekt.task_management_system.dto.AuthResponse;
import petproekt.task_management_system.dto.ErrorResponse;
import petproekt.task_management_system.dto.UserInfoDto;
import petproekt.task_management_system.enm.TokenType;
import petproekt.task_management_system.entity.UserApp;
import petproekt.task_management_system.exception.UserNotFoundException;
import petproekt.task_management_system.mapper.UserInfoMapper;
import petproekt.task_management_system.security.JwtUtil;
import petproekt.task_management_system.service.CustomUserDetailsService;
import petproekt.task_management_system.service.UserTokenService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final JwtUtil jwtUtil;
    private final UserTokenService userTokenService;
    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService userService;
    private final UserInfoMapper userInfoMapper;

    private ResponseEntity<ErrorResponse> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(message));
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает информацию о пользователе: id, username и замаскированный пароль"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfoDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь с таким ID не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный запрос (например, пустой или некорректный ID)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/id")
    public ResponseEntity<?> getCurrentUserInfo(Authentication authentication) {
        try {
            String username = authentication.getName(); // или получить userId, если вы его кладёте в токен
            UserApp user = userService.findByUsername(username); // или findById если в токене есть ID
            UserInfoDto response = userInfoMapper.toUserInfoDto(user);
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }


    @Operation(
            summary = "Обновить access-токен с помощью refresh-токена",
            description = "Принимает refresh-токен из заголовка Authorization и возвращает новый access-токен, если refresh-токен валиден"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Новый access-токен успешно сгенерирован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Ошибка авторизации: отсутствует, недействителен или просрочен refresh-токен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = extractTokenFromHeader(request);

        if (refreshToken == null || refreshToken.isBlank()) {
            return unauthorized("Отсутствует или некорректный refresh-токен");
        }

        if (!jwtUtil.validateToken(refreshToken, TokenType.REFRESH)) {
            return unauthorized("Refresh-токен недействителен или истек");
        }

        long expirationMinutes = jwtProperties.getRefreshTokenExpiration() / 60000;
        if (!userTokenService.isTokenValid(refreshToken, expirationMinutes)) {
            return unauthorized("Refresh-токен не найден в базе или просрочен");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken, TokenType.REFRESH);
        if (username == null || username.isBlank()) {
            return unauthorized("Не удалось извлечь имя пользователя из refresh-токена");
        }

        String newAccessToken = jwtUtil.generateToken(username, TokenType.ACCESS);

        // Проверка: если refresh-токен скоро истечет — обновляем
        long expirationSeconds = jwtUtil.getRemainingExpirationSeconds(refreshToken, TokenType.REFRESH);
        boolean needRefreshRotation = expirationSeconds < 60; // менее 1 минуты

        String actualRefreshToken = refreshToken;
        if (needRefreshRotation) {
            actualRefreshToken = jwtUtil.generateToken(username, TokenType.REFRESH);
            userTokenService.replaceToken(refreshToken, actualRefreshToken); // сохранить новый в БД
        }

        return ResponseEntity.ok(new AuthResponse(newAccessToken, actualRefreshToken));
    }

}
