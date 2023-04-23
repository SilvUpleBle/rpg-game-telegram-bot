package io.project.TestBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "shop_table")
public class ShopSQL {
    @Id
    // weapon
    // head
    // chest
    // legs
    // foots
    // talisman
    // heal
    private Long shopId;
    private String[] weapon;
    private String[] head;
    private String[] chest;
    private String[] legs;
    private String[] foots;
    private String[] talisman;
    private String[] heal;

    public String[] getHeal() {
        return heal;
    }

    public void setHeal(String[] heal) {
        this.heal = heal;
    }

    public String[] getTalisman() {
        return talisman;
    }

    public void setTalisman(String[] talisman) {
        this.talisman = talisman;
    }

    public String[] getFoots() {
        return foots;
    }

    public void setFoots(String[] foots) {
        this.foots = foots;
    }

    public String[] getLegs() {
        return legs;
    }

    public void setLegs(String[] legs) {
        this.legs = legs;
    }

    public String[] getChest() {
        return chest;
    }

    public void setChest(String[] chest) {
        this.chest = chest;
    }

    public String[] getHead() {
        return head;
    }

    public void setHead(String[] head) {
        this.head = head;
    }

    public String[] getWeapon() {
        return weapon;
    }

    public void setWeapon(String[] weapon) {
        this.weapon = weapon;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

}