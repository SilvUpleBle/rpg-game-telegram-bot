package io.project.TestBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "user_table")
public class UserSQL {

    @Id
    private Long userId;

    private long chatId;

    private boolean isAdmin;

    private String firstName;

    private String lastName;

    private String userName;

    private int points;

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
}