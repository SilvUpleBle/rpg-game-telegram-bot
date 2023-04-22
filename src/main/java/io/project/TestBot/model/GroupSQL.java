package io.project.TestBot.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "hero_groups")
public class GroupSQL {
    @Id
    private Long idGroup;

    private String groupName;

    private Long idLeader;

    private String idUsers = "";

    private String destination;

    public Long getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(Long idGroup) {
        this.idGroup = idGroup;
    }

    public Long getIdLeader() {
        return idLeader;
    }

    public void setIdLeader(Long idLeader) {
        this.idLeader = idLeader;
    }

    public String getIdUsers() {
        return idUsers;
    }

    public void setIdUsers(String idUsers) {
        this.idUsers = idUsers;
    }

    public void addUser(Long userId) {
        if (idUsers.equals("")) {
            idUsers = String.valueOf(userId);
        } else {
            idUsers += ";" + userId;
        }
    }

    public void excludeUser(Long userId) {
        List<String> users = new ArrayList<>(Arrays.asList(idUsers.split(";")));
        users.remove(String.valueOf(userId));

        idUsers = users.get(0);
        for (int i = 1; i < users.size(); i++) {
            idUsers += ";" + users.get(i);
        }
    }

    /*
     * public String showUsers() {
     * List<String> users = Arrays.asList(idUsers.split(";"));
     * String textToSend = users.get(0) + EmojiParser.parseToUnicode(":crown:");
     * for (int i = 1; i < users.size(); i++) {
     * textToSend += ";" + users.get(i);
     * }
     * return textToSend;
     * }
     */

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

}