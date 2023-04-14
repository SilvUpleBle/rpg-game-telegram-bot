package io.project.TestBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "task_table")
public class TaskSQL {
    @Id
    private Long TaskId;

    private Long AcceptorId;

    private Long CreatorId;

    private String TaskName;

    private String TaskDescription;

    private String DateStart;

    private String DateEnd;

    private String TaskType;

    public Long getTaskId() {
        return TaskId;
    }

    public void setTaskId(Long taskId) {
        TaskId = taskId;
    }

    public Long getAcceptorId() {
        return AcceptorId;
    }

    public void setAcceptorId(Long acceptorId) {
        AcceptorId = acceptorId;
    }

    public Long getCreatorId() {
        return CreatorId;
    }

    public void setCreatorId(Long creatorId) {
        CreatorId = creatorId;
    }

    public String getTaskName() {
        return TaskName;
    }

    public void setTaskName(String taskName) {
        TaskName = taskName;
    }

    public String getTaskDescription() {
        return TaskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        TaskDescription = taskDescription;
    }

    public String getDateStart() {
        return DateStart;
    }

    public void setDateStart(String dateStart) {
        DateStart = dateStart;
    }

    public String getDateEnd() {
        return DateEnd;
    }

    public void setDateEnd(String dateEnd) {
        DateEnd = dateEnd;
    }

    public String getTaskType() {
        return TaskType;
    }

    public void setTaskType(String taskType) {
        TaskType = taskType;
    }

}