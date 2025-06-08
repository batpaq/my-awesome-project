package petproekt.task_management_system.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import petproekt.task_management_system.entity.Task;
import petproekt.task_management_system.entity.UserApp;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByUserApp(UserApp userApp);
}
