package io.project.TestBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "user_hero")
public class UserHero {

    /*
     * 1 id
     * 2 name
     * 4 game role ? demiurg or adventurer
     * 6 fp
     * 7 head
     * 8 body
     * 9 arms
     * 10 legs
     * 11 left hand
     * 12 right hand
     * 13 inventory(string of items i
     */
    @Id
    private long userId;

    private String heroName;

    private String gameRole;

    private int forcePower;

    // private String head;

    // private String body;

    // private String arms;

    // private String legs;

    // private String rightHand;

    // private String leftHand;

    // private String inventory;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getHeroName() {
        return heroName;
    }

    public void setHeroName(String heroName) {
        this.heroName = heroName;
    }

    public String getGameRole() {
        return gameRole;
    }

    public void setGameRole(String gameRole) {
        this.gameRole = gameRole;
    }

    public long getForcePower() {
        return forcePower;
    }

    public void setForcePower(int forcePower) {
        this.forcePower = forcePower;
    }

    /*
     * public String getHead() {
     * return head;
     * }
     * 
     * public void setHead(String head) {
     * this.head = head;
     * }
     * 
     * public String getBody() {
     * return body;
     * }
     * 
     * public void setBody(String body) {
     * this.body = body;
     * }
     * 
     * public String getArms() {
     * return arms;
     * }
     * 
     * public void setArms(String arms) {
     * this.arms = arms;
     * }
     * 
     * public String getLegs() {
     * return legs;
     * }
     * 
     * public void setLegs(String legs) {
     * this.legs = legs;
     * }
     * 
     * public String getRightHand() {
     * return rightHand;
     * }
     * 
     * public void setRightHand(String rightHand) {
     * this.rightHand = rightHand;
     * }
     * 
     * public String getLeftHand() {
     * return leftHand;
     * }
     * 
     * public void setLeftHand(String leftHand) {
     * this.leftHand = leftHand;
     * }
     * 
     * public String getInventory() {
     * return inventory;
     * }
     * 
     * public void setInventory(String inventory) {
     * this.inventory = inventory;
     * }
     */
}
