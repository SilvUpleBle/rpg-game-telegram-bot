package io.project.TestBot.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private String activeTasks = "";

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

    public String[] getAllActiveTasksId() {
        String[] allActiveTasksId = getActiveTasks().split(";");
        return allActiveTasksId;
    }

    public void deleteTask(Long taskId) {
        List<String> allActiveTasksId = new ArrayList<>(Arrays.asList(activeTasks.split(";")));
        allActiveTasksId.remove(String.valueOf(taskId));
        if (!allActiveTasksId.isEmpty()) {
            activeTasks = allActiveTasksId.get(0);
            for (int i = 1; i < allActiveTasksId.size(); i++) {
                activeTasks += ";" + allActiveTasksId.get(i);
            }
        } else {
            activeTasks = "";
        }
    }

    public void setActiveTasks(String activeTasks) {
        this.activeTasks = activeTasks;
    }

    public void addActiveTask(Long taskId) {
        if (activeTasks == null || activeTasks.equals("")) {
            activeTasks = String.valueOf(taskId);
        } else {
            activeTasks += ";" + taskId;
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