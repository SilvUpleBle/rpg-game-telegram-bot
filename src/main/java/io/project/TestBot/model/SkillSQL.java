package io.project.TestBot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "skill_table")
public class SkillSQL {
    @Id
    private Long skillId;

    private String skillName;

    // self
    // teammate
    // teammates
    // enemy
    // enemys
    private String skillTarget;

    // скиллы пока только отнимают или повышают здоровье

    // health + -
    private String skillEffect;

    private String skillDescription;

    // фразы, которые применяются при использовании скилла
    // во фразе должны быть два %s
    // вместо первого будет подставлено имя героя
    // вместо второго - название скилла
    @Column(length = 1000)
    private String[] skillPhrases;

    public SkillSQL() {
    }

    public SkillSQL(Long skillId, String skillName, String skillDescription, String skillTarget, String skillEffect,
            String[] skillPhrases) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.skillDescription = skillDescription;
        this.skillTarget = skillTarget;
        this.skillEffect = skillEffect;
        this.skillPhrases = skillPhrases;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getSkillTarget() {
        return skillTarget;
    }

    public void setSkillTarget(String skillTarget) {
        this.skillTarget = skillTarget;
    }

    public String getSkillEffect() {
        return skillEffect;
    }

    public void setSkillEffect(String skillEffect) {
        this.skillEffect = skillEffect;
    }

    public String getSkillDescription() {
        return skillDescription;
    }

    public void setSkillDescription(String skillDescription) {
        this.skillDescription = skillDescription;
    }

    public String[] getSkillPhrases() {
        return skillPhrases;
    }

    public void setSkillPhrases(String[] skillPhrases) {
        this.skillPhrases = skillPhrases;
    }

}