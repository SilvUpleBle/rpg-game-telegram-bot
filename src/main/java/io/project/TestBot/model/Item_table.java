package io.project.TestBot.model;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface Item_table extends CrudRepository<ItemSQL, Long> {
    // Iterable<ItemSQL> findByAllItemType(String itemType);
    List<ItemSQL> findByItemType(String itemType);

    ItemSQL findByItemId(Long itemId);
}