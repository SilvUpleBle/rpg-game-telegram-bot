package io.project.TestBot.model;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface Battle_table extends CrudRepository<BattleSQL, Long> {
    
    Optional<BattleSQL> findByFirstSideIds(Long[] firstSideIds);

    Optional<BattleSQL> findBySecondSideIds(Long[] secondSideIds);
}