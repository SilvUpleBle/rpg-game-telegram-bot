package io.project.TestBot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity(name = "battle_table")
public class BattleSQL {

    @Id
    @GeneratedValue
    private Long battleId;

    private String firstSide = "user";

    // user, если битва на арене
    // monster, если битва с npc
    private String secondSide;

    @Column(length = 1000)
    private Long[] firstSideIds;

    @Column(length = 1000)
    private Long[] secondSideIds;

    private Integer messageId;

    private Integer logId;

    private String logMessage;

    public BattleSQL() {
    }

    public BattleSQL(String secondSide, Long[] firstSideIds, Long[] secondSideIds) {
        this.secondSide = secondSide;
        this.firstSideIds = firstSideIds;
        this.secondSideIds = secondSideIds;
    }

    public Long getBattleId() {
        return battleId;
    }

    public void setBattleId(Long battleId) {
        this.battleId = battleId;
    }

    public String getFirstSide() {
        return firstSide;
    }

    public void setFirstSide(String firstSide) {
        this.firstSide = firstSide;
    }

    public String getSecondSide() {
        return secondSide;
    }

    public void setSecondSide(String secondSide) {
        this.secondSide = secondSide;
    }

    public Long[] getFirstSideIds() {
        return firstSideIds;
    }

    public void setFirstSideIds(Long[] firstSideIds) {
        this.firstSideIds = firstSideIds;
    }

    public Long[] getSecondSideIds() {
        return secondSideIds;
    }

    public void setSecondSideIds(Long[] secondSideIds) {
        this.secondSideIds = secondSideIds;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

}