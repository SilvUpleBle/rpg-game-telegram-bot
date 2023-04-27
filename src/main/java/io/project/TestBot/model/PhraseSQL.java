package io.project.TestBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "phrase_table")
public class PhraseSQL {

    @Id
    private Long phraseId;

    private String text;

    private String type;

    public PhraseSQL() {

    }

    public PhraseSQL(Long phraseId, String text, String type) {
        this.phraseId = phraseId;
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