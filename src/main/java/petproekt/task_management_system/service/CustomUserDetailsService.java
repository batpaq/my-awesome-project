package petproekt.task_management_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import petproekt.task_management_system.dao.UserRepository;
import petproekt.task_management_system.entity.UserApp;
import petproekt.task_management_system.exception.UserNotFoundException;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Загружает пользователя по username для Spring Security.
     * @param username имя пользователя (логин)
     * @return объект UserDetails с данными для аутентификации
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserApp userApp = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь '" + username + "' не найден"));
        // Здесь можно вернуть роли/привилегии, сейчас пустой список авторизаций
        return new org.springframework.security.core.userdetails.User(
                userApp.getUsername(),
                userApp.getPassword(),
                Collections.emptyList()
        );
    }


    /**
     * Находит пользователя по его username (логину).
     *
     * @param username имя пользователя
     * @return найденный пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    public UserApp findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с username " + username + " не найден"));
    }

    /**
     * Метод для поиска пользователя по ID.
     * @param id UUID пользователя
     * @return найденный UserApp
     * @throws UserNotFoundException если пользователь не найден
     */
    public UserApp findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не найден"));
    }
}
