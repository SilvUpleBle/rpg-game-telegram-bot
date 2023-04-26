package io.project.TestBot.model;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface Shop_table extends CrudRepository<ShopSQL, Long> {
    ShopSQL findByShopId(Long shopId);
}