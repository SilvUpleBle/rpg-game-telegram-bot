package io.project.TestBot.model;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface Task_table extends CrudRepository<TaskSQL, Long> {
    TaskSQL findByTaskName(String taskName);

    TaskSQL findByTaskId(Long taskId);

    boolean existsByTaskName(String taskName);

    List<TaskSQL> findAllByCreatorId(Long creatorId);

    List<TaskSQL> findAllByRecipientId(String recipientId);
}
