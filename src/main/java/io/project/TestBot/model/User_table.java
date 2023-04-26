package io.project.TestBot.model;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface User_table extends CrudRepository<UserSQL, Long> {

    List<UserSQL> findAllByChatId(Long chatId);

    UserSQL findByUserId(long userId);

}