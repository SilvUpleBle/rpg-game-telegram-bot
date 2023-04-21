
package io.project.TestBot.service;

import io.project.TestBot.config.BotConfig;
import io.project.TestBot.model.UserSQL;
import io.project.TestBot.model.UserState;
import io.project.TestBot.model.User_hero;
import io.project.TestBot.model.User_state;
import io.project.TestBot.model.ItemSQL;
import io.project.TestBot.model.Item_table;
import io.project.TestBot.model.TaskSQL;
import io.project.TestBot.model.Task_table;
import io.project.TestBot.model.UserHero;
import io.project.TestBot.model.User_table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.utils.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.vdurmont.emoji.EmojiParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private Item_table item_table;
    @Autowired
    private User_state user_state;
    @Autowired
    private User_table user_table;
    @Autowired
    private User_hero user_hero;
    @Autowired
    private Task_table task_table;

    final BotConfig config;

    String[] cats = {
            "https://sun9-52.userapi.com/impg/iqGx6lG3CMWR8NkDvQYu6JD3emOP0a35ror-lw/MWX-MqXDMVs.jpg?size=1280x1242&quality=96&sign=fe2dba65bfe35a69ca1185e3201d55d9&type=album",
            "https://sun9-6.userapi.com/impg/Xn57Tfyamtbfn_17JsUeQhwhvcfXcqLtm21_bA/RSycQewibHk.jpg?size=1280x1234&quality=96&sign=903fe79684c0ab209f25486e63f4ecb9&type=album",
            "https://sun9-54.userapi.com/impg/tStT-_0vwHNuzCVXku0Z-hvzH3AvN4YSXDfV7w/YwKG7pX13m8.jpg?size=1280x1224&quality=96&sign=728751d38fac0c97a0e504b28a9b16d6&type=album",
            "https://sun9-62.userapi.com/impg/VE9gMyTK8T9I3MMPlXr-5czLv4Oxwhh3ky-k_g/K-NrldqmyA0.jpg?size=1280x1225&quality=96&sign=0ddb8766cbd12ac72a75a3cccb4d5252&type=album" };

    static final String HELP_TEXT = "help text";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/create_hero", "создать героя"));
        listOfCommands.add(new BotCommand("/create_user", "создать пользователя"));
        listOfCommands.add(new BotCommand("/delete_hero", "удалить героя"));
        listOfCommands.add(new BotCommand("/delete_user", "удалить пользователя и героя"));
        listOfCommands.add(new BotCommand("/get_rights", "получить права администратора"));
        listOfCommands.add(new BotCommand("/cancel", "сбросить текущее состояние пользователя"));
        listOfCommands.add(new BotCommand("/help", "вывести help-сообщение"));
        try {
            execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
            execute(new SendMessage(String.valueOf(778258104), "Я проснувся!"));

            // ItemSQL item = new ItemSQL();
            // item.setItemId(Long.valueOf(1));
            // item.setItemName("яблоко");
            // item.setItemType("heal");
            // item.setItemLevel(1);
            // item_table.save(item);
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

        if (update.hasCallbackQuery()) {
            log.info("callBackData = %s".formatted(update.getCallbackQuery().getData()));
            Message newMessage = new Message();

            newMessage.setFrom(update.getCallbackQuery().getFrom());
            newMessage.setText(update.getCallbackQuery().getData());

            Chat chat = new Chat();
            chat.setType("private");
            chat.setId(update.getCallbackQuery().getMessage().getChatId());
            newMessage.setChat(chat);

            update.setMessage(newMessage);
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText;
            if (update.getMessage().getText().split(" ").length > 1) {
                messageText = update.getMessage().getText().split(" ")[0];
            } else {
                messageText = update.getMessage().getText();
            }
            long chatId = update.getMessage().getFrom().getId();
            UserState user;

            if (user_state.findById(update.getMessage().getFrom().getId()).isEmpty()) {
                user = new UserState();
                user.setUserId(update.getMessage().getFrom().getId());
                user.setWaitForRequest(false);
                user.setStep(0);

                user_state.save(user);
            } else {
                user = user_state.findById(update.getMessage().getFrom().getId()).get();
            }

            if (user.getWaitForRequest()
                    && !(messageText.equals("/cancel") || messageText.equals("/cancel@tstbtstst_bot"))) {
                user.setWaitForRequest(false);
                user_state.save(user);

                switch (user.getProcess()) {
                    case "/create_hero":
                        createHero(update.getMessage(), (byte) user.getStep());
                        break;
                    case "/create_task":
                        createTask(update.getMessage(), (byte) user.getStep());
                        break;
                    case "/delete_user":
                        deleteUser(update.getMessage(), (byte) user.getStep());
                        break;
                    case "/delete_hero":
                        deleteHero(update.getMessage(), (byte) user.getStep());
                        break;
                    default:
                        break;

                }

            } else {
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
                            user.setProcess("/delete_user");
                            user_state.save(user);
                            deleteUser(update.getMessage(), (byte) 1);
                            break;
                        case "/delete_hero", "/delete_hero@tstbtstst_bot":
                            user.setProcess("/delete_hero");
                            user_state.save(user);
                            deleteHero(update.getMessage(), (byte) 1);
                            break;
                        case "/timer", "/timer@tstbtstst_bot": // ПЕРЕДЕЛАТЬ
                            user.setProcess("/timer"); // ПЕРЕДЕЛАТЬ
                            user_state.save(user); // ПЕРЕДЕЛАТЬ
                            makeTimer(chatId, 15); // ПЕРЕДЕЛАТЬ
                            user.setProcess(null); // ПЕРЕДЕЛАТЬ
                            user_state.save(user); // ПЕРЕДЕЛАТЬ
                            break; // ПЕРЕДЕЛАТЬ
                        case "/cancel", "/cancel@tstbtstst_bot":
                            cancelWithText(update.getMessage().getFrom().getId());
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
                        case "/menu", "/menu@tstbtstst_bot":
                            showMenu(update.getMessage().getFrom().getId());
                            break;
                        case "/profile", "/profile@tstbtstst_bot":
                            showProfile(update.getMessage().getFrom().getId());
                            break;
                        case "/hero", "/hero@tstbtstst_bot":
                            showHero(update.getMessage().getFrom().getId());
                            break;
                        case "/tasks", "/tasks@tstbtstst_bot":
                            showTasksList(update.getMessage().getFrom().getId());
                            break;
                        case "/administration", "/administration@tstbtstst_bot":
                            showAdministration(update.getMessage().getFrom().getId());
                            break;
                        case "/rating", "/rating@tstbtstst_bot":
                            showRating(update.getMessage().getFrom().getId());
                            break;
                        case "/showUsers", "/showUsers@tstbtstst_bot":
                            showUsers(update.getMessage().getFrom().getId());
                            break;
                        case "/showUserInfo", "/showUserInfo@tstbtstst_bot":
                            showUserInfo(update.getMessage().getFrom().getId(),
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/takeAdministrationRights", "/takeAdministrationRights@tstbtstst_bot":
                            takeAdministrationRights(update.getMessage().getFrom().getId(),
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/giveAdministrationRights", "/giveAdministrationRights@tstbtstst_bot":
                            giveAdministrationRights(update.getMessage().getFrom().getId(),
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/giveItem", "/giveItem@tstbtstst_bot":
                            giveItem(update.getMessage().getFrom().getId(),
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/heroInventory", "/heroInventory@tstbtstst_bot":
                            showHeroInventory(update.getMessage().getFrom().getId());
                            break;
                        case "/heroPet", "/heroPet@tstbtstst_bot":
                            showUnderConstruct(update.getMessage().getFrom().getId(),
                                    new Pair<String, String>("Назад", "/hero"));
                            break;

                        case "/createItems":
                            List<ItemSQL> list = new ArrayList<>();
                            list.add(new ItemSQL((long) 1, "яблоко", "heal", 1));
                            list.add(new ItemSQL((long) 2, "палка-убивалка", "weapon", 1));
                            list.add(new ItemSQL((long) 3, "клоунский колпак", "head", 1));
                            list.add(new ItemSQL((long) 4, "алмазный нагрудник", "chest", 1));
                            list.add(new ItemSQL((long) 5, "штаны из берёзовый коры", "legs", 1));
                            list.add(new ItemSQL((long) 6, "сапоги-скороходы", "foots", 1));
                            list.add(new ItemSQL((long) 7, "кольцо всевластия", "talisman", 1));
                            list.add(new ItemSQL((long) 8, "тетрадь в горошек", "loot", 1));
                            createItems(list);
                            break;
                        default:
                            sendMessage(chatId, "Не понимаю команду!");
                            user.setLastUserMessage(null);
                            user_state.save(user);
                            break;
                    }
                } else {
                    sendMessage(chatId,
                            "Выполнение предыдущей функции ещё не завершено!\nЕсли возникла ошибка, используйте /cancel!");
                    cancel(update.getMessage().getFrom().getId());
                }
            }
        }
    }

    //
    // НАЧАЛО БЛОКА СОЗДАНИЯ ПЕРСОНАЖА
    //
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
                        sendMessageWithInlineButtons(message.getFrom().getId(),
                                "Вы уверены, что его будут звать <b><i>%s</i></b>!"
                                        .formatted(message.getText()),
                                new String[][] { { "Да", "Нет" } });

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
                sendMessage(message.getFrom().getId(), "Хорошо, попробуй снова!\nЕсли хочешь выйти, то напиши /cancel");
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

    private void showMenu(long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Профиль", "/profile"));
        list.get(0).add(new Pair<String, String>("Герой", "/hero"));
        list.get(1).add(new Pair<String, String>("Задачи", "/tasks"));
        list.get(1).add(new Pair<String, String>("Рейтинг", "/rating"));
        if (user_table.findById(userId).get().isAdmin()) {
            list.add(new ArrayList<>());
            list.get(2)
                    .add(new Pair<String, String>(
                            EmojiParser.parseToUnicode(":hammer:") + "Администрирование"
                                    + EmojiParser.parseToUnicode(":hammer:"),
                            "/administration"));
        }

        if (user_state.findById(userId).get().getLastUserMessage() != null
                && (user_state.findById(userId).get().getLastUserMessage().equals("/profile")
                        || user_state.findById(userId).get().getLastUserMessage().equals("/hero")
                        || user_state.findById(userId).get().getLastUserMessage().equals("/tasks")
                        || user_state.findById(userId).get().getLastUserMessage().equals("/administration")
                        || user_state.findById(userId).get().getLastUserMessage().equals("/rating"))) {
            editMessage(userId, "Меню:", list);
        } else {
            sendMessageWithInlineButtons(userId, "Меню:", list);
        }

        UserState user = user_state.findById(userId).get();
        user.setLastUserMessage("/menu");
        user_state.save(user);
    }

    private void showProfile(long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Назад", "/menu"));

        UserSQL user = user_table.findById(userId).get();

        editMessage(userId, "Ваш профиль:" +
                "\n\n<b>Ваше имя:</b> \t" + user.getFirstName() +
                "\n<b>Права администратора:</b> \t" + user.isAdmin() +
                "\n<b>Количество Ваших очков:</b> \t" + user.getPoints() +
                "\n<b>Количество активных задач:</b> \t"
                + getIdsFromString(user.getActiveTasks(), ";").size(), list);

        UserState userS = user_state.findById(userId).get();
        userS.setLastUserMessage("/profile");
        user_state.save(userS);
    }

    private void showHero(long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        String textToSend;
        if (user_hero.findById(userId).isEmpty()) {
            textToSend = "У Вас нет героя!\nИспользуйте /create_hero, чтобы создать его!";

        } else {
            textToSend = "Меню героя:";
            list.add(new ArrayList<>());
            list.add(new ArrayList<>());
            list.get(0).add(new Pair<String, String>("Информация", "/heroInfo"));
            list.get(0).add(new Pair<String, String>("Экипировка", "/heroEquipment"));
            list.get(1).add(new Pair<String, String>("Инвентарь", "/heroInventory"));
            list.get(1).add(new Pair<String, String>("Питомец" + EmojiParser.parseToUnicode(":hammer:"), "/heroPet"));
        }

        list.get(list.size() - 1).add(new Pair<String, String>("Назад", "/menu"));
        editMessage(userId, textToSend, list);

        UserState user = user_state.findById(userId).get();
        user.setLastUserMessage("/hero");
        user_state.save(user);
    }

    private void showHeroInventory(Long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Назад", "/hero"));

        String textToSend = "Инвентарь:\n\n";
        UserHero hero = user_hero.findById(userId).get();
        List<Long> items = getIdsFromString(hero.getInventory(), ";");
        if (items.isEmpty()) {
            textToSend += "Ваш инвентарь пуст!";
        } else {
            for (Long itemId : items) {
                textToSend += item_table.findById(itemId).get().toString() + "\n";
            }
        }
        editMessage(userId, textToSend, list);
    }

    private void showTasksList(long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        UserSQL user = user_table.findById(userId).get();
        List<Long> tasksId = getIdsFromString(user.getActiveTasks(), ";");
        String textToSend = "Ваши задачи:";
        if (tasksId.isEmpty()) {
            textToSend += "\n\nУ Вас нет активных задач!";
        } else {
            for (int i = 0; i < tasksId.size(); i++) {
                list.add(new ArrayList<>());
                list.get(i).add(new Pair<String, String>(task_table.findById(tasksId.get(i)).get().getTaskName(),
                        "/getTask " + tasksId.get(i)));
            }
        }

        list.add(new ArrayList<>());
        list.get(tasksId.size()).add(new Pair<String, String>("Назад", "/menu"));

        editMessage(userId, textToSend, list);

        UserState userS = user_state.findById(userId).get();
        userS.setLastUserMessage("/tasks");
        user_state.save(userS);
    }

    private void showAdministration(long userId) {
        if (!user_table.findById(userId).get().isAdmin()) {
            sendMessage(userId, "Вы не обладаете правами администратора!");
            return;
        }
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Пользователи", "/showUsers"));
        list.get(1).add(new Pair<String, String>("Сбросить очки", "/dropAllPoints"));
        list.get(1).add(new Pair<String, String>("Задачи", "/adminTasks"));
        list.get(2).add(new Pair<String, String>("Назад", "/menu"));
        editMessage(userId, "Меню администратора:", list);

        UserState userS = user_state.findById(userId).get();
        userS.setLastUserMessage("/administration");
        user_state.save(userS);
    }

    private void showRating(Long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Назад", "/menu"));
        UserSQL user = user_table.findById(userId).get();
        Iterable<UserSQL> allUsers = user_table.findAll();

        List<UserSQL> userFromCurrentChat = new ArrayList<>();
        for (UserSQL userSQL : allUsers) {
            if (userSQL.getChatId() == user.getChatId()) {
                userFromCurrentChat.add(userSQL);
            }
        }

        Collections.sort(userFromCurrentChat);
        String textToSend = "Рейтинг пользователей:\n\n";
        int pos = 1;
        for (UserSQL userSQL : userFromCurrentChat) {
            if (user.getUserId() == userSQL.getUserId()) {
                textToSend += "<b>%d. @%s (%s %s) - %d</b>\n".formatted(pos, userSQL.getUserName(),
                        userSQL.getFirstName(),
                        userSQL.getLastName(), userSQL.getPoints());
            } else {
                textToSend += "%d. @%s (%s %s) - %d\n".formatted(pos, userSQL.getUserName(), userSQL.getFirstName(),
                        userSQL.getLastName(), userSQL.getPoints());
            }
            pos++;
        }

        editMessage(userId, textToSend, list);
        UserState userS = user_state.findById(userId).get();
        userS.setLastUserMessage("/rating");
        user_state.save(userS);
    }

    private void showUnderConstruct(Long userId, Pair<String, String> pair) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.get(0).add(pair);
        sendMessageWithPicture(userId, "Этот раздел пока ещё в разработке!",
                cats[ThreadLocalRandom.current().nextInt(0, 3)], list);
    }

    // ИЗУЧИТЬ ТЕМУ С ЗАПРОСАМИ ЧЕРЕЗ HIBERNATE (ПОТОМУ ЧТО chat_id не является @Id)
    private void showUsers(long userId) {
        if (!user_table.findById(userId).get().isAdmin()) {
            sendMessage(userId, "Вы не обладаете правами администратора!");
            return;
        }

        long chatId = user_table.findById(userId).get().getChatId();
        Iterable<UserSQL> allUsers = user_table.findAll();
        List<UserSQL> users = new ArrayList<>();
        for (UserSQL userSQL : allUsers) {
            if (userSQL.getChatId() == chatId) {
                users.add(userSQL);
            }
        }

        List<List<Pair<String, String>>> list = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            list.add(new ArrayList<>());
            list.get(i)
                    .add(new Pair<String, String>(users.get(i).getUserName(),
                            "/showUserInfo " + users.get(i).getUserId()));
        }
        list.add(new ArrayList<>());
        list.get(list.size() - 1).add(new Pair<String, String>("Назад", "/administration"));
        editMessage(userId, "Список пользователей в беседе:", list);

        UserState userS = user_state.findById(userId).get();
        userS.setLastUserMessage("/showUsers");
        user_state.save(userS);
        // Query query = session.createQuery("FROM user_table WHERE chat_id = :chatId");
        // Query query = org.hibernate.Session.createQuery("FROM user_table WHERE
        // chat_id = :chatId");
        // query.setParameter();
    }

    private void showUserInfo(long chatId, long userId) {
        if (!user_table.findById(chatId).get().isAdmin()) {
            sendMessage(userId, "Вы не обладаете правами администратора!");
            return;
        }
        UserSQL user = user_table.findById(userId).get();

        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        if (chatId == userId) {

        } else {
            list.add(new ArrayList<>());
            if (user_table.findById(userId).get().isAdmin()) {
                list.get(0)
                        .add(new Pair<String, String>("Забрать админку",
                                "/takeAdministrationRights " + user.getUserId()));
            } else {
                list.get(0)
                        .add(new Pair<String, String>("Выдать админку",
                                "/giveAdministrationRights " + user.getUserId()));
            }
        }
        list.get(list.size() - 1).add(new Pair<String, String>("Назад", "/showUsers"));

        editMessage(chatId, "Профиль пользователя <b>%s</b>:".formatted(user.getUserName()) +
                "\n\n<b>Имя:</b> \t" + user.getFirstName() +
                "\n<b>Права администратора:</b> \t" + user.isAdmin() +
                "\n<b>Количество очков:</b> \t" + user.getPoints() +
                "\n<b>Количество активных задач:</b> \t"
                + getIdsFromString(user.getActiveTasks(), ";").size(), list);

        UserState userS = user_state.findById(userId).get();
        userS.setLastUserMessage("/showUserInfo");
        user_state.save(userS);
    }

    private void giveAdministrationRights(Long chatId, Long userId) {
        UserSQL user = user_table.findById(userId).get();
        user.setIsAdmin(true);
        user_table.save(user);

        showUserInfo(chatId, userId);
    }

    private void takeAdministrationRights(Long chatId, Long userId) {
        UserSQL user = user_table.findById(userId).get();
        user.setIsAdmin(false);
        user_table.save(user);

        showUserInfo(chatId, userId);
    }

    private void createItems(List<ItemSQL> list) {
        for (ItemSQL item : list) {
            item_table.save(item);
        }
    }

    private void giveItem(Long userId, Long itemId) {
        UserHero hero = user_hero.findById(userId).get();
        hero.addToInventory(itemId);
        user_hero.save(hero);

        sendMessage(userId, "Вы получили предмет <b>%s</b>!".formatted(item_table.findById(itemId).get().toString()));
    }

    //
    // НАЧАЛО БЛОКА СЛУЖБНЫХ КОМАНД
    //

    private List<Long> getIdsFromString(String text, String separator) {
        List<Long> list = new ArrayList<>();
        if (text == null) {
            return list;
        }

        String[] strList = text.split(separator);
        for (String element : strList) {
            list.add(Long.valueOf(element));
        }
        return list;
    }

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
                if (user_table.findById(message.getFrom().getId()).get().isAdmin()) {

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
                                    task.setTaskDescription(user.getLastUserMessage().substring(arr[0].length() + 1));
                                    task.setTaskName(arr[0]);
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
                sendMessage(message.getFrom().getId(), "Вы не зарегистрированны");
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

    private void editMessage(long chatId, String newMessage, List<List<Pair<String, String>>> buttons) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(user_state.findById(chatId).get().getIdLastBotMessage());
        editMessageText.setText(newMessage);
        editMessageText.setReplyMarkup(createInlineKeyboard(buttons));
        editMessageText.enableHtml(true);

        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void deleteLastMessage(long chatId) {
        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId),
                user_state.findById(chatId).get().getIdLastBotMessage());

        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void sendMessageWithInlineButtons(long chatId, String textToSend, String[][] buttons) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableHtml(true);
        message.setReplyMarkup(createInlineKeyboard(buttons));

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

    private void sendMessageWithInlineButtons(long chatId, String textToSend,
            List<List<Pair<String, String>>> buttons) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableHtml(true);
        message.setReplyMarkup(createInlineKeyboard(buttons));

        try {
            Message msg = execute(message);

            if (!user_state.findById(chatId).isEmpty()) {
                log.info("Сохранил id сообщения: " + msg.getMessageId());
                UserState user = user_state.findById(chatId).get();
                user.setIdLastBotMessage(msg.getMessageId());
                user_state.save(user);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private InlineKeyboardMarkup createInlineKeyboard(String[][] buttons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> rowButtons = new ArrayList<>();
        InlineKeyboardButton button;

        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons[i].length; j++) {
                button = new InlineKeyboardButton(buttons[i][j]);
                button.setCallbackData(buttons[i][j]);
                rowButtons.add(button);
            }
            rowList.add(rowButtons);
            rowButtons = new ArrayList<>();
        }

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup createInlineKeyboard(List<List<Pair<String, String>>> buttons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> rowButtons = new ArrayList<>();
        InlineKeyboardButton button;

        for (int i = 0; i < buttons.size(); i++) {
            for (int j = 0; j < buttons.get(i).size(); j++) {
                button = new InlineKeyboardButton(buttons.get(i).get(j).getFirst());
                button.setCallbackData(buttons.get(i).get(j).getSecond());
                rowButtons.add(button);
            }
            rowList.add(rowButtons);
            rowButtons = new ArrayList<>();
        }

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
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

    private void sendMessageWithPicture(long chatId, String textToSend, String imageUrlToSend,
            List<List<Pair<String, String>>> buttons) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(String.valueOf(chatId));
        photo.setCaption(textToSend);
        photo.setPhoto(new InputFile(imageUrlToSend));
        photo.setReplyMarkup(createInlineKeyboard(buttons));

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

    private void cancel(Long userId) {
        UserState user = user_state.findById(userId).get();
        user.setProcess(null);
        user.setStep(0);
        user.setWaitForRequest(false);
        user_state.save(user);
    }

    private void cancelWithText(Long userId) {
        cancel(userId);
        sendMessage(userId, "Выполнение действия прекращено!");
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

    private void deleteHero(Message message, byte step) {
        switch (step) {
            case 1:
                if (user_hero.findById(message.getFrom().getId()).isEmpty()) {
                    sendMessage(message.getFrom().getId(),
                            "У вас нет созданного героя! Чтобы создать героя, используйте /create_hero");
                    cancel(message.getFrom().getId());
                } else {
                    sendMessageWithInlineButtons(message.getFrom().getId(),
                            "Вы уверены, что хотите удалить своего героя?", new String[][] { { "Да", "Нет" } });

                    UserState user = user_state.findById(message.getFrom().getId()).get();
                    user.setWaitForRequest(true);
                    user.setStep(2);
                    user_state.save(user);
                }
                break;
            case 2:
                switch (message.getText()) {
                    case "Да", "да":
                        user_hero.deleteById(message.getFrom().getId());
                        sendMessage(message.getFrom().getId(), "Ваш герой успешно удалён!");
                        cancel(message.getFrom().getId());
                        break;
                    case "Нет", "нет":
                        cancelWithText(message.getFrom().getId());
                        break;
                    default:
                        sendMessage(message.getFrom().getId(), "Не понимаю команду!");
                        cancel(message.getFrom().getId());
                        break;
                }
                break;
        }
    }

    private void deleteUser(Message message, byte step) {
        switch (step) {
            case 1:
                if (user_table.findById(message.getFrom().getId()).isEmpty()) {
                    sendMessage(message.getFrom().getId(),
                            "У вас нет созданного Бога! Чтобы создать Бога, используйте /create_user");
                    cancelWithText(message.getFrom().getId());
                } else {
                    sendMessageWithInlineButtons(message.getFrom().getId(),
                            "Вы уверены, что хотите удалить своего Бога и его героя?",
                            new String[][] { { "Да", "Нет" } });

                    UserState user = user_state.findById(message.getFrom().getId()).get();
                    user.setWaitForRequest(true);
                    user.setStep(2);
                    user_state.save(user);
                }
                break;
            case 2:
                switch (message.getText()) {
                    case "Да", "да":
                        user_hero.deleteById(message.getFrom().getId());
                        user_table.deleteById(message.getFrom().getId());
                        sendMessage(message.getFrom().getId(), "Ваш Бог и его герой успешно удалёны!");
                        cancel(message.getFrom().getId());
                        break;
                    case "Нет", "нет":
                        cancelWithText(message.getFrom().getId());
                        break;
                    default:
                        sendMessage(message.getFrom().getId(), "Не понимаю команду!");
                        cancelWithText(message.getFrom().getId());
                        break;
                }
                break;
        }
    }

    //
    // КОНЕЦ БЛОКА СЛУЖБНЫХ КОМАНД
    //
}
