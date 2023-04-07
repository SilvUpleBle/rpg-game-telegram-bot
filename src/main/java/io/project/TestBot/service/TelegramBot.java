package io.project.TestBot.service;

import io.project.TestBot.config.BotConfig;
import io.project.TestBot.model.UserSQL;
import io.project.TestBot.model.User_hero;
import io.project.TestBot.model.UserHero;
import io.project.TestBot.model.User_table;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private User_table user_table;
    @Autowired
    private User_hero user_hero;

    final BotConfig config;

    static final String HELP_TEXT = "Приветик!";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "switch bot on"));
        listOfCommands.add(new BotCommand("/register", "register user"));
        listOfCommands.add(new BotCommand("/command", "use command 1"));
        listOfCommands.add(new BotCommand("/help", "how to use this bot"));
        listOfCommands.add(new BotCommand("/createHero", "create your hero"));
        try {
            this.execute(new SendMessage(String.valueOf(778258104), "Я проснувся!"));
            this.execute(new SendMessage(String.valueOf(808370703), "Я проснувся!"));
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot`s command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandRecieved(chatId, update.getMessage().getChat().getFirstName() + " "
                            + update.getMessage().getChat().getLastName());
                    break;

                case "/register":
                    registerUser(update.getMessage());
                    break;

                case "/createHero":
                    createHero(update.getMessage());
                    break;

                case "/command":
                    break;

                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;

                default:
                    sendMessageWithKeyboard(chatId, "Sorry, command wasn`t recogised! :(");
                    break;
            }
        }
    }

    //
    // РЕГИСТРАЦИЯ
    //

    private void registerUser(Message message) {

        if (user_table.findById(message.getChatId()).isEmpty()) {
            User userT = message.getFrom();

            UserSQL user = new UserSQL();
            user.setUserId(userT.getId());
            user.setChatId(message.getChatId());
            user.setUserName(userT.getUserName());
            user.setFirstName(userT.getFirstName());
            user.setLastName(userT.getLastName());

            sendMessage(userT.getId(), "Введите пароль:");
            user_table.save(user);
        } else {
            sendMessage(message.getChatId(), "Вы уже зарегистрированы!");
        }
    }

    private void registerHero(long userId, String name) {
        if (user_hero.findById(userId).isEmpty()) {
            UserHero user = new UserHero();

            user.setUserId(userId);
            user.setHeroName(name);

            user_hero.save(user);
            sendMessage(userId, "Персонаж создан");
        } else {
            sendMessage(userId, "У вас уже есть персонаж!");
        }
    }

    private void createHero(Message message) {
        long chatId = message.getChatId();
        sendMessage(chatId,
                message.getChat().getFirstName() + " давай начнем создание твоего персонажа, какое его имя?");
        log.info("Start creating hero" + message.getChat().getFirstName());

        // получаем сообщение
        String textToSend = "Слава";
        sendMessage(chatId, "Имя персонажа " + textToSend + " вы уверены?");
        // получаем сообщение
        // присваиваем сообщение textToSend = getMessage()
        textToSend = "Нет";
        switch (textToSend) {
            case "Да":
                sendMessage(chatId, "Персонаж создан");
                break;
            case "Нет":
                sendMessage(chatId, "Введите имя персонажа");
                // получение сообщения
                registerHero(message.getChat().getId(), "Слава");
                break;
            default:
                sendMessage(chatId, "Неизвестная команда");
                break;
        }
    }

    // private void
    private void startCommandRecieved(long chatId, String textToSend) {
        String answer = "Hi, " + textToSend + ", nice to meet you!";
        log.info("Replied to user " + textToSend);

        sendMessage(chatId, answer);
    }

    /*
     * принимает массив строк где в кейборде
     * {{инвентарь}(первая строка),{голова, торс,
     * поножи}(вторая строка)и тд}
     */
    private ReplyKeyboardMarkup keyboard(String[][] rowArray) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        for (int i = 0; i < rowArray.length; i++) {
            for (int j = 0; j < rowArray[j].length; j++) {
                row.add(rowArray[i][j]);
            }
            row = new KeyboardRow();
        }
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private void sendMessageWithKeyboard(long chatId, String textToSend) {// возможно массив строк реализовать как
                                                                          // параметр
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        String[][] rowArray = { { "Инвентарь" }, { "Квас", "Торс", "Руки", "Ноги" },
                { "Левая рука", "Правая рука" } };
        ReplyKeyboardMarkup Keyboard = keyboard(rowArray);
        message.setReplyMarkup(Keyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void sendMessageWithPicture(long chatId, String textToSend, String imageUrlToSend) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(String.valueOf(chatId));
        photo.setCaption(textToSend);
        photo.setPhoto(new InputFile(imageUrlToSend));

        try {
            execute(photo);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    // ОТПРАВИТЬ КАРТИНКИ + СООБЩЕНИЕ
    private void sendMessageWithPictures(long chatId, String textToSend, ArrayList<String> listOfImageUrlToSend) {
        ArrayList<InputMedia> inputMedia = new ArrayList<>();

        for (String imageUrl : listOfImageUrlToSend) {
            inputMedia.add(new InputMediaPhoto(imageUrl));
        }
        inputMedia.get(0).setCaption(textToSend);

        SendMediaGroup mediaGroup = new SendMediaGroup();
        mediaGroup.setChatId(String.valueOf(chatId));
        mediaGroup.setMedias(inputMedia);

        try {
            execute(mediaGroup);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void sendImage(long chatId, String imageUrlToSend) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(String.valueOf(chatId));
        photo.setPhoto(new InputFile(imageUrlToSend));

        try {
            execute(photo);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}