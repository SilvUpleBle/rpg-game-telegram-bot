package io.project.TestBot.model;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface Item_table extends CrudRepository<ItemSQL, Long> {
    List<ItemSQL> findByAllItemType(String itemType);
}