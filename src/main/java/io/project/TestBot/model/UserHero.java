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

    private String health = "10/10";

    private int money = 0;

    private int points = 0;

    private String inventory = "";

    private String equipment = "0;0;0;0;0;0";

    private Long[] equipedSkills = new Long[4];

    private List<Long> skills;

    private Long idGroup;

    private int level = 1;

    private int experience = 0;

    private int armor = 0;

    private int minAttack = 0;

    private int maxAttack = 5;

    private int criticalChance = 0;

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

    public String getInventory() {
        return inventory;
    }

    public void setInventory(String inventory) {
        this.inventory = inventory;
    }

    public void addToInventory(Long itemId) {
        List<String> list;
        if (!inventory.split(";")[0].equals("")) {
            list = new ArrayList<>(Arrays.asList(inventory.split(";")));
        } else {
            list = new ArrayList<>();
        }
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

    public void changeEquipment(int item, Long equipmentId, Item_table item_table) {
        String[] equip = equipment.split(";");
        if (!equip[item].equals("0")) {
            this.addToInventory(Long.valueOf(equip[item]));
        }
        equip[item] = String.valueOf(equipmentId);
        equipment = equip[0];
        for (int i = 1; i < equip.length; i++) {
            equipment += ";" + equip[i];
        }
        recalculateAttackAndArmor(item_table);
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void addExperience(int experience) {
        this.experience += experience;
        checkLevel();
    }

    public void checkLevel() {
        if (experience >= getExperienceForNewLevel()) {
            level++;
            health = (level * 10) + "/" + (level * 10);
            experience = experience - getExperienceForNewLevel();
        }
    }

    public int getExperienceForNewLevel() {
        return (int) Math.pow(level / 0.05, 1.5);
    }

    public int getArmor() {
        return armor;
    }

    public void setArmor(int armor) {
        this.armor = armor;
    }

    public int getMinAttack() {
        return minAttack;
    }

    public int getMaxAttack() {
        return maxAttack;
    }

    public int getCriticalChance() {
        return criticalChance;
    }

    public void recalculateAttackAndArmor(Item_table item_table) {
        armor = 0;
        health = (level * 10) + "/" + (level * 10);
        for (int i = 0; i < 4; i++) {
            armor += item_table.findById(Long.valueOf(equipment.split(";")[i])).get().getValue()
                    * item_table.findById(Long.valueOf(equipment.split(";")[5])).get().getItemLevel();
        }
        criticalChance = item_table.findById(Long.valueOf(equipment.split(";")[4])).get().getItemLevel();
        if (!equipment.split(";")[5].equals("0")) {
            minAttack = item_table.findById(Long.valueOf(equipment.split(";")[5])).get().getValue();
            maxAttack = minAttack
                    * (item_table.findById(Long.valueOf(equipment.split(";")[5])).get().getItemLevel() + 1);
        } else {
            minAttack = 0;
            maxAttack = 5;
        }
    }
}
