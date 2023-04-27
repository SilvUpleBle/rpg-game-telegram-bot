package io.project.TestBot.model;

import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity(name = "phrase_table")
public class PhraseSQL {

    @Id
    @GeneratedValue
    private Long phraseId;

    private String text;

    private String type;

    public PhraseSQL() {

    }

    public PhraseSQL(String text, String type) {
        this.text = text;
        this.type = type;
    }

    public Long getPhraseId() {
        return phraseId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}