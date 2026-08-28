package ru.anyforms.edu.repository.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import ru.anyforms.edu.model.task.Task;
import ru.anyforms.edu.model.task.TaskStatus;
import ru.anyforms.edu.model.task.TaskType;
import ru.anyforms.edu.repository.GetterTaskByStatus;
import ru.anyforms.edu.repository.SaverTask;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
class TaskManager implements GetterTaskByStatus, SaverTask {

    private final TaskRepo taskRepo;

    @Override
    public List<Task> getByTaskTypeAndStatus(TaskType taskType, TaskStatus taskStatus, int batchSize) {
        try {
            return taskRepo.findByTypeAndStatusOrderByCreatedAtAsc(taskType, taskStatus, PageRequest.of(0, batchSize));
        } catch (Exception e) {
            log.error("getByTaskTypeAndStatus failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }

    @Override
    public Task save(Task task) {
        try {
            return taskRepo.save(task);
        } catch (Exception e) {
            log.error("save task failed", e);
            throw new RuntimeException("Database exception", e);
        }
    }
}
