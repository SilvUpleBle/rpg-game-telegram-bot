package io.project.TestBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "user_hero")
public class UserHero {

    @Id
    private Long userId;

    private String heroName;

    private String gameRole;

    private int forcePower;

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
     * public String getInventory() {
     * return inventory;
     * }
     * 
     * public void setInventory(String inventory) {
     * this.inventory = inventory;
     * }
     */
}
