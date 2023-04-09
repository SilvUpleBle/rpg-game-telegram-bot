package io.project.TestBot.service;

import io.project.TestBot.config.BotConfig;
import io.project.TestBot.model.UserSQL;
import io.project.TestBot.model.User_hero;
import io.project.TestBot.model.UserHero;
import io.project.TestBot.model.User_table;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
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

    private boolean waitForRequest = false;

    private String currentProcess;

    private int currentStep;

    private String previousUserMessage = "";

    static final String HELP_TEXT = "Приветик!";

    int lastMessageId;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "switch bot on"));
        listOfCommands.add(new BotCommand("/register", "register user"));
        listOfCommands.add(new BotCommand("/timer", "timer for 15 seconds"));
        listOfCommands.add(new BotCommand("/command", "use command 1"));
        listOfCommands.add(new BotCommand("/help", "how to use this bot"));
        listOfCommands.add(new BotCommand("/createHero", "create your hero"));
        try {
            execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
            execute(new SendMessage(String.valueOf(778258104), "Я проснувся!"));
            // this.execute(new SendMessage(String.valueOf(-939824682), "Я проснувся!"));
            // this.execute(new SendMessage(String.valueOf(808370703), "Я проснувся!"));
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
            if (waitForRequest) {
                waitForRequest = false;

                switch (currentProcess) {
                    case "/createHero":
                        createHero(update.getMessage(), (byte) currentStep);
                        break;
                    default:
                        break;
                }
            } else {
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
                        createHero(update.getMessage(), (byte) 1);
                        currentProcess = "/createHero";
                        break;
                    case "/delete_user":
                        deleteUser(update.getMessage());
                        break;

                    case "/command":
                        break;

                    case "/help":
                        sendMessage(chatId, HELP_TEXT);

                        break;

                    default:

                        sendMessageKbWithText(chatId, "Извините, команда не опознана :(",
                                new String[][] { { "Инвентарь" }, { "Голова", "Торс",
                                        "Руки", "Ноги" },
                                        { "Левая рука", "Правая рука" } });

                        break;
                }
            }
        }
    }

    //
    // НАЧАЛО БЛОКА РЕГИСТРАЦИИ
    //

    private void registerUser(Message message) {

        if (user_table.findById(message.getFrom().getId()).isEmpty()) {
            User userT = message.getFrom();

            UserSQL user = new UserSQL();
            user.setUserId(userT.getId());
            user.setChatId(message.getChatId());
            user.setUserName(userT.getUserName());
            user.setFirstName(userT.getFirstName());
            user.setLastName(userT.getLastName());

            boolean isAdmin = false;
            if (message.getChat().isGroupChat()) {
                List<ChatMember> chatMembers;
                try {
                    chatMembers = execute(new GetChatAdministrators(message.getChatId().toString()));
                    for (var chatMember : chatMembers) {
                        if (chatMember.getUser().getId() == userT.getId()) {
                            isAdmin = true;
                            break;
                        }
                    }
                } catch (TelegramApiException e) {
                    log.error("Error occurred: " + e.getMessage());
                }
            }
            user.setIsAdmin(isAdmin);

            sendMessage(userT.getId(), "Введите пароль:");
            user_table.save(user);
        } else {
            sendMessage(message.getChatId(), "Вы уже зарегистрированы!");
        }
    }

    //
    // КОНЕЦ БЛОКА РЕГИСТРАЦИИ
    //

    private void deleteUser(Message message) {
        if (user_table.findById(message.getFrom().getId()).isEmpty()) {
            sendMessage(message.getChatId(), "Вы еще не зарегестрированы");
        } else {
            user_table.deleteById(message.getFrom().getId());
        }
    }

    //
    // НАЧАЛО БЛОКА СОЗДАНИЯ ПЕРСОНАЖА
    //

    private void createHero(Message message, byte step) {
        if (message.getChat().isUserChat()) {
            if (user_hero.findById(message.getFrom().getId()).isEmpty()) {
                switch (step) {
                    case 1:
                        sendMessage(message.getFrom().getId(),
                                message.getChat().getFirstName() + ", давай начнем создание твоего героя!");
                        log.info("Start creating hero " + message.getChat().getFirstName());
                        createHero(message, (byte) 2);
                        break;
                    case 2:
                        sendMessage(message.getFrom().getId(), "Каким будет его имя?");
                        waitForRequest = true;
                        currentStep = 3;
                        break;
                    case 3:
                        previousUserMessage = message.getText();
                        sendMessageKbWithText(message.getFrom().getId(),
                                "Имя персонажа <b><i>%s</i></b>. Вы уверены?".formatted(previousUserMessage),
                                new String[][] { { "Да", "Нет" } });

                        waitForRequest = true;
                        currentStep = 4;

                        break;
                    case 4:

                        createHero(message, previousUserMessage);

                        break;
                    default:
                        break;
                }
            } else {
                sendMessage(message.getFrom().getId(),
                        "У вас уже есть персонаж <b><i>%s</i></b>!"
                                .formatted(user_hero.findById(message.getFrom().getId()).get().getHeroName()));
            }
        } else {
            sendMessage(message.getChatId(), "Используйте эту команду в личных сообщениях с ботом!");
        }
    }

    private void createHero(Message message, String heroName) {
        switch (message.getText()) {
            case "Да", "да":
                registerHero(message.getFrom().getId(), heroName);
                break;
            case "Нет", "нет":
                createHero(message, (byte) 2);
                break;
            default:
                sendMessage(message.getFrom().getId(), "Неизвестная команда");
                createHero(message, (byte) 2);

                break;
        }
    }

    private void registerHero(long userId, String name) {
        UserHero user = new UserHero();

        user.setUserId(userId);
        user.setHeroName(name);
        user.setForcePower(0);
        user.setGameRole("adventurer");

        user_hero.save(user);
        sendMessage(userId, "Персонаж <b><i>%s</i></b> создан!".formatted(name));
    }

    //
    // КОНЕЦ БЛОКА СОЗДАНИЯ ПЕРСОНАЖА
    //

    //
    // ТАЙМЕР
    //

    private void makeTimer(long chatId, int seconds) {
        int secondsLeft = 0;
        String newMessage = "[  " + "<b>.</b> ".repeat(seconds) + " ]";
        sendMessage(chatId, newMessage);

        for (; secondsLeft <= seconds; secondsLeft++) {
            try {
                TimeUnit.MILLISECONDS.sleep(750);
            } catch (InterruptedException e) {
                log.error("Error occurred: " + e.getMessage());
            }

            newMessage = "[  " + "<b>I</b> ".repeat(secondsLeft) + "<b>.</b> ".repeat(seconds - secondsLeft) + " ]";
            editMessage(chatId, newMessage);
        }
        sendMessage(chatId, "Время вышло!");
    }

    //
    // НАЧАЛО БЛОКА СЛУЖБНЫХ КОМАНД
    //
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

    private void sendMessageKbWithText(long chatId, String str, String[][] arrStr) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.enableHtml(true);
        message.setText(str);// без этого не работает

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        for (int i = 0; i < arrStr.length; i++) {
            for (int j = 0; j < arrStr[i].length; j++) {
                row.add(arrStr[i][j]);
            }
            keyboardRows.add(row);
            row = new KeyboardRow();
        }

        keyboardMarkup.setKeyboard(keyboardRows);

        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setSelective(true);
        message.setReplyMarkup(keyboardMarkup);
        try {
            lastMessageId = execute(message).getMessageId();
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void sendMessageKb(long chatId, String[][] arrStr) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.enableHtml(true);
        message.setText("");// без этого не работает

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        keyboardMarkup.getOneTimeKeyboard();

        for (int i = 0; i < arrStr.length; i++) {
            for (int j = 0; j < arrStr[i].length; j++) {
                row.add(arrStr[i][j]);
            }
            keyboardRows.add(row);
            row = new KeyboardRow();
        }

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        try {
            lastMessageId = execute(message).getMessageId();
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void DeleteKb() {
        SendMessage message = new SendMessage();
        message.setText("Удаляю");
        message.setReplyMarkup(null);
        try {
            lastMessageId = execute(message).getMessageId();
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableHtml(true);
        try {
            lastMessageId = execute(message).getMessageId();
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void editMessage(long chatId, String newMessage) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(lastMessageId);
        editMessageText.setText(newMessage);
        editMessageText.enableHtml(true);

        try {
            execute(editMessageText);
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

    //
    // КОНЕЦ БЛОКА СЛУЖБНЫХ КОМАНД
    //
}