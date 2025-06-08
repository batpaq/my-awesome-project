package petproekt.task_management_system.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import petproekt.task_management_system.entity.Task;
import petproekt.task_management_system.entity.UserApp;
import petproekt.task_management_system.dao.TaskRepository;
import petproekt.task_management_system.dao.UserRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    UserRepository userRepository;

    @Operation(
            summary = "Получить список задач текущего пользователя",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "Возвращает список задач, связанных с авторизованным пользователем"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список задач успешно возвращён"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @GetMapping
    public List<Task> getTask(Authentication authentication) {
        UserApp userApp = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return taskRepository.findByUserApp(userApp);
    }
    @Operation(
            summary = "Создать новую задачу для текущего пользователя",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "Добавляет новую задачу, привязанную к текущему пользователю"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача успешно создана"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content)
    })
    @PostMapping
    public Task create(@RequestBody Task task, Authentication authentication) {
        UserApp userApp = userRepository.findByUsername(authentication.getName()).orElseThrow();
        task.setUserApp(userApp);
        return taskRepository.save(task);
    }
    @Operation(
            summary = "Обновить задачу по ID",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "Обновляет данные задачи, если она принадлежит текущему пользователю"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача успешно обновлена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён — задача не принадлежит пользователю", content = @Content),
            @ApiResponse(responseCode = "404", description = "Задача не найдена", content = @Content)
    })
    @PostMapping("/{id}")
public Task update(@PathVariable UUID id,@RequestBody Task taskUpdate,Authentication authentication){
        Task task = taskRepository.findById(id).orElseThrow();
        if(!task.getUserApp().getUsername().equals(authentication.getName()))throw new SecurityException();
        task.setTitle(taskUpdate.getDescription());
        task.setDone(taskUpdate.isDone());
        return taskRepository.save(task);
    }
    @Operation(
            summary = "Удалить задачу по ID",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "Удаляет задачу, если она принадлежит текущему пользователю"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Задача успешно удалена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён — задача не принадлежит пользователю", content = @Content),
            @ApiResponse(responseCode = "404", description = "Задача не найдена", content = @Content)
    })
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id,Authentication authentication){
        Task task = taskRepository.findById(id).orElseThrow();
        if(!task.getUserApp().getUsername().equals(authentication.getName()))throw new SecurityException();
        taskRepository.delete(task);
    }

}
