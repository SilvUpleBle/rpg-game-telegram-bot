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
    private String[] itemId;

    public String[] getItemId() {
        return itemId;
    }

    public void setItemId(String[] itemId) {
        this.itemId = itemId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

}