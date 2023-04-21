package io.project.TestBot.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "user_hero")
public class UserHero {

    @Id
    private Long userId;

    private String heroName;

    private String gameRole;

    private int forcePower;

    private int currentHealth;

    private int maxHealth;

    private int money;

    private String inventory = "";

    private String equipment = ";;;;;";

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

    public String getInventory() {
        return inventory;
    }

    public void setInventory(String inventory) {
        this.inventory = inventory;
    }

    public void addToInventory(Long itemId) {
        if (inventory.equals("")) {
            inventory = String.valueOf(itemId);
        } else {
            inventory += ";" + itemId;
        }
    }

    public void takeFromInventory(Long itemId) {
        List<String> list = new ArrayList<>(Arrays.asList(inventory.split(";")));
        inventory = "";
        if (list.size() > 1) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(String.valueOf(itemId))) {
                    list.remove(i);
                    break;
                }
            }

            inventory += list.get(0);
            for (int i = 1; i < list.size(); i++) {
                inventory += ";" + list.get(i);
            }
        }
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

}
