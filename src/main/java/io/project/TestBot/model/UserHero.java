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

    private int forcePower = 0;

    private String health = "10/10";

    private int money = 0;

    private int points = 0;

    private String inventory = "";

    private String equipment = "0;0;0;0;0;0";

    private Long[] equipedSkills = new Long[4];

    private List<Long> skills;

    private Long idGroup;

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
        List<String> list = new ArrayList<>(Arrays.asList(inventory.split(";")));
        list.add(String.valueOf(itemId));
        inventory = "";
        inventory += list.get(0);
        for (int i = 1; i < list.size(); i++) {
            inventory += ";" + list.get(i);
        }
    }

    public void takeFromInventory(Long itemId) {
        List<String> list = new ArrayList<>(Arrays.asList(inventory.split(";")));
        list.remove(String.valueOf(itemId));
        inventory = "";
        if (!list.isEmpty()) {
            inventory += list.get(0);
            for (int i = 1; i < list.size(); i++) {
                inventory += ";" + list.get(i);
            }
        }
    }

    public String[] getEquipment() {
        return equipment.split(";");
    }

    public void changeEquipment(int item, Long equipmentId) {
        String[] equip = equipment.split(";");
        if (!equip[item].equals("0")) {
            this.addToInventory(Long.valueOf(equip[item]));
        }
        equip[item] = String.valueOf(equipmentId);
        equipment = equip[0];
        for (int i = 1; i < equip.length; i++) {
            equipment += ";" + equip[i];
        }
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public Long getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(Long idGroup) {
        this.idGroup = idGroup;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public void setCurrentHealth(int hp) {
        String[] pair = health.split("/");
        if (hp > Integer.valueOf(pair[1])) {
            pair[0] = pair[1];
        } else {
            pair[0] = String.valueOf(hp);
        }
        health = pair[0] + "/" + pair[1];
    }

    public int getCurrentHealth() {
        return Integer.valueOf(health.split("/")[0]);
    }

    public void setMaxHealth(int hp) {
        String[] pair = health.split("/");
        pair[1] = String.valueOf(hp);
        if (hp > Integer.valueOf(pair[0])) {
            pair[0] = String.valueOf(hp);
        }
        health = pair[0] + "/" + pair[1];
    }

    public int getMaxHealth() {
        return Integer.valueOf(health.split("/")[1]);
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public void subPoints(int points) {
        this.points -= points;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public Long[] getEquipedSkills() {
        checkSkills();
        return equipedSkills;
    }

    private void checkSkills() {
        if (equipedSkills == null) {
            equipedSkills = new Long[4];
        }
        if (skills == null) {
            skills = new ArrayList<>();
        }
    }

    public void equipSkill(Long skillId, int position) {
        checkSkills();
        equipedSkills[position] = skillId;
        skills.remove(skillId);
    }

    public void unequipSkill(Long skillId, int position) {
        checkSkills();
        equipedSkills[position] = null;
        skills.add(skillId);
    }

    public void setEquipedSkills(Long[] equipedSkills) {
        this.equipedSkills = equipedSkills;
    }

    public List<Long> getSkills() {
        checkSkills();
        return skills;
    }

    public void addSkill(Long skillId) {
        checkSkills();
        skills.add(skillId);
    }

    public void setSkills(List<Long> skills) {
        this.skills = skills;
    }

}
