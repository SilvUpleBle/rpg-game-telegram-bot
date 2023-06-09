package io.project.TestBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "task_table")
public class TaskSQL {
    @Id
    private Long taskId;

    private Long creatorId;

    private String recipientId;

    private String taskName;

    private String taskDescription;

    private int points;

    private String dateStart;

    private String dateEnd;

    private String taskType;

    private Integer messageId;

    private Integer capacity;
    private Boolean waitForAccept;

    public Boolean getWaitForAccept() {
        return waitForAccept;
    }

    public void setWaitForAccept(Boolean waitForAccept) {
        this.waitForAccept = waitForAccept;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String[] getAllRecipientId() {
        return getRecipientId().split(";");
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public void addRecipientId(String recipientId) {
        if (this.recipientId == null) {
            this.recipientId = recipientId;
        } else {
            this.recipientId += ";%s".formatted(recipientId);
        }
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getDateStart() {
        return dateStart;
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

}