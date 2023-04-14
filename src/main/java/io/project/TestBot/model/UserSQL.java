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

    private String password;

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

    public boolean getIsAdmin() {
        return isAdmin;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toString() {
        return "User{" +
                " userId=" + userId +
                ", firstName=" + firstName +
                ", lastName=" + lastName +
                ", userName=" + userName +
                ", password=" + password + " " +
                "}";
    }
}