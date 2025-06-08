package petproekt.task_management_system.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import petproekt.task_management_system.entity.UserApp;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserApp, UUID> {

    Optional<UserApp> findByUsername(String username);
    boolean existsByUsername(String username);
}
