package io.project.TestBot.model;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface Phrase_table extends CrudRepository<PhraseSQL, Long> {

    List<PhraseSQL> findByType(String type);

}