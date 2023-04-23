package io.project.TestBot.model;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface Hero_groups extends CrudRepository<GroupSQL, Long> {
    Optional<GroupSQL> findByIdLeader(Long leaderId);
}