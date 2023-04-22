package io.project.TestBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "user_table")
public class UserSQL implements Comparable<UserSQL> {

    @Id
    private Long userId;

    private long chatId;

    private boolean isAdmin;

    private String firstName;

    private String lastName;

    private String userName;

    private int points;

    private String activeTasks;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String toString() {
        return "User{" +
                " userId=" + userId +
                ", firstName=" + firstName +
                ", lastName=" + lastName +
                ", userName=" + userName +
                "}";
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void addPoints(int points) {
        if (this.points == 0) {
            this.points = points;
        } else {
            this.points += points;
        }
    }

    public String getActiveTasks() {
        return activeTasks;
    }

    public void setActiveTasks(String activeTasks) {
        this.activeTasks = activeTasks;
    }

    public void addActiveTasks(String activeTasks) {
        if (this.activeTasks == null) {
            this.activeTasks = activeTasks;
        } else {
            this.activeTasks += ";%s".formatted(activeTasks);
        }
    }

    @Override
    public int compareTo(UserSQL user) {
        if (this.points == user.getPoints()) {
            return 0;
        }

        if (this.points > user.getPoints()) {
            return -1;
        }

        return 1;
    }

}