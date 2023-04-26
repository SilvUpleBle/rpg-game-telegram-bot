package io.project.TestBot.model;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface User_hero extends CrudRepository<UserHero, Long> {
    Optional<UserHero> findByUserId(long userId);

}