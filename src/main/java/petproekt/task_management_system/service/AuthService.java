package petproekt.task_management_system.service;

import petproekt.task_management_system.dto.AuthRequest;
import petproekt.task_management_system.dto.AuthResponse;
import petproekt.task_management_system.dto.RegisterRequest;

public interface AuthService {
    String register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
    void logout(String token);

    AuthResponse refreshToken(String refreshToken);
}
