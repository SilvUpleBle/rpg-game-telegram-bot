package io.project.TestBot.model;

import com.vdurmont.emoji.EmojiParser;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "item_table")
public class ItemSQL {
    @Id
    private Long itemId;

    private String itemName;

    // loot
    // weapon
    // head
    // chest
    // legs
    // foots
    // talisman
    // heal
    private String itemType;

    private int itemLevel;

    public ItemSQL() {
    }

    public ItemSQL(Long itemId, String itemName, String itemType, int itemLevel) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemType = itemType;
        this.itemLevel = itemLevel;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public int getItemLevel() {
        return itemLevel;
    }

    public void setItemLevel(int itemLevel) {
        this.itemLevel = itemLevel;
    }

    // стрелочка вверх "\u21d1"
    // стрелочка вниз "\u21d3"
    // стрелочка вверх "\u2B06"
    // стрелочка вниз "\u2B07"
    public String toString() {
        return itemName + " lv." + itemLevel;
    }

    public String toStringWithType() {
        return EmojiParser.parseToUnicode(typeToEmoji()) + " " + itemName + " lv." + itemLevel;
    }

    private String typeToEmoji() {
        switch (itemType) {
            case "weapon":
                return ":dagger_knife:";
            case "head":
                return ":tophat:";
            case "chest":
                return ":shirt:";
            case "legs":
                return ":jeans:";
            case "foots":
                return ":mans_shoe:";
            case "talisman":
                return ":ring:";
            case "heal":
                return ":pill:";
            case "loot":
                return ":moneybag:";
            default:
                return "";
        }
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

}