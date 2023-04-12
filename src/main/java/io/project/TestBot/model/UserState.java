package io.project.TestBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "user_state")
public class UserState {

    @Id
    private long userId;

    private String function;

    private byte step = 0;

    private boolean waitForRequest = false;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public byte getStep() {
        return step;
    }

    public void setStep(byte step) {
        this.step = step;
    }

    public boolean getWaitForRequest() {
        return waitForRequest;
    }

    public void setWaitForRequest(boolean waitForRequest) {
        this.waitForRequest = waitForRequest;
    }

}