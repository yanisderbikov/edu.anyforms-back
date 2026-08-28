package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.task.Task;
import ru.anyforms.edu.model.task.TaskStatus;
import ru.anyforms.edu.model.task.TaskType;

import java.util.List;

public interface GetterTaskByStatus {
    List<Task> getByTaskTypeAndStatus(TaskType taskType, TaskStatus taskStatus, int batchSize);
}
