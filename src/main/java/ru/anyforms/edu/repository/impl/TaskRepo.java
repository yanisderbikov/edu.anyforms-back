package ru.anyforms.edu.repository.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.anyforms.edu.model.task.Task;
import ru.anyforms.edu.model.task.TaskStatus;
import ru.anyforms.edu.model.task.TaskType;

import java.util.List;
import java.util.UUID;

@Repository
interface TaskRepo extends JpaRepository<Task, UUID> {

    List<Task> findByTypeAndStatusOrderByCreatedAtAsc(TaskType type, TaskStatus status, Pageable pageable);
}
