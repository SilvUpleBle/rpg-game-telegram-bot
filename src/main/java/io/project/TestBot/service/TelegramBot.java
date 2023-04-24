
package io.project.TestBot.service;

import io.project.TestBot.config.BotConfig;
import io.project.TestBot.model.UserSQL;
import io.project.TestBot.model.UserState;
import io.project.TestBot.model.User_hero;
import io.project.TestBot.model.User_state;
import io.project.TestBot.model.BattleSQL;
import io.project.TestBot.model.Battle_table;
import io.project.TestBot.model.GroupSQL;
import io.project.TestBot.model.Hero_groups;
import io.project.TestBot.model.ItemSQL;
import io.project.TestBot.model.Item_table;
import io.project.TestBot.model.SkillSQL;
import io.project.TestBot.model.Skill_table;
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
    @Autowired
    private Hero_groups hero_groups;
    @Autowired
    private Skill_table skill_table;
    @Autowired
    private Battle_table battle_table;

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
                if (messageText.length() > 255) {
                    sendMessage(chatId, "Входящее сообщение не должно превышать 255 символов! Попробуйте ещё раз!");
                    return;
                }
                user.setWaitForRequest(false);
                user_state.save(user);

                switch (user.getProcess()) {
                    case "/create_hero":
                        createHero(update.getMessage(), (byte) user.getStep());
                        break;
                    case "/create_task":
                        createTask(update.getMessage(), (byte) user.getStep());
                        break;
                    case "/edit_task":
                        editTask(update.getMessage(), (byte) user.getStep());
                        break;
                    case "/delete_task":
                        deleteTask(update.getMessage(), (byte) user.getStep());
                        break;
                    case "/delete_user":
                        deleteUser(update.getMessage(), (byte) user.getStep());
                        break;
                    case "/delete_hero":
                        deleteHero(update.getMessage(), (byte) user.getStep());
                        break;
                    case "/createGroup":
                        user.setLastUserMessage(update.getMessage().getText());
                        user_state.save(user);
                        createGroup(update.getMessage().getFrom().getId());
                        break;
                    case "/changeHeroName":
                        user.setLastUserMessage(update.getMessage().getText());
                        user_state.save(user);
                        changeHeroName(update.getMessage().getFrom().getId());
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
                        // TODO переделать таймер в асинхрон
                        case "/timer", "/timer@tstbtstst_bot":
                            user.setProcess("/timer");
                            user_state.save(user);
                            makeTimer(chatId, 15);
                            user.setProcess(null);
                            user_state.save(user);
                            break;
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
                        case "/edit_task":
                            user.setProcess("/edit_task");
                            user_state.save(user);
                            editTask(update.getMessage(), (byte) 1);
                            break;
                        case "/delete_task":
                            user.setProcess("/delete_task");
                            user_state.save(user);
                            deleteTask(update.getMessage(), (byte) 1);
                            break;
                        case "/show_creators_tasks":
                            user.setProcess("/show_creators_tasks");
                            user_state.save(user);
                            showCreatorsTasks(update.getMessage());
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
                        case "/heroProfile", "/heroProfile@tstbtstst_bot":
                            showHeroProfile(update.getMessage().getFrom().getId());
                            break;
                        case "/heroSkills", "/heroSkills@tstbtstst_bot":
                            showHeroSkills(update.getMessage().getFrom().getId());
                            break;
                        case "/heroGroup", "/heroGroup@tstbtstst_bot":
                            showHeroGroup(update.getMessage().getFrom().getId());
                            break;
                        case "/heroGroupList", "/heroGroupList@tstbtstst_bot":
                            showHeroGroupList(update.getMessage().getFrom().getId());
                            break;
                        case "/leaveGroup", "/leaveGroup@tstbtstst_bot":
                            leaveGroup(update.getMessage().getFrom().getId());
                            break;
                        case "/deleteGroup", "/deleteGroup@tstbtstst_bot":
                            deleteGroup(update.getMessage().getFrom().getId());
                            showHeroGroup(update.getMessage().getFrom().getId());
                            break;
                        case "/createGroup", "/createGroup@tstbtstst_bot":
                            user.setProcess("/createGroup");
                            user.setStep(1);
                            user_state.save(user);
                            createGroup(update.getMessage().getFrom().getId());
                            break;
                        case "/showInviteToGroup", "/showInviteToGroup@tstbtstst_bot":
                            showInviteToGroup(update.getMessage().getFrom().getId());
                            break;
                        case "/inviteToGroup", "/inviteToGroup@tstbtstst_bot":
                            inviteToGroup(update.getMessage().getFrom().getId(),
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/acceptInviteToGroup", "/acceptInviteToGroup@tstbtstst_bot":
                            acceptInviteToGroup(Long.valueOf(update.getMessage().getText().split(" ")[1]),
                                    Long.valueOf(update.getMessage().getText().split(" ")[2]));
                            break;
                        case "/refuseInviteToGroup", "/refuseInviteToGroup@tstbtstst_bot":
                            refuseInviteToGroup(Long.valueOf(update.getMessage().getText().split(" ")[1]),
                                    Long.valueOf(update.getMessage().getText().split(" ")[2]));
                            break;
                        case "/excludeFromGroup", "/excludeFromGroup@tstbtstst_bot":
                            excludeFromGroup(Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            showHeroGroupList(update.getMessage().getFrom().getId());
                            break;
                        case "/travelTo", "/travelTo@tstbtstst_bot":
                            if (update.getMessage().getText().split(" ").length == 1) {
                                showTravelTo(update.getMessage().getFrom().getId());
                            } else {
                                switch (update.getMessage().getText().split(" ")[1]) {
                                    case "dungeon":
                                        showUnderConstruct(update.getMessage().getFrom().getId(),
                                                new Pair<String, String>("Назад", "/travelTo"));
                                        break;
                                    case "town":
                                        showUnderConstruct(update.getMessage().getFrom().getId(),
                                                new Pair<String, String>("Назад", "/travelTo"));
                                        break;
                                }
                            }
                            break;
                        case "/changeHeroName", "/changeHeroName@tstbtstst_bot":
                            user.setStep(1);
                            user_state.save(user);
                            changeHeroName(update.getMessage().getFrom().getId());
                            break;
                        case "/heroEquipment", "/heroEquipment@tstbtstst_bot":
                            showHeroEquipment(update.getMessage().getFrom().getId());
                            break;
                        case "/changeEquipment", "/changeEquipment@tstbtstst_bot":
                            showChangeEquipment(update.getMessage().getFrom().getId(),
                                    Integer.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/changeEquipmentTo", "/changeEquipmentTo@tstbtstst_bot":
                            сhangeEquipmentTo(update.getMessage().getFrom().getId(),
                                    Integer.valueOf(update.getMessage().getText().split(" ")[1]),
                                    Long.valueOf(update.getMessage().getText().split(" ")[2]));
                            showHeroEquipment(update.getMessage().getFrom().getId());
                            break;
                        case "/addSkill", "/addSkill@tstbtstst_bot":
                            SkillSQL skill = new SkillSQL(Long.valueOf(1), "фаерболл", "Герой бросает огненный шар",
                                    "enemy", "health -10",
                                    new String[] {
                                            "<b>%s</b> выкрикнул \"Получи, фашист, гранату!\" и использовал <b><i>%s</i></b>",
                                            "\"Лови маслину\" - крикнул <b>%s</b>, используя <b><i>%s</i></b>" });
                            skill_table.save(skill);
                            break;
                        case "/showSkill", "/showSkill@tstbtstst_bot":
                            SkillSQL skilll = skill_table.findById(Long.valueOf(1)).get();
                            sendMessage(update.getMessage().getFrom().getId(), skilll.getSkillPhrases()[0]);
                            sendMessage(update.getMessage().getFrom().getId(), skilll.getSkillPhrases()[1]);
                            break;
                        case "/createUserBattle", "/createUserBattle@tstbtstst_bot":
                            createUserBattle(update.getMessage().getFrom().getId(),
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/createItems":
                            List<ItemSQL> list = new ArrayList<>();
                            list.add(new ItemSQL((long) 0, "ничегошеньки", "all", 0));
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
                if (heroName.length() > 255) {
                    sendMessage(message.getFrom().getId(), "Имя героя не должно превышать 255 символов!");
                    createHero(message, (byte) 2);
                } else {
                    registerHero(message.getFrom().getId(), heroName);
                }
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

    private void createUserBattle(Long userId, Long enemyUserId) {
        battle_table.save(new BattleSQL("user", new Long[] { userId }, new Long[] { enemyUserId }));
        sendMenuMessage(userId,
                "Битва с <b>%s (@%s)</b> началась!".formatted(user_hero.findById(enemyUserId).get().getHeroName(),
                        user_table.findById(enemyUserId).get().getUserName()));
        sendMenuMessage(enemyUserId,
                "Битва с <b>%s</b> началась!".formatted(user_hero.findById(userId).get().getHeroName(),
                        user_table.findById(userId).get().getUserName()));
        UserState user = user_state.findById(userId).get();
        user.setProcess("/battle");
        UserState enemy = user_state.findById(enemyUserId).get();
        enemy.setProcess("/battle");
        enemy.setWaitForRequest(true);
        user_state.save(user);
        user_state.save(enemy);
        showBattleMessage(userId);
        showBattleMessage(enemyUserId);
    }

    // TODO прописать метод, который позволит брать battleSQL из таблицы,
    // TODO сделать его универсальным (и для арены, и для подземелья)
    private void showBattleMessage(Long userId) {

    }

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
            editMenuMessage(userId, "Меню:", list);
        } else {
            sendMenuMessage(userId, "Меню:", list);
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

        editMenuMessage(userId, "Ваш профиль:" +
                "\n\n<b>Ваше имя:</b> \t" + user.getFirstName() +
                "\n<b>Права администратора:</b> \t" + user.isAdmin() +
                "\n<b>Количество Ваших очков:</b> \t" + user.getPoints() +
                "\n<b>Количество активных задач:</b> \t"
                + getIdsFromString(user.getActiveTasks(), ";").size(), list);

        UserState userS = user_state.findById(userId).get();
        userS.setLastUserMessage("/profile");
        user_state.save(userS);
    }

    private void showTravelTo(long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>(
                "Подземелье" + EmojiParser.parseToUnicode(":crossed_swords:"), "/travelTo dungeon"));
        list.get(1).add(
                new Pair<String, String>("Город" + EmojiParser.parseToUnicode(":european_castle:"), "/travelTo town"));
        list.get(2).add(new Pair<String, String>("Назад", "/hero"));
        editMenuMessage(userId, "Куда вы желаете отправиться?", list);
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
            list.add(new ArrayList<>());
            list.get(0).add(new Pair<String, String>("Информация", "/heroProfile"));
            list.get(0).add(new Pair<String, String>("Экипировка", "/heroEquipment"));
            list.get(1).add(new Pair<String, String>("Инвентарь", "/heroInventory"));
            list.get(1).add(new Pair<String, String>("Способности", "/heroSkills"));
            list.get(2).add(new Pair<String, String>("Группа", "/heroGroup"));
            list.get(2).add(new Pair<String, String>("Отправиться в..." + EmojiParser.parseToUnicode(":runner:"),
                    "/travelTo"));
        }

        list.get(list.size() - 1).add(new Pair<String, String>("Назад", "/menu"));
        editMenuMessage(userId, textToSend, list);

        UserState user = user_state.findById(userId).get();
        user.setLastUserMessage("/hero");
        user_state.save(user);
    }

    // TODO сделать!!
    private void showHeroSkills(Long userId) {

    }

    private void showHeroProfile(Long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Сменить имя", "/changeHeroName"));
        list.get(1).add(new Pair<String, String>("Назад", "/hero"));

        UserHero hero = user_hero.findById(userId).get();
        String textToSend = "Профиль героя:\n\nИмя героя: <b>%s</b>\n\nЗдоровье героя: <b>%s</b>\n\nУровень силы героя: <b>%s</b>\n\nКоличество монет: <b>%d</b>\n\nКоличество алмазов: <b>%d</b>\n\nГруппа героя: <b>%s</b>"
                .formatted(hero.getHeroName(), hero.getHealth(), hero.getForcePower(), hero.getMoney(),
                        hero.getPoints(), hero.getIdGroup() == null ? "не состоит в группе"
                                : hero_groups.findById(hero.getIdGroup()).get().getGroupName());
        editMenuMessage(userId, textToSend, list);
    }

    private void changeHeroName(Long userId) {
        UserState user = user_state.findById(userId).get();
        switch (user.getStep()) {
            case 1:
                sendMessage(userId, "Введите новое имя героя!");
                user.setStep(2);
                user.setProcess("/changeHeroName");
                user.setWaitForRequest(true);
                user_state.save(user);
                break;
            case 2:
                if (user.getLastUserMessage().length() > 255) {
                    sendMessage(userId, "Имя героя не должно превышать 255 символов! Попробуйте ещщё раз!");
                    user.setWaitForRequest(true);
                    user_state.save(user);
                } else {
                    UserHero hero = user_hero.findById(userId).get();
                    hero.setHeroName(user.getLastUserMessage());
                    user_hero.save(hero);
                    user.setProcess(null);
                    user_state.save(user);
                    sendMessage(userId, "Имя героя изменено на <b>%s</b>!".formatted(user.getLastUserMessage()));
                    showHeroProfile(userId);
                }
                break;
        }
    }

    private void showHeroEquipment(Long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(
                new Pair<String, String>("Шлем🎩", "/changeEquipment 0"));
        list.get(0).add(
                new Pair<String, String>("Нагрудник👕", "/changeEquipment 1"));
        list.get(1).add(
                new Pair<String, String>("Поножи👖", "/changeEquipment 2"));
        list.get(1).add(
                new Pair<String, String>("Ботинки👞", "/changeEquipment 3"));
        list.get(2).add(
                new Pair<String, String>("Талисман💍", "/changeEquipment 4"));
        list.get(2).add(
                new Pair<String, String>("Оружие🗡", "/changeEquipment 5"));
        list.get(3).add(new Pair<String, String>("Назад", "/hero"));

        String[] equipment = user_hero.findById(userId).get().getEquipment();
        String textToSend = "Снаряжение героя:\n\n" +
                "🎩: <b>%s</b>\n".formatted(item_table.findById(Long.valueOf(equipment[0])).get().toString())
                + "👕: <b>%s</b>\n".formatted(item_table.findById(Long.valueOf(equipment[1])).get().toString())
                + "👖: <b>%s</b>\n".formatted(item_table.findById(Long.valueOf(equipment[2])).get().toString())
                + "👞: <b>%s</b>\n".formatted(item_table.findById(Long.valueOf(equipment[3])).get().toString())
                + "💍: <b>%s</b>\n".formatted(item_table.findById(Long.valueOf(equipment[4])).get().toString())
                + "🗡: <b>%s</b>".formatted(item_table.findById(Long.valueOf(equipment[5])).get().toString());
        editMenuMessage(userId, textToSend, list);
    }

    private void showChangeEquipment(Long userId, int typeItem) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());

        String type = "";
        String textToSend = "Доступные ";
        switch (typeItem) {
            case 0:
                type = "head";
                textToSend += "шлемы:";
                break;
            case 1:
                type = "chest";
                textToSend += "нагрудники:";
                break;
            case 2:
                type = "legs";
                textToSend += "поножи:";
                break;
            case 3:
                type = "foots";
                textToSend += "ботинки:";
                break;
            case 4:
                type = "talisman";
                textToSend += "талисманы:";
                break;
            case 5:
                type = "weapon";
                textToSend = "Доступное оружие:";
                break;
        }

        UserHero hero = user_hero.findById(userId).get();
        String[] inventory = hero.getInventory().split(";");
        for (String itemId : inventory) {
            if (item_table.findById(Long.valueOf(itemId)).get().getItemType().equals(type)) {
                list.add(new ArrayList<>());
                list.get(list.size() - 2).add(
                        new Pair<String, String>("Надеть " + item_table.findById(Long.valueOf(itemId)).get().toString(),
                                "/changeEquipmentTo " + typeItem + " " + Long.valueOf(itemId)));
            }
        }
        if (!hero.getEquipment()[typeItem].equals("0")) {
            list.add(new ArrayList<>());
            list.get(list.size() - 2).add(
                    new Pair<String, String>("Снять", "/changeEquipmentTo " + typeItem + " " + 0));
        }
        list.get(list.size() - 1).add(
                new Pair<String, String>("Назад",
                        "/heroEquipment"));
        editMenuMessage(userId, textToSend, list);
    }

    private void сhangeEquipmentTo(Long userId, int typeItem, Long itemId) {
        UserHero hero = user_hero.findById(userId).get();
        hero.takeFromInventory(itemId);
        hero.changeEquipment(typeItem, itemId);
        user_hero.save(hero);
    }

    private void showHeroInventory(Long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Назад", "/hero"));

        String textToSend = "Инвентарь:\n\n";
        UserHero hero = user_hero.findById(userId).get();
        List<Long> items = getIdsFromString(hero.getInventory(), ";");
        if (items.size() == 0) {
            textToSend += "Ваш инвентарь пуст!";
        } else {
            for (Long itemId : items) {
                textToSend += item_table.findById(itemId).get().toStringWithType() + "\n";
            }
        }
        editMenuMessage(userId, textToSend, list);
    }

    private void showHeroGroup(Long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        String textToSend;
        if (user_hero.findById(userId).get().getIdGroup() == null) {
            textToSend = "Ваш герой не состоит в группе!\nВоспользуйтесь кнопкой ниже, чтобы создать группы или ожидайте, пока Вашего героя пригласят в уже существующую группу!";

            list.get(0).add(new Pair<String, String>("Создать группу", "/createGroup"));
        } else {
            textToSend = "Ваш герой состоит в группе <b>%s</b>:".formatted(
                    hero_groups.findById(user_hero.findById(userId).get().getIdGroup()).get().getGroupName());
            list.add(new ArrayList<>());
            list.get(0).add(new Pair<String, String>("Состав", "/heroGroupList"));
            if (hero_groups.findById(user_hero.findById(userId).get().getIdGroup()).get().getIdLeader()
                    .equals(userId)) {
                list.get(1).add(new Pair<String, String>("Распустить группу", "/deleteGroup"));
            } else {
                list.get(1).add(new Pair<String, String>("Покинуть группу", "/leaveGroup"));
            }
        }
        list.get(list.size() - 1).add(new Pair<String, String>("Назад", "/hero"));
        editMenuMessage(userId, textToSend, list);
    }

    // TODO поставить проверки на количество символов меньше 255 везде, где ожидаем
    // ответ от пользователя
    private void createGroup(Long userId) {
        UserState user = user_state.findById(userId).get();
        switch (user.getStep()) {
            case 1:
                sendMessage(userId, "Введите название группы!");
                user.setStep(2);
                user.setWaitForRequest(true);
                user_state.save(user);
                break;
            case 2:
                GroupSQL group = new GroupSQL();
                group.setGroupName(user.getLastUserMessage());
                group.setIdLeader(userId);
                group.addUser(userId);
                group = hero_groups.save(group);
                UserHero hero = user_hero.findById(userId).get();
                hero.setIdGroup(group.getIdGroup());
                user_hero.save(hero);
                user.setProcess(null);
                user_state.save(user);
                sendMessage(userId, "Группа <b>%s</b> создана!".formatted(user.getLastUserMessage()));
                showHeroGroup(userId);
                break;
        }
    }

    private void showHeroGroupList(Long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        String textToSend = "Состав группы:\n\n";
        GroupSQL group = hero_groups.findById(user_hero.findById(userId).get().getIdGroup()).get();
        String[] users = group.getIdUsers().split(";");
        if (users[0].equals(String.valueOf(userId))) {
            textToSend += EmojiParser.parseToUnicode(":crown:") + "<b>%s (@%s)</b>".formatted(
                    user_hero.findById(Long.valueOf(users[0])).get().getHeroName(),
                    user_table.findById(Long.valueOf(users[0])).get().getUserName());
        } else {
            textToSend += EmojiParser.parseToUnicode(":crown:") + "%s (@%s)".formatted(
                    user_hero.findById(Long.valueOf(users[0])).get().getHeroName(),
                    user_table.findById(Long.valueOf(users[0])).get().getUserName());
        }
        for (int i = 1; i < users.length; i++) {
            if (users[i].equals(String.valueOf(userId))) {
                textToSend += "\n<b>%s (@%s)</b>".formatted(
                        user_hero.findById(Long.valueOf(users[i])).get().getHeroName(),
                        user_table.findById(Long.valueOf(users[i])).get().getUserName());
            } else {
                textToSend += "\n%s (@%s)".formatted(
                        user_hero.findById(Long.valueOf(users[i])).get().getHeroName(),
                        user_table.findById(Long.valueOf(users[i])).get().getUserName());
            }
        }

        if (users[0].equals(String.valueOf(userId))) {
            for (int i = 1; i < users.length; i++) {
                list.add(new ArrayList<>());
                list.get(i - 1)
                        .add(new Pair<String, String>(
                                "выгнать %s".formatted(user_hero.findById(Long.valueOf(users[i])).get().getHeroName()),
                                "/excludeFromGroup " + users[i]));
            }
        }
        list.get(list.size() - 2).add(new Pair<String, String>("+", "/showInviteToGroup"));
        list.get(list.size() - 1).add(new Pair<String, String>("Назад", "/heroGroup"));
        editMenuMessage(userId, textToSend, list);
    }

    private void showInviteToGroup(Long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        Long groupId = user_hero.findById(userId).get().getIdGroup();
        List<UserSQL> users = user_table.findAllByChatId(user_table.findById(userId).get().getChatId());
        List<UserHero> heroes = new ArrayList<>();
        for (UserSQL user : users) {
            if (!user_hero.findById(user.getUserId()).isEmpty()) {
                heroes.add(user_hero.findById(user.getUserId()).get());
                if (heroes.get(heroes.size() - 1).getIdGroup() == null
                        || !heroes.get(heroes.size() - 1).getIdGroup().equals(groupId)) {
                    list.add(new ArrayList<>());
                    list.get(list.size() - 2)
                            .add(new Pair<String, String>(
                                    "%s (%s)".formatted(heroes.get(heroes.size() - 1).getHeroName(),
                                            user.getUserName()),
                                    "/inviteToGroup " + heroes.get(heroes.size() - 1).getUserId()));

                }
            }
        }
        list.get(list.size() - 1).add(new Pair<String, String>("Назад", "/heroGroupList"));
        editMenuMessage(userId, "Пригласить пользователя:", list);
    }

    private void inviteToGroup(Long inviterId, Long inventedId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Принять", "/acceptInviteToGroup " + inventedId + " "
                + hero_groups.findById(user_hero.findById(inviterId).get().getIdGroup()).get().getIdGroup()));
        list.get(1).add(new Pair<String, String>("Отказаться", "/refuseInviteToGroup " + inventedId + " "
                + hero_groups.findById(user_hero.findById(inviterId).get().getIdGroup()).get().getIdGroup()));

        sendMessageWithInlineButtons(inventedId,
                "Пользователь <b>@%s</b> пригласил Вас в группу <b>%s</b>!\n\nЕсли Вы уже состоите в какой-то группе, то принятие приглашения сменит Вашу группу на предложенную."
                        .formatted(user_table.findById(inviterId).get().getUserName(),
                                hero_groups.findById(user_hero.findById(inviterId).get().getIdGroup()).get()
                                        .getGroupName()),
                list);
    }

    private void acceptInviteToGroup(Long inventedId, Long groupId) {
        if (hero_groups.findById(groupId).isEmpty()) {
            editMessage(inventedId, "Данная группа уже распущена её лидером!");
        } else {
            GroupSQL group = hero_groups.findById(groupId).get();
            if (!hero_groups.findByIdLeader(inventedId).isEmpty()) {
                deleteGroup(inventedId);
            }
            String[] users = group.getIdUsers().split(";");
            for (int i = 0; i < users.length; i++) {
                sendMessage(Long.valueOf(users[i]), "Пользователь <b>@%s</b> присоединился к Вашей группе!"
                        .formatted(user_table.findById(inventedId).get().getUserName()));
            }
            group.addUser(inventedId);
            hero_groups.save(group);
            UserHero hero = user_hero.findById(inventedId).get();
            hero.setIdGroup(groupId);
            user_hero.save(hero);
            editMessage(inventedId, "Вы прняли приглашение в группу <b>%s</b>!"
                    .formatted(hero_groups.findById(groupId).get().getGroupName()));
        }
    }

    private void refuseInviteToGroup(Long inventedId, Long groupId) {
        if (hero_groups.findById(groupId).isEmpty()) {
            editMessage(inventedId, "Данная группа уже распущена её лидером!");
        } else {
            editMessage(inventedId, "Вы отказались от вступления в группу <b>%s</b>!"
                    .formatted(hero_groups.findById(groupId).get().getGroupName()));
        }
    }

    private void excludeFromGroup(Long userId) {
        UserHero hero = user_hero.findById(userId).get();
        GroupSQL group = hero_groups.findById(hero.getIdGroup()).get();
        group.excludeUser(userId);
        hero_groups.save(group);
        hero.setIdGroup(null);
        user_hero.save(hero);
        sendMessage(userId, "Лидер исключил Вас из группы!");
        sendMessage(group.getIdLeader(), "Вы выгнали пользователя <b>@%s</b> из группы!"
                .formatted(user_table.findById(userId).get().getUserName()));
    }

    private void deleteGroup(Long userId) {
        String[] users = hero_groups.findById(user_hero.findById(userId).get().getIdGroup()).get().getIdUsers()
                .split(";");
        GroupSQL group = hero_groups.findById(user_hero.findById(userId).get().getIdGroup()).get();
        for (String user : users) {
            UserHero hero = user_hero.findById(Long.valueOf(user)).get();
            hero.setIdGroup(null);
            user_hero.save(hero);
            sendMessage(Long.valueOf(user), "Группа <b>%s</b> была распущена!".formatted(group.getGroupName()));
        }
        hero_groups.delete(group);
    }

    private void leaveGroup(Long userId) {
        GroupSQL group = hero_groups.findById(user_hero.findById(userId).get().getIdGroup()).get();
        group.excludeUser(userId);
        hero_groups.save(group);
        UserHero hero = user_hero.findById(userId).get();
        hero.setIdGroup(null);
        user_hero.save(hero);
        sendMessage(userId, "Ваш герой покинул группу <b>%s</b>!".formatted(group.getGroupName()));
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

        editMenuMessage(userId, textToSend, list);

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
        editMenuMessage(userId, "Меню администратора:", list);

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

        editMenuMessage(userId, textToSend, list);
        UserState userS = user_state.findById(userId).get();
        userS.setLastUserMessage("/rating");
        user_state.save(userS);
    }

    private void showUnderConstruct(Long userId, Pair<String, String> pair) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.get(0).add(pair);
        sendMessageWithPicture(userId, "Этот раздел пока ещё в разработке!",
                cats[ThreadLocalRandom.current().nextInt(0, 4)], list);
    }

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
        editMenuMessage(userId, "Список пользователей в беседе:", list);

        UserState userS = user_state.findById(userId).get();
        userS.setLastUserMessage("/showUsers");
        user_state.save(userS);
    }

    private void showUserInfo(long chatId, long userId) {
        if (!user_table.findById(chatId).get().isAdmin()) {
            sendMessage(userId, "Вы не обладаете правами администратора!");
            return;
        }
        UserSQL user = user_table.findById(userId).get();

        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        if (chatId != userId) {
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

        editMenuMessage(chatId, "Профиль пользователя <b>%s</b>:".formatted(user.getUserName()) +
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
        if (text == null || text.equals("")) {
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
                                    "Введите название задания и через ; его описание");
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
                                    String[] arr = user.getLastUserMessage().split(";");

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
                                    user_state.save(user);
                                    sendMessage(message.getFrom().getId(), "Создание окончено!");
                                    List<List<Pair<String, String>>> list = new ArrayList<>();
                                    list.add(new ArrayList<>());
                                    list.get(0).add(new Pair<String, String>("Принять", "/task_agree"));
                                    // acceptTask(task.getTaskId(), message.getFrom().getId(), list);// придумать
                                    // как
                                    // кидать в группу

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

    private void editTask(Message message, byte step) {
        UserState user = user_state.findById(message.getFrom().getId()).get();
        if (message.getChat().isUserChat()) {
            if (!user_table.findById(message.getFrom().getId()).isEmpty()) {
                if (user_table.findById(message.getFrom().getId()).get().isAdmin()) {
                    TaskSQL task;
                    switch (step) {
                        case 1:
                            sendMessage(message.getFrom().getId(),
                                    "Присутупим к изменению задания!");

                            editTask(message, (byte) 2);
                            break;
                        case 2:
                            sendMessage(message.getFrom().getId(),
                                    "Введите название задания которое хотите изменить");
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
                                    if (task_table.existsByTaskName(user.getLastUserMessage())) {
                                        task = task_table.findByTaskName(user.getLastUserMessage());
                                        sendMessage(message.getChatId(), "Напишите что вы хотите изменить",
                                                new String[][] { { "Название", "Описание", "Награда" } });
                                        user.setWaitForRequest(true);
                                        user.setStep((byte) 5);
                                        user_state.save(user);
                                    } else {
                                        sendMessage(message.getChatId(),
                                                "Введено неверное название, такого названия не существует!");
                                        editTask(message, (byte) 2);
                                        user.setStep((byte) 2);
                                        user_state.save(user);
                                    }

                                    break;
                                case "Нет", "нет":
                                    editTask(message, (byte) 2);
                                    user.setStep((byte) 2);
                                    user_state.save(user);
                                    break;
                                default:
                                    break;
                            }
                        case 5:
                            switch (message.getText()) {
                                case "Название", "название":
                                    sendMessage(message.getChatId(), "Введите новое название");
                                    user.setWaitForRequest(true);
                                    user.setStep((byte) 51);
                                    user_state.save(user);
                                    break;
                                case "Описание", "описание":
                                    sendMessage(message.getChatId(), "Введите новое описание");
                                    user.setWaitForRequest(true);
                                    user.setStep((byte) 52);
                                    user_state.save(user);
                                    break;
                                case "Награда", "награда":
                                    sendMessage(message.getChatId(), "Введите новую награду");
                                    user.setWaitForRequest(true);
                                    user.setStep((byte) 53);
                                    user_state.save(user);
                                    break;
                                default:
                                    break;
                            }
                            break;
                        //////////// Добавить уведомление о изменении задания в группу
                        case 51:
                            task = task_table.findByTaskName(user.getLastUserMessage());
                            task.setTaskName(message.getText());
                            task_table.save(task);
                            user.setProcess(null);
                            user_state.save(user);
                            sendMessage(message.getChatId(), "Изменение завершено!");
                            break;
                        case 52:
                            task = task_table.findByTaskName(user.getLastUserMessage());
                            task.setTaskDescription(message.getText());
                            task_table.save(task);
                            user.setProcess(null);
                            user_state.save(user);
                            sendMessage(message.getChatId(), "Изменение завершено!");
                            break;
                        case 53:
                            task = task_table.findByTaskName(user.getLastUserMessage());
                            task.setPoints(Integer.parseInt(message.getText()));
                            task_table.save(task);
                            user.setProcess(null);
                            user_state.save(user);
                            sendMessage(message.getChatId(), "Изменение завершено!");
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

    private void deleteTask(Message message, byte step) {
        UserState user = user_state.findById(message.getFrom().getId()).get();
        if (message.getChat().isUserChat()) {
            if (!user_table.findById(message.getFrom().getId()).isEmpty()) {
                if (user_table.findById(message.getFrom().getId()).get().isAdmin()) {
                    TaskSQL task;
                    switch (step) {
                        case 1:
                            sendMessage(message.getFrom().getId(),
                                    "Присутупим к удалению задания!");

                            deleteTask(message, (byte) 2);
                            break;
                        case 2:
                            sendMessage(message.getFrom().getId(),
                                    "Введите название задания которое хотите удалить");
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
                                    if (task_table.existsByTaskName(user.getLastUserMessage())) {
                                        task = task_table.findByTaskName(user.getLastUserMessage());
                                        task_table.delete(task);
                                        user.setProcess(null);
                                        user_state.save(user);
                                        sendMessage(message.getChatId(), "Удаление завершено!");
                                    } else {
                                        sendMessage(message.getChatId(),
                                                "Ошибка в названии, такого задания не существует!");
                                        deleteTask(message, (byte) 2);
                                        user.setStep((byte) 2);
                                        user_state.save(user);
                                    }

                                    break;
                                case "Нет", "нет":
                                    deleteTask(message, (byte) 2);
                                    user.setStep((byte) 2);
                                    user_state.save(user);
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

    private void showCreatorsTasks(Message message) {
        List<TaskSQL> taskList = new ArrayList<>();
        taskList = task_table.findAllByCreatorId(message.getFrom().getId());
        for (int i = 0; i < taskList.size(); i++) {
            sendMessage(message.getChatId(), i + 1 + ". " + taskList.get(i).getTaskName());
            sendMessage(message.getChatId(), "Описание " + taskList.get(i).getTaskDescription());
            sendMessage(message.getChatId(), "--------------------------------------");
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

    private void sendMenuMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableHtml(true);

        try {
            Message msg = execute(message);

            if (!user_state.findById(chatId).isEmpty()) {
                UserState user = user_state.findById(chatId).get();
                user.setIdLastBotMessage(msg.getMessageId());
                user.setIdMenuMessage(msg.getMessageId());
                user_state.save(user);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void sendMenuMessage(long chatId, String textToSend, List<List<Pair<String, String>>> buttons) {
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
                user.setIdMenuMessage(msg.getMessageId());
                user_state.save(user);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void editMenuMessage(long chatId, String newMessage) {
        if (user_state.findById(chatId).get().getIdLastBotMessage() == user_state.findById(chatId).get()
                .getIdMenuMessage()) {
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
        } else {
            sendMenuMessage(chatId, newMessage);
        }
    }

    private void editMenuMessage(long chatId, String newMessage, List<List<Pair<String, String>>> buttons) {
        if (user_state.findById(chatId).get().getIdLastBotMessage() == user_state.findById(chatId).get()
                .getIdMenuMessage()) {
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
        } else {
            sendMenuMessage(chatId, newMessage, buttons);
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
    // TODO переделать таймер в асинхрон
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
                        if (user_hero.findById(message.getFrom().getId()).get().getIdGroup() != null) {
                            if (hero_groups.findByIdLeader(message.getFrom().getId()).isEmpty()) {
                                GroupSQL group = hero_groups.findById(
                                        user_hero.findById(message.getFrom().getId()).get().getIdGroup()).get();
                                group.excludeUser(message.getFrom().getId());
                                hero_groups.save(group);
                            } else {
                                deleteGroup(message.getFrom().getId());
                            }
                        }
                        user_hero.deleteById(message.getFrom().getId());
                        sendMessage(message.getFrom().getId(), "Ваш герой успешно удалён!");
                        cancel(message.getFrom().getId());
                        break;
                    case "Нет", "нет":
                        cancel(message.getFrom().getId());
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
                        if (user_hero.findById(message.getFrom().getId()).get().getIdGroup() != null) {
                            if (hero_groups
                                    .findByIdLeader(user_hero.findById(message.getFrom().getId()).get().getIdGroup())
                                    .isEmpty()) {
                                GroupSQL group = hero_groups.findByIdLeader(
                                        user_hero.findById(message.getFrom().getId()).get().getIdGroup()).get();
                                group.excludeUser(message.getFrom().getId());
                                hero_groups.save(group);
                            } else {
                                deleteGroup(message.getFrom().getId());
                            }
                        }
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