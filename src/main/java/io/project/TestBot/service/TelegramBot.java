
package io.project.TestBot.service;

import io.project.TestBot.config.BotConfig;
import io.project.TestBot.model.UserSQL;
import io.project.TestBot.model.UserState;
import io.project.TestBot.model.User_hero;
import io.project.TestBot.model.User_state;
import io.project.TestBot.model.TaskSQL;
import io.project.TestBot.model.Task_table;
import io.project.TestBot.model.UserHero;
import io.project.TestBot.model.User_table;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Data;
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
    private User_state user_state;
    @Autowired
    private User_table user_table;
    @Autowired
    private User_hero user_hero;
    @Autowired
    private Task_table task_table;

    final BotConfig config;

    static final String HELP_TEXT = "help text";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/create_hero", "создать героя"));
        listOfCommands.add(new BotCommand("/create_user", "создать пользователя"));
        listOfCommands.add(new BotCommand("/delete_hero", "удалить героя"));
        listOfCommands.add(new BotCommand("/delete_user", "удалить пользователя и героя"));
        listOfCommands.add(new BotCommand("/get_rights", "получить права администратора"));
        listOfCommands.add(new BotCommand("/create_task", "создать задачу"));
        listOfCommands.add(new BotCommand("/timer", "таймер на 15 секунд"));
        listOfCommands.add(new BotCommand("/cancel", "сбросить текущее состояние пользователя"));
        listOfCommands.add(new BotCommand("/help", "вывести help-сообщение"));
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

        UserState user = new UserState();
        if (user_state.findById(update.getMessage().getFrom().getId()).isEmpty()) {
            user.setUserId(update.getMessage().getFrom().getId());
            user.setWaitForRequest(false);
            user.setStep(0);

            user_state.save(user);
        } else {
            user = user_state.findById(update.getMessage().getFrom().getId()).get();
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            if (user.getWaitForRequest()
                    && (!messageText.equals("/cancel") || !messageText.equals("/cancel@tstbtstst_bot"))) {
                user.setWaitForRequest(false);
                user_state.save(user);

                switch (user.getProcess()) {
                    case "/create_hero":
                        createHero(update.getMessage(), (byte) user.getStep());
                        break;
                    case "/create_task":
                        createTask(update.getMessage(), (byte) user.getStep());
                        break;
                    default:
                        break;
                }
            } else {
                long chatId = update.getMessage().getChatId();
                if (messageText.equals("/cancel") || user.getProcess() == null
                        || messageText.equals("/cancel@tstbtstst_bot")) {
                    switch (messageText) {
                        case "/create_user", "/create_user@tstbtstst_bot":
                            createUser(update.getMessage());
                            break;
                        case "/create_hero", "/create_hero@tstbtstst_bot":
                            user.setProcess("/create_hero");
                            user_state.save(user);
                            createHero(update.getMessage(), (byte) 1);
                            break;
                        case "/delete_user", "/delete_user@tstbtstst_bot":
                            deleteHero(update.getMessage().getFrom().getId());
                            deleteUser(update.getMessage().getFrom().getId());
                            break;
                        case "/delete_hero", "/delete_hero@tstbtstst_bot":
                            deleteHero(update.getMessage().getFrom().getId());
                            break;
                        case "/timer", "/timer@tstbtstst_bot":
                            user.setProcess("/timer");
                            user_state.save(user);
                            makeTimer(chatId, 15);
                            user.setProcess(null);
                            user_state.save(user);
                            break;
                        case "/cancel", "/cancel@tstbtstst_bot":
                            user.setProcess(null);
                            user.setStep((byte) 0);
                            user.setWaitForRequest(false);
                            user_state.save(user);
                            break;
                        case "/help", "/help@tstbtstst_bot":
                            sendMessage(chatId, HELP_TEXT, new String[][] { { "/cancel" } });
                            break;
                        case "/create_task", "/create_task@tstbtstst_bot":
                            user.setProcess("/create_task");
                            user_state.save(user);
                            createTask(update.getMessage(), (byte) 1);
                            break;
                        case "/get_rights", "/get_rights@tstbtstst_bot":
                            getAdminRights(update.getMessage());
                            break;

                        default:
                            sendMessage(chatId, "Не понимаю команду!");
                            break;
                    }
                } else {
                    sendMessage(chatId, "Ещё не закончено выполнение предыдущей функции!");
                }
            }
        }
    }

    //
    // НАЧАЛО БЛОКА СОЗДАНИЯ ПЕРСОНАЖА
    ///

    private void createHero(Message message, byte step) {
        UserState user = user_state.findById(message.getFrom().getId()).get();
        if (message.getChat().isUserChat()) {
            if (user_table.findById(message.getFrom().getId()).isEmpty()) {
                sendMessage(message.getFrom().getId(), "Сперва создайте своего Бога, используя /create_user!");
                user.setProcess(null);
                user_state.save(user);
                return;
            }
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

                        user.setWaitForRequest(true);
                        user.setStep((byte) 3);
                        user_state.save(user);
                        break;
                    case 3:
                        sendMessage(message.getFrom().getId(), "Вы уверены, что его будут звать <b><i>%s</i></b>!"
                                .formatted(message.getText()), new String[][] { { "Да", "Нет" } });

                        user.setLastUserMessage(message.getText());
                        user.setWaitForRequest(true);
                        user.setStep((byte) 4);
                        user_state.save(user);
                        break;
                    case 4:
                        createHero(message, user.getLastUserMessage());
                        break;
                    default:
                        break;
                }
            } else {
                user.setProcess(null);
                user_state.save(user);
                sendMessage(message.getFrom().getId(),
                        "У вас уже есть персонаж <b><i>%s</i></b>!"
                                .formatted(user_hero.findById(message.getFrom().getId()).get().getHeroName()));
            }
        } else {
            sendMessage(message.getChatId(), "Используйте эту команду в личных сообщениях с ботом!");
            user.setProcess(null);
            user_state.save(user);
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

        UserState userState = user_state.findById(userId).get();
        userState.setProcess(null);
        userState.setStep(0);
        user_state.save(userState);
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
    private void getAdminRights(Message message) {
        UserSQL userSQL = user_table.findById(message.getFrom().getId()).get();
        userSQL.setIsAdmin(true);
        sendMessage(message.getChatId(), "Права получены");
        user_table.save(userSQL);
    }

    private void createTask(Message message, byte step) {
        UserState user = user_state.findById(message.getFrom().getId()).get();
        if (message.getChat().isUserChat()) {
            if (!user_table.findById(message.getFrom().getId()).isEmpty()) {
                if (user_table.findById(message.getFrom().getId()).get().getIsAdmin()) {

                    switch (step) {
                        case 1:
                            sendMessage(message.getFrom().getId(),
                                    message.getChat().getFirstName() + ", приступим к созданию задания");
                            log.info("Start creating task " + message.getChat().getFirstName());
                            createTask(message, (byte) 2);
                            break;
                        case 2:
                            sendMessage(message.getFrom().getId(),
                                    "Введите название задания и через пробел его описание");
                            user.setWaitForRequest(true);
                            user.setStep((byte) 3);
                            user_state.save(user);
                            break;
                        case 3:
                            user.setLastUserMessage(message.getText());
                            sendMessage(message.getFrom().getId(), "Проверьте, все так? <b><i>%s</i></b>!"
                                    .formatted(user.getLastUserMessage()), new String[][] { { "Да", "Нет" } });
                            user.setWaitForRequest(true);
                            user.setStep((byte) 4);
                            user_state.save(user);
                            break;
                        case 4:
                            switch (message.getText()) {
                                case "Да", "да":
                                    TaskSQL task = new TaskSQL();
                                    String[] arr = user.getLastUserMessage().split(" ");

                                    Long randomInt;
                                    do {
                                        randomInt = ThreadLocalRandom.current().nextLong(0, 10000);
                                    } while (!task_table.findById((long) randomInt).isEmpty());

                                    task.setTaskId(randomInt);
                                    task.setCreatorId(message.getFrom().getId());
                                    task.setTaskDescription(arr[0]);
                                    task.setTaskName(user.getLastUserMessage().substring(arr[0].length() + 1));
                                    task.setTaskType("real life task");
                                    task_table.save(task);

                                    user.setProcess(null);
                                    user.setStep(0);
                                    user_state.save(user);
                                    sendMessage(message.getFrom().getId(), "Создание окончено!");
                                    break;
                                case "Нет", "нет":
                                    createTask(message, (byte) 2);
                                    break;
                                default:
                                    break;
                            }
                            break;

                        default:
                            break;
                    }
                } else {
                    sendMessage(message.getChatId(), "Вы не администратор");
                    user.setProcess(null);
                    user_state.save(user);
                }
            } else {
                user.setProcess(null);
                user_state.save(user);
                sendMessage(message.getFrom().getId(), "Вы не зарегестрированны");
            }
        } else {
            sendMessage(message.getChatId(), "Используйте эту команду в личных сообщениях с ботом!");
            user.setProcess(null);
            user_state.save(user);
        }
    }

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
            Message msg = execute(message);

            if (!user_state.findById(chatId).isEmpty()) {
                UserState user = user_state.findById(chatId).get();
                user.setIdLastBotMessage(msg.getMessageId());
            }
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
            Message msg = execute(message);

            if (!user_state.findById(chatId).isEmpty()) {
                UserState user = user_state.findById(chatId).get();
                user.setIdLastBotMessage(msg.getMessageId());
                user_state.save(user);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void sendMessage(long chatId, String textToSend, String[][] arrStr) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableHtml(true);
        message.setReplyMarkup(createReplyKeyboard(arrStr));

        try {
            Message msg = execute(message);

            if (!user_state.findById(chatId).isEmpty()) {
                UserState user = user_state.findById(chatId).get();
                user.setIdLastBotMessage(msg.getMessageId());
                user_state.save(user);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void editMessage(long chatId, String newMessage) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(user_state.findById(chatId).get().getIdLastBotMessage());
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
            Message msg = execute(photo);

            if (!user_state.findById(chatId).isEmpty()) {
                UserState user = user_state.findById(chatId).get();
                user.setIdLastBotMessage(msg.getMessageId());
                user_state.save(user);
            }
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
            Message msg = execute(photo);

            if (!user_state.findById(chatId).isEmpty()) {
                UserState user = user_state.findById(chatId).get();
                user.setIdLastBotMessage(msg.getMessageId());
                user_state.save(user);
            }
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
            sendMessage(userId, "Ваш Бог успешно удалёны!");
        }
    }

    //
    // КОНЕЦ БЛОКА СЛУЖБНЫХ КОМАНД
    //
}
