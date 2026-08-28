package ru.anyforms.edu.repository;

import ru.anyforms.edu.model.task.Task;

public interface SaverTask {
    Task save(Task task);
}
