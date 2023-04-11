Skip to content Search or jump to…Pull requests Issues Codespaces Marketplace Explore

@SlavaNoBugs SilvUpleBle/rpg-game-telegram-bot Private Fork your own copy of SilvUpleBle/rpg-game-telegram-bot Code Issues Pull requests Actions Projects Security Insights rpg-game-telegram-bot/src/main/java/io/project/TestBot/service/TelegramBot.java/@SilvUpleBle SilvUpleBle Merge branch'main'into Andrei`s_branch Latest commit 69d a8e5 24 minutes ago History 2 contributors @SilvUpleBle @SlavaNoBugs 480 lines(415 sloc)19 KB

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
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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

    private boolean waitForRequest = false;

    private String currentProcess;

    private int currentStep;

    private String previousUserMessage = "";

    static final String HELP_TEXT = "Приветик!";

    int lastMessageId;

    Message lastMessage;
    // ДЛЯ ОПТИМИЗАЦИИ И ПРАВИЛЬНОГО ВЫВОДА КОМАНД НУЖНЫМ ПОЛЬЗОВАТЕЛЯМ НУЖНО
    // СОЗДАТЬ
    // MAP, КОТОРЫЙ БУДЕТ ХРАНИТЬ В СЕБЕ ID ПОЛЬЗОВАТЕЛЯ И ДЕЙСТВИЕ, КОТОРОЕ ОН
    // ВЫПОЛНЯЕТ В ДАННЫЙ МОМЕНТ
    // ТАКЖЕ, ВОЗМОЖНО, НУЖНО БУДЕТ ХРАНИТЬ КАК-ТО ШАГ, НА КОТОРОМ ПОЛЬЗОВАТЕЛЬ
    // ОСТАНОВИЛСЯ, ТАК ЧТО МОЖЕТ ПОТРЕБОВАТЬСЯ БД

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        // listOfCommands.add(new BotCommand("/start", "switch bot on"));
        listOfCommands.add(new BotCommand("/create_hero", "create your hero"));
        listOfCommands.add(new BotCommand("/delete_hero", "delete your hero"));
        listOfCommands.add(new BotCommand("/delete_user", "delete your user and hero"));
        listOfCommands.add(new BotCommand("/timer", "timer for 15 seconds"));
        listOfCommands.add(new BotCommand("/help", "how to use this bot"));
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

        /*
         * if (update.hasCallbackQuery()) {
         * 
         * String call_data = update.getCallbackQuery().getData();
         * long message_id = update.getCallbackQuery().getMessage().getMessageId();
         * long chatId = update.getCallbackQuery().getMessage().getChatId();
         * 
         * if (call_data.equals("Yes")) {
         * 
         * CallbackQuery cb = new CallbackQuery();
         * cb.setData("message");
         * update.setCallbackQuery(cb);
         * if (update.hasMessage() && update.getMessage().hasText()) {
         * sendMessage(chatId, cb.getData());
         * }
         * }
         * }
         */

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            if (waitForRequest) {
                waitForRequest = false;

                switch (currentProcess) {
                    case "/create_hero":
                        createHero(update.getMessage(), (byte) currentStep);
                        break;
                    default:
                        break;
                }
            } else {
                long chatId = update.getMessage().getChatId();

                switch (messageText) {
                    //
                    // ТУТ БЫЛ /start, НО Я НЕ УВЕРЕН, ЧТО ОН ВООБЩЕ НУЖЕН
                    //
                    case "/create_user", "/create_user@tstbtstst_bot":
                        createUser(update.getMessage());
                        break;
                    case "/create_hero", "/create_hero@tstbtstst_bot":
                        createHero(update.getMessage(), (byte) 1);
                        currentProcess = "/create_hero";
                        break;
                    case "/delete_user", "/delete_user@tstbtstst_bot":
                        deleteUser(update.getMessage().getFrom().getId());
                        break;
                    case "/delete_hero", "/delete_hero@tstbtstst_bot":
                        deleteHero(update.getMessage().getFrom().getId());
                        break;
                    case "/timer", "/timer@tstbtstst_bot":
                        makeTimer(chatId, 10);
                        break;
                    case "/help", "/help@tstbtstst_bot":
                        sendMessage(chatId, HELP_TEXT, new String[][] { { "Новая кнопка" } });
                        // sendMessageIKB_YesNo(chatId);
                        break;

                    default:
                        sendMessage(chatId, "poshol naxui");
                        break;
                }
            }
        }
    }

    //
    // НАЧАЛО БЛОКА СОЗДАНИЯ ПЕРСОНАЖА
    ///

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
                        sendMessage(message.getFrom().getId(), "Вы уверены, что его будут звать <b><i>%s</i></b>!"
                                .formatted(previousUserMessage), new String[][] { { "Да", "Нет" } });
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
                currentProcess = "";
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
        currentProcess = "";
    }

    private void createUser(Message message) {

        if (user_table.findById(message.getChatId()).isEmpty()) {
            User userT = message.getFrom();

            UserSQL user = new UserSQL();
            user.setUserId(userT.getId());
            user.setChatId(message.getChatId());
            user.setUserName(userT.getUserName());
            user.setFirstName(userT.getFirstName());
            user.setLastName(userT.getLastName());

            try {
                ChatMember chatMember;
                chatMember = execute(new GetChatMember(String.valueOf(message.getChatId()), message.getFrom().getId()));
                if (chatMember.getStatus().equals("creator")) {
                    user.setIsAdmin(true);
                } else {
                    user.setIsAdmin(false);
                }
            } catch (TelegramApiException e) {
                log.error("Error occurred: " + e.getMessage());
            }
            if (message.getChat().isGroupChat()) {

            }
            user_table.save(user);
            sendMessage(message.getChatId(),
                    "Приветствуйте нового бога в нашем мире! Имя ему <b><i>%s</i></b>".formatted(userT.getUserName()));
            sendMessage(message.getFrom().getId(),
                    "И какой же Вы бог, о Великий, если у Вас нет героя? Ну же! Вперёд! Давайте создадим его!");
            // тут под сообщением нужно кнопочку добаваить "создать героя"!!
        } else {
            sendMessage(message.getChatId(), "Вы уже зарегистрированы!");
        }
    }

    //
    // КОНЕЦ БЛОКА СОЗДАНИЯ ПЕРСОНАЖА
    //

    //
    // НАЧАЛО БЛОКА СЛУЖБНЫХ КОМАНД
    //

    /*
     * принимает массив строк где в кейборде
     * {{инвентарь}(первая строка),{голова, торс,
     * поножи}(вторая строка)и тд}
     */
    private ReplyKeyboardMarkup createReplyKeyboard(String[][] arrStr) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
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
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setSelective(true);
        sendMessage(lastMessage.getChatId(), "RPKM создан!");
        return keyboardMarkup;
    }

    private void sendMessageIKB_YesNo(long chatId) {// InlineKeyboard да/нет
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("InlineKeyboard");
        message.enableHtml(true);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton ikb = new InlineKeyboardButton();
        ikb.setText("Да");
        ikb.setCallbackData("Yes");
        rowInline.add(ikb);
        ikb = new InlineKeyboardButton();
        ikb.setText("Нет");
        ikb.setCallbackData("No");
        rowInline.add(ikb);
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
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
            lastMessage = execute(message);
            lastMessageId = lastMessage.getMessageId();
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void sendMessage(long chatId, String textToSend, String[][] arrStr) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableHtml(true);

        sendMessage(chatId, "markup = " + String.valueOf(message.getReplyMarkup()));
        ReplyKeyboardMarkup replyKeyboardMarkup = createReplyKeyboard(arrStr);
        message.setReplyMarkup(replyKeyboardMarkup);

        try {
            lastMessage = execute(message);
            lastMessageId = lastMessage.getMessageId();
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

    private void deleteHero(long userId) {
        if (user_hero.findById(userId).isEmpty()) {
            sendMessage(userId, "У вас нет созданного героя! Чтобы создать героя, используйте /create_hero");
        } else {
            user_hero.deleteById(userId);
            sendMessage(userId, "Ваш персонаж успешно удалён!");
        }
    }

    private void deleteUser(long userId) {
        if (user_table.findById(userId).isEmpty()) {
            sendMessage(userId, "Вы еще не зарегестрированы");
        } else {
            user_table.deleteById(userId);
            deleteHero(userId);
            sendMessage(userId, "Ваш Бог успешно удалёны!");
        }
    }

    //
    // КОНЕЦ БЛОКА СЛУЖБНЫХ КОМАНД
    //
}
