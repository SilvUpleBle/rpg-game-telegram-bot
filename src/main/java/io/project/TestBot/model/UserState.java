package io.project.TestBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "user_state")
public class UserState {

    @Id
    private Long userId;

    private String process;

    private int step;

    private boolean waitForRequest;

    private String lastUserMessage;

    private int idLastBotMessage;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public boolean getWaitForRequest() {
        return waitForRequest;
    }

    public void setWaitForRequest(boolean waitForRequest) {
        this.waitForRequest = waitForRequest;
    }

    public boolean isWaitForRequest() {
        return waitForRequest;
    }

    public String getLastUserMessage() {
        return lastUserMessage;
    }

    public void setLastUserMessage(String lastUserMessage) {
        this.lastUserMessage = lastUserMessage;
    }

    public int getIdLastBotMessage() {
        return idLastBotMessage;
    }

    public void setIdLastBotMessage(int idLastBotMessage) {
        this.idLastBotMessage = idLastBotMessage;
    }

}