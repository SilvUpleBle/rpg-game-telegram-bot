
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
import io.project.TestBot.model.PhraseSQL;
import io.project.TestBot.model.Phrase_table;
import io.project.TestBot.model.SkillSQL;
import io.project.TestBot.model.Skill_table;
import io.project.TestBot.model.ShopSQL;
import io.project.TestBot.model.Shop_table;
import io.project.TestBot.model.TaskSQL;
import io.project.TestBot.model.Task_table;
import io.project.TestBot.model.UserHero;
import io.project.TestBot.model.User_table;

import java.time.LocalDate;
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
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
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
    @Autowired
    private Shop_table shop_table;
    @Autowired
    private Phrase_table phrase_table;
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
        listOfCommands.add(new BotCommand("/menu", "открыть меню"));
        listOfCommands.add(new BotCommand("/hero", "открыть меню героя"));
        listOfCommands.add(new BotCommand("/create_user", "создать пользователя"));
        listOfCommands.add(new BotCommand("/delete_hero", "удалить героя"));
        listOfCommands.add(new BotCommand("/delete_user", "удалить пользователя и героя"));
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
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            UserState user = user_state.findByUserId(update.getMessage().getFrom().getId());
            if (user.getProcess().equals("/submit_task_by_user")) {
                submitTaskByUser(update.getMessage(), (byte) user.getStep(), Long.parseLong(user.getLastUserMessage()));
            }

        }
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
                if (!user.getProcess().equals("/battle")) {
                    user.setWaitForRequest(false);
                    user_state.save(user);
                }

                switch (user.getProcess()) {
                    case "/create_hero":
                        createHero(update.getMessage(), (byte) user.getStep());
                        break;
                    case "/create_task":
                        createTask(update.getMessage(), (byte) user.getStep());
                        break;
                    case "/edit_task":
                        editTask(update.getMessage(), (byte) user.getStep(),
                                Long.valueOf(user.getLastUserMessage()));

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
                    case "/battle":
                        BattleSQL battle = battle_table.findById(user.getBattleId()).get();
                        switch (messageText) {
                            case "/showBattleMessage":
                                showFirstBattleMessage(user.getUserId());
                                break;
                            case "/showHeroSkillsInBattle":
                                showHeroSkillsInBattle(user.getUserId());
                                break;
                            case "/showSkillInfo":
                                showSkillInfoInBattle(user.getUserId(),
                                        Long.valueOf(update.getMessage().getText().split(" ")[1]));
                                break;
                            case "/useSkill":
                                useSkill(user.getUserId(),
                                        Long.valueOf(update.getMessage().getText().split(" ")[1]),
                                        Long.valueOf(update.getMessage().getText().split(" ")[2]));
                                break;
                            case "/useAttack":
                                useAttack(user.getUserId(), Long.valueOf(update.getMessage().getText().split(" ")[1]));
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        break;

                }
            } else {
                if (messageText.equals("/cancel") || user.getProcess() == null
                        || messageText.equals("/cancel@tstbtstst_bot")) {
                    switch (messageText) {
                        case "/start":
                            sendMessage(chatId, messageText);
                            break;
                        case "/start@tstbtstst_bot":
                            break;
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
                        case "/task_agree", "/task_agree@tstbtstst_bot":
                            agreeTask(update.getMessage(), Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/edit_task":
                            user.setProcess("/edit_task");
                            user_state.save(user);
                            editTask(update.getMessage(), (byte) 1,
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/delete_task":
                            deleteTask(update.getMessage(), Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/show_creators_tasks":
                            showCreatorsTasks(update.getMessage().getFrom().getId());
                            break;
                        case "/adminTasks", "/adminTasks@tstbtstst_bot":
                            adminTasks(update.getMessage().getFrom().getId());
                            break;
                        case "/showAdminTask":
                            showAdminTask(update.getMessage().getFrom().getId(),
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]));
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
                        case "/user_tasks", "/user_tasks@tstbtstst_bot":
                            showUserTask(update.getMessage().getFrom().getId(),
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/submit_task_by_user", "/submit_task_by_user@tstbtstst_bot":
                            user.setProcess("/submit_task_by_user");
                            user_state.save(user);
                            submitTaskByUser(update.getMessage(), (byte) 1,
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/submitTaskByAdmin", "/submitTaskByAdmin@tstbtstst_bot":
                            submitTaskByAdmin(update.getMessage(),
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/rejectTask", "/rejectTask@tstbtstst_bot":
                            rejectTask(update.getMessage().getFrom().getId(),
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/administration", "/administration@tstbtstst_bot":
                            showAdministration(update.getMessage().getFrom().getId());
                            break;
                        case "/dropAllPointsQuestion":
                            dropAllPointsQuestion(update.getMessage().getFrom().getId());

                            break;
                        case "/dropAllPoints":

                            dropAllPoints(update.getMessage().getFrom().getId());
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
                                        Integer len = update.getMessage().getText().split(" ").length;
                                        if (len == 2) {
                                            showTown(update.getMessage().getFrom().getId());
                                        } else {
                                            switch (update.getMessage().getText().split(" ")[2]) {
                                                case "shop":
                                                    if (len == 3) {
                                                        showShop(update.getMessage().getFrom().getId());
                                                    } else {
                                                        if (len >= 4) {
                                                            switch (update.getMessage().getText().split(" ")[3]) {
                                                                case "roll":
                                                                    shopRoll(update.getMessage().getFrom().getId());

                                                                    break;
                                                                // case "randomBox":
                                                                // break;
                                                                case "show":
                                                                    if (len == 4) {
                                                                        showProduct(
                                                                                update.getMessage().getFrom().getId());
                                                                    } else {
                                                                        if (len == 5) {
                                                                            showItem(
                                                                                    update.getMessage().getFrom()
                                                                                            .getId(),
                                                                                    update.getMessage().getText()
                                                                                            .split(" ")[4],
                                                                                    "buy");
                                                                        } else {
                                                                            buyProduct(
                                                                                    update.getMessage().getFrom()
                                                                                            .getId(),
                                                                                    update.getMessage().getText()
                                                                                            .split(" ")[5]);
                                                                        }

                                                                    }
                                                                    break;
                                                                case "sell":
                                                                    if (len == 4) {
                                                                        showMyItems(
                                                                                update.getMessage().getFrom().getId());
                                                                    } else {
                                                                        if (len == 5) {
                                                                            showItem(
                                                                                    update.getMessage().getFrom()
                                                                                            .getId(),
                                                                                    update.getMessage().getText()
                                                                                            .split(" ")[4],
                                                                                    "sell");

                                                                        } else {
                                                                            sellMyItems(
                                                                                    update.getMessage().getFrom()
                                                                                            .getId(),
                                                                                    update.getMessage().getText()
                                                                                            .split(" ")[5]);
                                                                        }

                                                                    }
                                                                    break;
                                                            }

                                                        }
                                                    }
                                                    break;

                                                case "bar":
                                                    showUnderConstruct(update.getMessage().getFrom().getId(),
                                                            new Pair<String, String>("Назад", "/travelTo town"));
                                                    break;
                                                case "hospital":
                                                    if (update.getMessage().getText().split(" ").length == 3) {
                                                        showHospital(update.getMessage().getFrom().getId());
                                                    } else {
                                                        switch (update.getMessage().getText().split(" ")[3]) {
                                                            case "1":
                                                                hospitalHeal(update.getMessage().getFrom().getId(), 1);
                                                                break;
                                                            case "2":
                                                                hospitalHeal(update.getMessage().getFrom().getId(), 3);
                                                                break;
                                                            case "3":
                                                                hospitalHeal(update.getMessage().getFrom().getId(), 5);
                                                                break;
                                                            case "4":
                                                                hospitalHeal(update.getMessage().getFrom().getId(), 10);
                                                                break;
                                                            default:
                                                                break;
                                                        }
                                                    }
                                                    break;
                                                case "arena":
                                                    showArena(user.getUserId());
                                                    break;
                                                case "library":
                                                    showUnderConstruct(update.getMessage().getFrom().getId(),
                                                            new Pair<String, String>("Назад", "/travelTo town"));
                                                    break;
                                                default:
                                                    break;
                                            }
                                        }

                                        break;
                                    default:
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
                                    "enemy", "damage", 3, 5,
                                    new String[] {
                                            "<b>%s</b> выкрикнул \"Получи, фашист, гранату!\" и использовал <b><i>%s</i></b>",
                                            "\"Лови маслину\" - крикнул <b>%s</b>, используя <b><i>%s</i></b>" });
                            skill_table.save(skill);
                            break;
                        case "/giveSkill", "/giveSkill@tstbtstst_bot":
                            UserHero hero = user_hero.findById(update.getMessage().getFrom().getId()).get();
                            hero.addSkill(Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            user_hero.save(hero);
                            break;
                        case "/showSkill", "/showSkill@tstbtstst_bot":
                            SkillSQL skilll = skill_table.findById(Long.valueOf(1)).get();
                            sendMessage(update.getMessage().getFrom().getId(), skilll.getSkillPhrases()[0]);
                            sendMessage(update.getMessage().getFrom().getId(), skilll.getSkillPhrases()[1]);
                            break;
                        case "/equipSkill", "/equipSkill@tstbtstst_bot":
                            UserHero hero1 = user_hero.findById(update.getMessage().getFrom().getId()).get();
                            hero1.equipSkill(Long.valueOf(update.getMessage().getText().split(" ")[1]),
                                    Integer.valueOf(update.getMessage().getText().split(" ")[2]));
                            user_hero.save(hero1);
                            showChangeSkills(update.getMessage().getFrom().getId());
                            break;
                        case "/unequipSkill", "/unequipSkill@tstbtstst_bot":
                            UserHero hero2 = user_hero.findById(update.getMessage().getFrom().getId()).get();
                            hero2.unequipSkill(Long.valueOf(update.getMessage().getText().split(" ")[1]),
                                    Integer.valueOf(update.getMessage().getText().split(" ")[2]));
                            user_hero.save(hero2);
                            showChangeSkills(update.getMessage().getFrom().getId());
                            break;
                        case "/sendInviteToArena", "/sendInviteToArena@tstbtstst_bot":
                            sendInviteToArena(update.getMessage().getFrom().getId(),
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/acceptInviteToArena", "/acceptInviteToArena@tstbtstst_bot":
                            createUserBattle(update.getMessage().getFrom().getId(),
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/refuseInviteToArena", "/refuseInviteToArena@tstbtstst_bot":

                            break;
                        case "/showChangeSkills", "/showChangeSkills@tstbtstst_bot":
                            showChangeSkills(update.getMessage().getFrom().getId());
                            break;
                        case "/showAvailableSkills", "/showAvailableSkills@tstbtstst_bot":
                            showAvailableSkills(update.getMessage().getFrom().getId(),
                                    Integer.valueOf(update.getMessage().getText().split(" ")[1]));
                            break;
                        case "/showSkillInfo", "/showSkillInfo@tstbtstst_bot":
                            showSkillInfo(update.getMessage().getFrom().getId(),
                                    Long.valueOf(update.getMessage().getText().split(" ")[1]),
                                    Integer.valueOf(update.getMessage().getText().split(" ")[2]));
                            break;
                        case "/createItems":

                            createItems();
                            break;
                        case "/createPhrases":

                            createPhrases();
                            break;

                        default:
                            user.setLastUserMessage(null);
                            user_state.save(user);
                            sendMessage(chatId, "Не понимаю команду!");
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
    // TODO сделать обобщённый баттл и убрать юзеровский
    private void createBattle() {
    }

    private void createUserBattle(Long userId, Long enemyUserId) {
        BattleSQL battle = battle_table.save(new BattleSQL("user", new Long[] { userId }, new Long[] { enemyUserId }));
        BattleSQL battle2 = battle_table.save(new BattleSQL("user", new Long[] { enemyUserId }, new Long[] { userId }));
        sendMenuMessage(userId,
                "Битва с <b>%s (@%s)</b> началась!".formatted(user_hero.findById(enemyUserId).get().getHeroName(),
                        user_table.findById(enemyUserId).get().getUserName()));
        sendMenuMessage(enemyUserId,
                "Битва с <b>%s (@%s)</b> началась!".formatted(user_hero.findById(userId).get().getHeroName(),
                        user_table.findById(userId).get().getUserName()));
        UserState user = user_state.findById(userId).get();
        user.setProcess("/battle");
        user.setBattleId(battle.getBattleId());
        UserState enemy = user_state.findById(enemyUserId).get();
        enemy.setProcess("/battle");
        enemy.setWaitForRequest(true);
        enemy.setBattleId(battle2.getBattleId());
        user_state.save(user);
        user_state.save(enemy);
        showFirstBattleMessage(userId);
        showFirstBattleMessage(enemyUserId);
    }

    // TODO прописать метод, который позволит брать battleSQL из таблицы,
    // TODO сделать его универсальным (и для арены, и для подземелья)
    private void showFirstBattleMessage(Long userId) {
        UserState userState = user_state.findById(userId).get();
        UserSQL user;
        UserHero hero;
        BattleSQL battle = battle_table.findById(userState.getBattleId()).get();
        String textToSend = "Битва:\n\nВаша команда:\n";
        String logToSend = "";
        for (Long id : battle.getFirstSideIds()) {
            user = user_table.findById(id).get();
            hero = user_hero.findById(id).get();

            if (id.equals(userId)) {
                textToSend += "<b>%s (@%s) \u2014 %s❤️</b>\n".formatted(hero.getHeroName(), user.getUserName(),
                        hero.getHealth());
            } else {
                textToSend += "%s (@%s) \u2014 %s❤️\n".formatted(hero.getHeroName(), user.getUserName(),
                        hero.getHealth());
            }
        }

        textToSend += "\n\nКоманда противника:\n";
        for (Long id : battle.getSecondSideIds()) {
            user = user_table.findById(id).get();
            hero = user_hero.findById(id).get();
            textToSend += "%s (@%s) \u2014 %s❤️\n".formatted(hero.getHeroName(), user.getUserName(),
                    hero.getHealth());
        }

        hero = user_hero.findById(userId).get();
        if (userState.getWaitForRequest()) {
            List<List<Pair<String, String>>> list = new ArrayList<>();
            list.add(new ArrayList<>());
            list.add(new ArrayList<>());
            list.get(0).add(new Pair<String, String>("Атаковать", "/useAttack " + battle.getSecondSideIds()[0]));
            list.get(0).add(
                    new Pair<String, String>("Способность", "/showHeroSkillsInBattle"));
            list.get(1).add(new Pair<String, String>("Сдаться", "/giveUp"));
            if (battle.getMessageId() == null) {
                logToSend = "Противник в ожидании Вашего хода...";
                battle.setMessageId(sendMessageWithInlineButtons(userId, textToSend, list).getMessageId());
                battle.setLogId(sendMessage(userId, logToSend).getMessageId());
            } else {
                if (battle.isEnd()) {
                    logToSend += battle.getLogMessage();
                    if (hero.getCurrentHealth() == 0) {
                        logToSend += "\n\nВы проиграли!\nЗдоровье участников восстановлено.";
                    } else {
                        logToSend += "\n\nВы победили!\nЗдоровье участников восстановлено.";
                    }
                } else {
                    if (battle.getLogMessage() != null) {
                        logToSend += battle.getLogMessage() + "\n\nПротивник в ожидании Вашего хода...";
                    } else {
                        logToSend += "\n\nПротивник в ожидании Вашего хода...";
                    }
                }
                editMessage(userId, battle.getMessageId(), textToSend, list);
                editMessage(userId, battle.getLogId(), logToSend);
            }
        } else {
            if (battle.getMessageId() == null) {
                logToSend = "Ожидайте хода противника...";
                battle.setMessageId(sendMessage(userId, textToSend).getMessageId());
                battle.setLogId(sendMessage(userId, logToSend).getMessageId());
            } else {
                if (battle.isEnd()) {
                    logToSend += battle.getLogMessage();
                    if (hero.getCurrentHealth() == 0) {
                        logToSend += "\n\nВы проиграли!\nЗдоровье участников восстановлено.";
                    } else {
                        logToSend += "\n\nВы победили!\nЗдоровье участников восстановлено.";
                    }
                } else {
                    if (battle.getLogMessage() != null) {
                        logToSend += battle.getLogMessage() + "\n\nОжидайте хода противника...";
                    } else {
                        logToSend += "\n\nОжидайте хода противника...";
                    }
                }
                editMessage(userId, battle.getMessageId(), textToSend);
                editMessage(userId, battle.getLogId(), logToSend);
            }
        }
        battle_table.save(battle);
    }

    private void showHeroSkillsInBattle(Long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        BattleSQL battle = battle_table.findById(user_state.findById(userId).get().getBattleId()).get();
        UserHero hero = user_hero.findById(userId).get();
        for (Long skillId : hero.getEquipedSkills()) {
            if (skillId != null) {
                list.add(new ArrayList<>());
                list.get(list.size() - 2)
                        .add(new Pair<String, String>(skill_table.findById(skillId).get().getSkillName(),
                                "/showSkillInfo " + skillId));
            }
        }
        list.get(list.size() - 1).add(new Pair<String, String>("Назад",
                "/showBattleMessage"));
        editMessage(userId, battle.getMessageId(), "Доступные способности:", list);
    }

    private void useAttack(Long userId, Long enemyId) {
        UserHero hero = user_hero.findById(userId).get();
        BattleSQL heroBattle = battle_table.findById(user_state.findById(userId).get().getBattleId()).get();
        BattleSQL enemyBattle = battle_table.findById(user_state.findById(enemyId).get().getBattleId()).get();
        UserHero enemy = user_hero.findById(enemyId).get();
        int attack = ThreadLocalRandom.current().nextInt(hero.getMinAttack(), hero.getMaxAttack() + 1);
        int impact = attack - enemy.getArmor() < 0 ? 0 : attack - enemy.getArmor();
        enemy.setCurrentHealth(Integer.valueOf(enemy.getCurrentHealth()) - impact);
        String textToSend = "<b>%s</b> нанёс <b>%d</b> урона <b>%s</b>, используя <b>%s</b>!\n(%d🗡 - %d🛡)"
                .formatted(hero.getHeroName(), impact, enemy.getHeroName(),
                        hero.getEquipment()[5].equals("0") ? "кулаки"
                                : item_table.findById(Long.valueOf(hero.getEquipment()[5])).get().toString(),
                        attack, enemy.getArmor());
        heroBattle.setLogMessage(textToSend);
        enemyBattle.setLogMessage(textToSend);
        if (enemy.getCurrentHealth() > 0) {
            user_hero.save(enemy);
            UserState user = user_state.findById(userId).get();
            user.setWaitForRequest(false);
            UserState enemyUser = user_state.findById(enemyId).get();
            enemyUser.setWaitForRequest(true);
            user_state.save(user);
            user_state.save(enemyUser);
            battle_table.save(heroBattle);
            battle_table.save(enemyBattle);
        } else {
            heroBattle.setIsEnd(true);
            enemyBattle.setIsEnd(true);
            battle_table.save(heroBattle);
            battle_table.save(enemyBattle);
            enemy.setCurrentHealth(0);
            user_hero.save(enemy);
            hero.setCurrentHealth(hero.getMaxHealth());
            enemy.setCurrentHealth(enemy.getMaxHealth());
            cancel(userId);
            cancel(enemyId);
        }
        showFirstBattleMessage(userId);
        showFirstBattleMessage(enemyId);
        user_hero.save(hero);
        user_hero.save(enemy);
    }

    private void showSkillInfoInBattle(Long userId, Long skillId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());

        SkillSQL skill = skill_table.findById(skillId).get();
        BattleSQL battle = battle_table.findById(user_state.findById(userId).get().getBattleId()).get();
        String textToSend = "Информация о способности:\n\nНазвание: <b>%s</b>\nТип: <b>%s</b>\nЦель: <b>%s</b>"
                .formatted(skill.getSkillName(), skill.getSkillEffect(), skill.getSkillTarget());
        textToSend += skill.getSkillEffect().equals("damage")
                ? "\nУрон: <b>%s-%s</b>🗡".formatted(skill.getMinValue(), skill.getMaxValue())
                : "\nЛечение: <b>%s-%s</b>💊".formatted(skill.getMinValue(), skill.getMaxValue());
        if (skill.getSkillTarget().equals("enemy")) {
            for (Long enemyId : battle.getSecondSideIds()) {
                list.add(new ArrayList<>());
                list.get(list.size() - 1)
                        .add(new Pair<String, String>("🔴" + user_hero.findById(enemyId).get().getHeroName(),
                                "/useSkill " + skillId + " " + enemyId));
            }
        }
        if (skill.getSkillTarget().equals("enemy")) {
            for (Long teammateId : battle.getFirstSideIds()) {
                list.add(new ArrayList<>());
                list.get(list.size() - 1)
                        .add(new Pair<String, String>("🟢" + user_hero.findById(teammateId).get().getHeroName(),
                                "/useSkill " + skillId + " " + teammateId));
            }
        } else {
            list.add(new ArrayList<>());
            list.get(list.size() - 1)
                    .add(new Pair<String, String>("Использовать", "/useSkill " + skillId + " " + userId));
        }

        list.get(list.size() - 1).add(new Pair<String, String>("Назад",
                "/showHeroSkillsInBattle"));

        if (battle.getMessageId() == null) {
            battle.setMessageId(sendMessageWithInlineButtons(userId, textToSend, list).getMessageId());
        } else {
            editMessage(userId, battle.getMessageId(), textToSend, list);
        }
    }

    private void useSkill(Long userId, Long skillId, Long enemyId) {
        SkillSQL skill = skill_table.findById(skillId).get();
        UserHero hero = user_hero.findById(userId).get();
        UserHero enemy = user_hero.findById(enemyId).get();
        BattleSQL battle = battle_table.findById(user_state.findById(userId).get().getBattleId()).get();
        BattleSQL enemyBattle = battle_table
                .findById(user_state.findById(battle.getSecondSideIds()[0]).get().getBattleId()).get();
        int attack;
        int impact;
        String textToSend = "";

        switch (skill.getSkillTarget()) {
            case "self":
                attack = ThreadLocalRandom.current().nextInt(skill.getMinValue(), skill.getMaxValue() + 1);
                hero.setCurrentHealth(hero.getCurrentHealth() + attack);
                user_hero.save(hero);
                textToSend = "<b>%s</b> восстановил себе <b>%s</b>💊 здоровья, используя <b>%s</b>!"
                        .formatted(hero.getHeroName(), attack, skill.getSkillName());
                break;
            case "teammate":
                attack = ThreadLocalRandom.current().nextInt(skill.getMinValue(), skill.getMaxValue() + 1);
                enemy.setCurrentHealth(enemy.getCurrentHealth() + attack);
                user_hero.save(enemy);
                textToSend = "<b>%s</b> восстановил <b>%s</b> <b>%s</b>💊 здоровья, используя <b>%s</b>!"
                        .formatted(hero.getHeroName(), enemy.getHeroName(), attack, skill.getSkillName());
                break;
            case "teammates":
                for (Long id : battle.getFirstSideIds()) {
                    enemy = user_hero.findById(id).get();
                    attack = ThreadLocalRandom.current().nextInt(skill.getMinValue(), skill.getMaxValue() + 1);
                    enemy.setCurrentHealth(enemy.getCurrentHealth() + attack);
                    user_hero.save(enemy);
                    textToSend += "<b>%s</b> восстановил <b>%s</b> <b>%s</b>💊 здоровья, используя <b>%s</b>!"
                            .formatted(hero.getHeroName(), enemy.getHeroName(), attack, skill.getSkillName());
                }
                break;
            case "enemy":
                attack = ThreadLocalRandom.current().nextInt(skill.getMinValue(), skill.getMaxValue() + 1);
                impact = attack - enemy.getArmor() < 0 ? 0 : attack - enemy.getArmor();
                enemy.setCurrentHealth(enemy.getCurrentHealth() - impact);
                user_hero.save(enemy);
                textToSend += "<b>%s</b> нанёс <b>%d</b> урона <b>%s</b>, используя <b>%s</b>!\n(%d🗡 - %d🛡)"
                        .formatted(hero.getHeroName(), impact, enemy.getHeroName(),
                                skill.getSkillName(), attack, enemy.getArmor());
                break;
            case "enemys":
                for (Long id : battle.getFirstSideIds()) {
                    enemy = user_hero.findById(id).get();
                    attack = ThreadLocalRandom.current().nextInt(skill.getMinValue(), skill.getMaxValue() + 1);
                    impact = attack - enemy.getArmor() < 0 ? 0 : attack - enemy.getArmor();
                    enemy.setCurrentHealth(enemy.getCurrentHealth() - impact);
                    user_hero.save(enemy);
                    textToSend += "<b>%s</b> нанёс <b>%d</b> урона <b>%s</b>, используя <b>%s</b>!\n(%d🗡 - %d🛡)"
                            .formatted(hero.getHeroName(), impact, enemy.getHeroName(),
                                    skill.getSkillName(), attack, enemy.getArmor());
                }
                break;

            default:
                break;
        }

        battle.setLogMessage(textToSend);
        enemyBattle.setLogMessage(textToSend);
        battle_table.save(battle);
        battle_table.save(enemyBattle);
        checkBattleToEnd(battle);

        showFirstBattleMessage(userId);
        showFirstBattleMessage(enemyId);
    }

    private void checkBattleToEnd(BattleSQL battle) {
        BattleSQL enemyBattle = battle_table
                .findById(user_state.findById(battle.getSecondSideIds()[0]).get().getBattleId()).get();
        boolean isEnd = true;
        UserState hero;

        for (Long id : enemyBattle.getSecondSideIds()) {
            hero = user_state.findById(id).get();
            hero.setWaitForRequest(true);
            user_state.save(hero);
            if (user_hero.findById(id).get().getCurrentHealth() > 0) {
                isEnd = false;
            }
        }
        for (Long id : enemyBattle.getFirstSideIds()) {
            hero = user_state.findById(id).get();
            hero.setWaitForRequest(false);
            user_state.save(hero);
            if (user_hero.findById(id).get().getCurrentHealth() > 0) {
                isEnd = false;
            }
        }

        battle.setIsEnd(isEnd);
        enemyBattle.setIsEnd(isEnd);
        battle_table.save(battle);
        battle_table.save(enemyBattle);
    }

    // TODO сделать универсальную проверку
    private void checkHeroesState(BattleSQL battle) {

    }

    private void showMenu(long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Профиль🪪", "/profile"));
        list.get(0).add(new Pair<String, String>("Герой🧍🏻", "/hero"));
        list.get(1).add(new Pair<String, String>("Задачи🔖", "/tasks"));
        list.get(1).add(new Pair<String, String>("Рейтинг🏅", "/rating"));
        if (user_table.findById(userId).get().isAdmin()) {
            list.add(new ArrayList<>());
            list.get(2).add(new Pair<String, String>("Администрирование🪬", "/administration"));
        }

        /*
         * if (user_state.findById(userId).get().getLastUserMessage() != null
         * && (user_state.findById(userId).get().getLastUserMessage().equals("/profile")
         * || user_state.findById(userId).get().getLastUserMessage().equals("/hero")
         * || user_state.findById(userId).get().getLastUserMessage().equals("/tasks")
         * || user_state.findById(userId).get().getLastUserMessage().equals(
         * "/administration")
         * || user_state.findById(userId).get().getLastUserMessage().equals("/rating")))
         * {
         * } else {
         * sendMenuMessage(userId, "Меню:", list);
         * }
         */

        editMenuMessage(userId, "Меню:", list);
        // UserState user = user_state.findById(userId).get();
        // user.setLastUserMessage("/menu");
        // user_state.save(user);
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

    private void showTown(long userId) {
        if (phrase_table.count() == 0) {
            createPhrases();
        }
        List<PhraseSQL> phrases = phrase_table.findByType("town");
        Integer rnd = ThreadLocalRandom.current().nextInt(0, phrases.size());
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Таверна🍻", "/travelTo town bar"));
        list.get(1).add(
                new Pair<String, String>("Лавка торговца🏪", "/travelTo town shop"));
        list.get(2).add(
                new Pair<String, String>("Библиотека 📚",
                        "/travelTo town library"));
        list.get(3).add(
                new Pair<String, String>("Лавка целителя🏥", "/travelTo town hospital"));
        list.get(4).add(new Pair<String, String>("Арена⚔️", "/travelTo town arena"));
        list.get(5).add(new Pair<String, String>("Назад", "/travelTo"));
        editMenuMessage(userId,
                phrases.get(rnd).getText() + "\n\nВ городе можно зайти в интересующее вас место",
                list);
    }

    private void showShop(long userId) {
        if (phrase_table.count() == 0) {
            createPhrases();
        }
        List<PhraseSQL> phrases = phrase_table.findByType("shop");
        Integer rnd = ThreadLocalRandom.current().nextInt(0, phrases.size());
        List<List<Pair<String, String>>> list = new ArrayList<>();

        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>(
                "Поменять ассортимент в продаже за 10 монет 🎰", "/travelTo town shop roll"));
        list.get(1).add(new Pair<String, String>(
                "Показать товары 🧳", "/travelTo town shop show"));
        list.get(2).add(new Pair<String, String>(
                "Продать свои вещи 💰", "/travelTo town shop sell"));
        list.get(3).add(new Pair<String, String>("Назад", "/travelTo town"));

        editMenuMessage(userId,
                phrases.get(rnd).getText() + "\n\nЛавка торговца, тут можно купить новые и продать старые вещи!",
                list);
    }

    private void showProduct(long userId) {
        UserHero hero = user_hero.findByUserId(userId).get();
        if (shop_table.findById(userId).isEmpty()) {
            shopGenerator(userId);
        }
        ShopSQL shop = shop_table.findByShopId(userId);
        ItemSQL item = new ItemSQL();
        List<List<Pair<String, String>>> list = new ArrayList<>();
        int k = 0;
        for (int i = 0; i < 7; i++) {// 7 - категорий к продаже
            list.add(new ArrayList<>());
            for (int j = 0; j < shop.getItemId().length / 7; j++) {
                item = item_table.findByItemId(Long.parseLong(shop.getItemId()[k]));
                k++;
                list.get(i).add(new Pair<String, String>(
                        item.toStringWithType(),
                        "/travelTo town shop show " + item.getItemId()));
            }
        }

        list.add(new ArrayList<>());
        list.get(7).add(new Pair<String, String>("Назад", "/travelTo town shop"));
        editMenuMessage(userId,
                "Тут можно купить, все нужное для выживания \n Кошелек: " + hero.getMoney()
                        + " злотых",
                list);
    }

    private void showItem(long userId, String itemId, String type) {// type is for bying or sell
        UserHero hero = user_hero.findByUserId(userId).get();
        ItemSQL item = item_table.findByItemId(Long.parseLong(itemId));
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        if (type.equals("buy")) {
            list.get(0).add(new Pair<String, String>("Купить", "/travelTo town shop show buy " + itemId));
            list.get(1).add(new Pair<String, String>("Назад", "/travelTo town shop show"));
            editMenuMessage(userId,
                    "У вас " + hero.getMoney() + "🟡\nВы хотите купить " + item.toStringWithType() + " за "
                            + (5 + item.getItemLevel() * 10) + "🟡",
                    list);
        } else {
            if (type.equals("sell")) {
                list.get(0).add(new Pair<String, String>("Продать", "/travelTo town shop sell sell " + itemId));
                list.get(1).add(new Pair<String, String>("Назад", "/travelTo town shop sell"));
                editMenuMessage(userId,
                        "У вас " + hero.getMoney() + "🟡\nВы хотите купить " + item.toStringWithType() + " за "
                                + (5 + item.getItemLevel() * 10) + "🟡",
                        list);
            }
        }

    }

    private void buyProduct(long userId, String itemIdStr) {
        if (phrase_table.count() == 0) {
            createPhrases();
        }
        List<PhraseSQL> phrases = phrase_table.findByType("shop_after_buying");
        Integer rnd = ThreadLocalRandom.current().nextInt(0, phrases.size());

        long itemId = Long.parseLong(itemIdStr);
        UserHero hero = user_hero.findByUserId(userId).get();
        ItemSQL item = item_table.findByItemId(itemId);
        Integer price = 5 + item.getItemLevel() * 10;
        if (hero.getMoney() >= price) {
            hero.setMoney(hero.getMoney() - price);
            hero.addToInventory(itemId);
            user_hero.save(hero);
            showShop(userId);
            List<List<Pair<String, String>>> list = new ArrayList<>();
            list.add(new ArrayList<>());
            list.get(0).add(new Pair<String, String>("Назад", "/travelTo town shop show"));
            editMenuMessage(userId,
                    phrases.get(rnd).getText() + "\n\nПоздравляем, вы приобрели: " + item.toStringWithType() + " за "
                            + price + " злотых",
                    list);
        } else {
            List<List<Pair<String, String>>> list = new ArrayList<>();
            list.add(new ArrayList<>());
            list.get(0).add(new Pair<String, String>("Назад", "/travelTo town shop"));
            editMenuMessage(userId,
                    "Товар стоит " + price + " слишком бедны (˚ ˃̣̣̥⌓˂̣̣̥ )",
                    list);
        }
    }

    private void shopGenerator(long userId) {
        if (shop_table.findByShopId(userId) != null) {
            ShopSQL delShop = shop_table.findByShopId(userId);
            shop_table.delete(delShop);
        }

        String[] typeList = { "weapon", "head", "chest", "legs", "foots", "talisman", "heal" };
        List<ItemSQL> itemsList = new ArrayList<>();
        Integer rnd;
        String str = "";
        ShopSQL shop = new ShopSQL();
        shop.setShopId(userId);
        for (String type : typeList) {
            itemsList = item_table.findByItemType(type);
            for (int k = 0; k < 2; k++) {
                rnd = ThreadLocalRandom.current().nextInt(0, itemsList.size());
                str += itemsList.get(rnd).getItemId() + ";";
            }
            shop.setItemId(str.split(";"));
            shop_table.save(shop);
        }
        showShop(userId);

    }

    private void shopRoll(long userId) {
        UserHero hero = user_hero.findByUserId(userId).get();
        Integer price = 10;
        if (hero.getMoney() >= price) {
            hero.setMoney(hero.getMoney() - price);
            user_hero.save(hero);
            shopGenerator(userId);
            List<List<Pair<String, String>>> list = new ArrayList<>();
            list.add(new ArrayList<>());
            list.get(0).add(new Pair<String, String>("Назад", "/travelTo town shop"));
            editMenuMessage(userId,
                    "Ассортимент товаров обновлен!🆕🔥",
                    list);
        } else {
            List<List<Pair<String, String>>> list = new ArrayList<>();
            list.add(new ArrayList<>());
            list.get(0).add(new Pair<String, String>("Назад", "/travelTo town shop"));
            editMenuMessage(userId,
                    "Обновление товара стоит " + price + " слишком бедны (˚ ˃̣̣̥⌓˂̣̣̥ )",
                    list);
        }
    }

    private void showMyItems(long userId) {
        UserHero hero = user_hero.findByUserId(userId).get();
        List<List<Pair<String, String>>> list = new ArrayList<>();
        if (hero.getInventory().equals("") || hero.getInventory().equals(null)) {
            list.add(new ArrayList<>());
            list.get(0).add(new Pair<String, String>(
                    "Назад",
                    "/travelTo town shop"));
            editMenuMessage(userId,
                    "У вас нет вещей!",
                    list);

        } else {
            String[] itemsId = hero.getInventory().split(";");
            int i = 0;
            for (; i < itemsId.length; i++) {
                list.add(new ArrayList<>());
                ItemSQL item = item_table.findByItemId(Long.parseLong(itemsId[i]));
                list.get(i).add(new Pair<String, String>(
                        item.toStringWithType(),
                        "/travelTo town shop sell " + itemsId[i]));
            }
            i++;
            list.add(new ArrayList<>());
            list.get(i - 1).add(new Pair<String, String>(
                    "Назад",
                    "/travelTo town shop"));
            editMenuMessage(userId,
                    "Ваши товары.\n Какие вы хотите продать?",
                    list);
        }

    }

    private void sellMyItems(long userId, String ItemIdStr) {
        UserHero hero = user_hero.findByUserId(userId).get();
        Long itemId = Long.parseLong(ItemIdStr);
        ItemSQL item = item_table.findByItemId(itemId);
        Integer price = 5 + item.getItemLevel() * 8;
        hero.setMoney(hero.getMoney() + price);
        hero.takeFromInventory(itemId);
        user_hero.save(hero);

        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>(
                "Назад",
                "/travelTo town shop sell"));
        editMenuMessage(userId,
                "Вы продали " + item.toStringWithType() + " за " + hero.getMoney() + " злотых.",
                list);

    }

    private void showHospital(long userId) {
        if (phrase_table.count() == 0) {
            createPhrases();
        }
        List<PhraseSQL> phrases = phrase_table.findByType("healer");
        Integer rnd = ThreadLocalRandom.current().nextInt(0, phrases.size());
        UserHero hero = user_hero.findByUserId(userId).get();
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>(
                "Подорожник" + EmojiParser.parseToUnicode(":leaves:") + " 2 злотый", "/travelTo town hospital 1"));
        list.get(0).add(new Pair<String, String>(
                "Перевязка" + EmojiParser.parseToUnicode(":gift_heart:") + " 6 злотый", "/travelTo town hospital 2"));
        list.get(1).add(new Pair<String, String>(
                "Странное зелье" + EmojiParser.parseToUnicode(":coffee:") + " 10 злотый", "/travelTo town hospital 3"));
        list.get(1).add(new Pair<String, String>(
                "Вас излечат" + EmojiParser.parseToUnicode(":woman_health_worker:") + " 20 злотый",
                "/travelTo town hospital 4"));
        list.get(2).add(new Pair<String, String>("Назад", "/travelTo town"));
        editMenuMessage(userId,
                phrases.get(rnd).getText()
                        + "\n\nЛавка целителя\n Можете выбрать способ лечения, который вам по карману\n Ваше здоровье: "
                        + hero.getCurrentHealth() + "/" + hero.getMaxHealth() + "❤️" + "\n Кошелек: " + hero.getMoney()
                        + " злотых💰",
                list);
    }

    private void hospitalHeal(long userId, Integer health) {
        UserHero hero = user_hero.findByUserId(userId).get();
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        String textToSend;
        if (hero.getCurrentHealth() == hero.getMaxHealth()) {

            list.get(0).add(new Pair<String, String>("Назад", "/travelTo town"));
            textToSend = "Сейчас глянем... Так, печень, естественно, увеличена, но это профессиональное...\n В основном — здоров как бык.\n Нечего тут лечить!";

        } else {
            if (hero.getMoney() >= health * 2) {
                textToSend = "Так ну вроде бы здоров)";
                list.get(0).add(new Pair<String, String>("Назад", "/travelTo town hospital"));
                hero.setCurrentHealth(hero.getCurrentHealth() + health);
                hero.setMoney(hero.getMoney() - health * 2);
                user_hero.save(hero);

            } else {
                textToSend = EmojiParser.parseToUnicode(":rage:")
                        + " Иди отсюда бродяга, возвращайся если денег наскребешь!";
                list.get(0).add(new Pair<String, String>("Назад", "/travelTo town"));
            }

        }

        editMenuMessage(userId, textToSend, list);

    }

    private void showArena(Long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        Iterable<UserHero> heroes = user_hero.findAll();
        for (UserHero userHero : heroes) {
            if (userHero.getUserId() != userId) {
                list.add(new ArrayList<>());
                list.get(list.size() - 2)
                        .add(new Pair<String, String>(
                                "%s (@%s)".formatted(userHero.getHeroName(),
                                        user_table.findById(userHero.getUserId()).get().getUserName()),
                                "/sendInviteToArena " + userHero.getUserId()));
            }
        }
        list.get(list.size() - 1).add(new Pair<String, String>("Назад", "/travelTo town"));
        editMenuMessage(userId, "Кого вы хотите вызвать на дуэль?", list);
    }

    private void sendInviteToArena(Long inviterId, Long invitedId) {
        if (user_state.findById(invitedId).get().getProcess() == null) {
            showInviteToArena(inviterId, invitedId);
            editMenuMessage(inviterId, "Приглашение было отправлено!");
        } else {
            sendMessage(inviterId, "Игрок в данный момент занят! Выберите кого-нибудь другого!");
        }
    }

    private void showInviteToArena(Long inviterId, Long invitedId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(list.size() - 1).add(new Pair<String, String>("Принять", "/acceptInviteToArena " + inviterId));
        list.get(list.size() - 1).add(new Pair<String, String>("Отклонить", "/refuseInviteToArena " + inviterId));

        sendMessageWithInlineButtons(invitedId,
                "<b>%s (@%s)</b> вызывает Вас на поединок!".formatted(user_hero.findById(inviterId).get().getHeroName(),
                        user_table.findById(inviterId).get().getUserName()),
                list);
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
            list.get(0).add(new Pair<String, String>("Информация📃", "/heroProfile"));
            list.get(0).add(new Pair<String, String>("Экипировка🧍🏻", "/heroEquipment"));
            list.get(1).add(new Pair<String, String>("Инвентарь🎒", "/heroInventory"));
            list.get(1).add(new Pair<String, String>("Способности⭐️", "/heroSkills"));
            list.get(2).add(new Pair<String, String>("Группа👤", "/heroGroup"));
            list.get(2).add(new Pair<String, String>("Отправиться в...🗺", "/travelTo"));
        }

        list.get(list.size() - 1).add(new Pair<String, String>("Назад", "/menu"));
        editMenuMessage(userId, textToSend, list);

        UserState user = user_state.findById(userId).get();
        user.setLastUserMessage("/hero");
        user_state.save(user);
    }

    private void showHeroSkills(Long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());

        String textToSend = "Cпособности героя:\n\nЭкипированные:\n";
        UserHero hero = user_hero.findById(userId).get();
        boolean hideButton = true;
        SkillSQL skill;
        for (Long skillId : hero.getEquipedSkills()) {
            if (skillId == null) {
                textToSend += "▫️пусто\n";
            } else {
                skill = skill_table.findById(skillId).get();
                textToSend += "▫️<b>%s ".formatted(skill.getSkillName());
                hideButton = false;
                if (skill.getSkillEffect().equals("damage")) {
                    textToSend += "🗡";
                } else {
                    textToSend += "❤️";
                }
                textToSend += "(%s-%s)</b>\n".formatted(skill.getMinValue() * hero.getLevel(),
                        skill.getMaxValue() * hero.getLevel());
            }
        }
        textToSend += "\n\nДоступные:\n";
        if (hero.getSkills().isEmpty()) {
            textToSend += "▪️пусто";
        } else {
            for (Long skillId : hero.getSkills()) {
                textToSend += "▪️<b>%s</b>\n".formatted(skill_table.findById(skillId).get().getSkillName());
                hideButton = false;
            }
        }

        if (!hideButton) {
            list.add(new ArrayList<>());
            list.get(0).add(new Pair<String, String>("Экипировать способности",
                    "/showChangeSkills"));
        }
        list.get(list.size() - 1).add(new Pair<String, String>("Назад",
                "/hero"));
        editMenuMessage(userId, textToSend, list);
    }

    private void showChangeSkills(Long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());

        Long[] equipedSkills = user_hero.findById(userId).get().getEquipedSkills();
        String textToSend, command;
        for (Long id : equipedSkills) {
            list.add(new ArrayList<>());
            if (id != null) {
                textToSend = skill_table.findById(id).get().getSkillName();
                command = "/showSkillInfo %d %d".formatted(id, list.size() - 2);
            } else {
                textToSend = " ";
                command = "/showAvailableSkills " + (list.size() - 2);
            }
            list.get(list.size() - 2).add(new Pair<String, String>(textToSend, command));
        }
        list.get(4).add(new Pair<String, String>("Назад", "/heroSkills"));
        textToSend = "Экипированные слоты:";
        editMenuMessage(userId, textToSend, list);
    }

    private void showAvailableSkills(Long userId, int position) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        List<Long> skills = user_hero.findById(userId).get().getSkills();
        for (Long id : skills) {
            list.add(new ArrayList<>());
            list.get(list.size() - 2).add(
                    new Pair<String, String>(skill_table.findById(id).get().getSkillName(),
                            "/showSkillInfo %d %d".formatted(id, position)));
        }

        list.get(list.size() - 1).add(new Pair<String, String>("Назад", "/showChangeSkills"));
        editMenuMessage(userId, "Выберите способность:", list);
    }

    private void showSkillInfo(Long userId, Long skillId, int position) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());

        boolean isForRemove = false;
        for (Long id : user_hero.findById(userId).get().getEquipedSkills()) {
            if (skillId.equals(id)) {
                isForRemove = true;
                break;
            }
        }

        SkillSQL skill = skill_table.findById(skillId).get();
        String textToSend = "Название: <b>%s</b>\n\nПрименение: <b>%s</b>\nОписание: <b>%s</b>\nЭффект: <b>%s</b>"
                .formatted(skill.getSkillName(), skill.getSkillTarget(), skill.getSkillDescription(),
                        skill.getSkillEffect());
        if (skill.getSkillEffect().equals("damage")) {
            textToSend += "\nУрон: <b>%d*hero.level-%d*hero.level</b>".formatted(skill.getMinValue(),
                    skill.getMaxValue());
        } else {
            textToSend += "\nЛечение: <b>%d*hero.level-%d*hero.level</b>".formatted(skill.getMinValue(),
                    skill.getMaxValue());
        }
        if (isForRemove) {
            list.get(list.size() - 2)
                    .add(new Pair<String, String>("Снять", "/unequipSkill %d %d".formatted(skillId, position)));
        } else {
            list.get(list.size() - 2)
                    .add(new Pair<String, String>("Использовать", "/equipSkill %d %d".formatted(skillId, position)));
        }
        list.get(list.size() - 1).add(new Pair<String, String>("Назад", "/showChangeSkills"));
        editMenuMessage(userId, textToSend, list);
    }

    private void showHeroProfile(Long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Сменить имя", "/changeHeroName"));
        list.get(1).add(new Pair<String, String>("Назад", "/hero"));

        UserHero hero = user_hero.findById(userId).get();
        String textToSend = "Профиль героя:\n\nИмя героя: <b>%s</b>\nЗдоровье героя: <b>%s</b>❤️\nУровень героя: <b>%s</b> (%s/%s опыта)\nЗащита: <b>%s</b>🛡\nАтака: <b>%s-%s</b>🗡\nШанс крита: <b>%s%%</b>🩸\nКоличество монет: <b>%d</b>🟡\nКоличество алмазов: <b>%d</b>💎\nГруппа героя: <b>%s</b>"
                .formatted(hero.getHeroName(), hero.getHealth(), hero.getLevel(), hero.getExperience(),
                        hero.getExperienceForNewLevel(), hero.getArmor(),
                        hero.getMinAttack(), hero.getMaxAttack(), hero.getCriticalChance(), hero.getMoney(),
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
        if (!inventory[0].equals("")) {
            for (String itemId : inventory) {
                if (item_table.findById(Long.valueOf(itemId)).get().getItemType().equals(type)) {
                    list.add(new ArrayList<>());
                    list.get(list.size() - 2).add(
                            new Pair<String, String>(
                                    "Использовать " + item_table.findById(Long.valueOf(itemId)).get().toString(),
                                    "/changeEquipmentTo " + typeItem + " " + Long.valueOf(itemId)));
                }
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
        hero.changeEquipment(typeItem, itemId, item_table);
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

    // TODO можно вернуться и нажать на кнопку снова
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
            editMessage(inventedId, "Вы приняли приглашение в группу <b>%s</b>!"
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

        List<TaskSQL> taskList = new ArrayList<>();
        if (user_table.findById(userId).get().getActiveTasks() != null
                && !user_table.findById(userId).get().getActiveTasks().equals("")) {
            String[] taskId = user_table.findById(userId).get().getAllActiveTasksId();
            for (int i = 0; i < taskId.length; i++) {
                TaskSQL task = task_table.findByTaskId(Long.parseLong(taskId[i]));
                taskList.add(task);
            }
            for (int i = 0; i < taskList.size(); i++) {
                list.add(new ArrayList<>());
                list.get(i).add(new Pair<String, String>(taskList.get(i).getTaskName(),
                        "/user_tasks " + taskList.get(i).getTaskId()));
            }
        }

        list.add(new ArrayList<>());
        list.get(list.size() - 1).add(new Pair<String, String>("Назад", "/menu"));

        editMessage(userId, "Список заданий", list);

        UserState userS = user_state.findById(userId).get();
        userS.setLastUserMessage("/tasks");
        user_state.save(userS);
    }

    private void showUserTask(Long userId, Long taskId) {
        TaskSQL task = task_table.findByTaskId(taskId);
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Сдать задание",
                "/submit_task_by_user " + taskId));
        list.get(1).add(new Pair<String, String>("Назад",
                "/tasks"));
        editMessage(userId, "Задание: " + task.getTaskName() + "\n" + "Описание: " + task.getTaskDescription() + "\n"
                + "Награда: " + task.getPoints() + "\n" + "Дата начала: " + task.getDateStart() + "\n" + "Дата конца: "
                + task.getDateEnd(), list);
    }

    private void submitTaskByUser(Message message, byte step, long taskId) {
        TaskSQL task = task_table.findByTaskId(taskId);
        UserState user = user_state.findByUserId(message.getFrom().getId());

        switch (step) {
            case 1:
                sendMessage(message.getFrom().getId(), "Отправьте фото выполненного задания");
                user.setLastUserMessage(String.valueOf(taskId));
                user.setWaitForRequest(true);
                user.setStep((byte) 2);
                user_state.save(user);
                break;
            case 2:

                List<List<Pair<String, String>>> list = new ArrayList<>();
                list.add(new ArrayList<>());
                list.add(new ArrayList<>());
                list.get(0).add(new Pair<String, String>("Принять задание",
                        "/submitTaskByAdmin " + taskId));
                list.get(1).add(new Pair<String, String>("Отказать",
                        "/rejectTask " + taskId));
                sendMessageWithPhotoAndInlineKB(message, task.getCreatorId(),
                        "Задание" + ": " + task.getTaskName() + "\n" + "Описание" + ": " + task.getTaskDescription(),
                        list);

                sendMessage(message.getFrom().getId(), "Отправлено на обработку");
                task.setWaitForAccept(true);
                user.setProcess(null);

                user_state.save(user);
                task_table.save(task);
                cancel(message.getFrom().getId());
                break;
        }

    }

    private void submitTaskByAdmin(Message message, Long taskId) {
        TaskSQL task = task_table.findByTaskId(taskId);
        String[] usersId = task.getAllRecipientId();
        List<UserSQL> users = new ArrayList<>();
        for (int i = 0; i < usersId.length; i++) {
            UserSQL user = user_table.findByUserId(Long.parseLong(usersId[i]));
            users.add(user);
        }
        for (UserSQL user : users) {
            user.setPoints(user.getPoints() + task.getPoints());
            sendMessage(user.getUserId(),
                    "Задание успешно сдано\n Вы получили: " + task.getPoints() + " очков пользователя!");// TODO
                                                                                                         // Придумать
                                                                                                         // нормальное
            log.info("id задачи = " + taskId); // название
            user.deleteTask(taskId);
            user_table.save(user);
        }

        sendMessage(message.getFrom().getId(), "Задание закрыто!");
        deleteTask(message, taskId);
    }

    private void rejectTask(Long userId, Long taskId) {
        UserState user = user_state.findByUserId(userId);
        user.setIdLastBotMessage(0);
        TaskSQL task = task_table.findByTaskId(taskId);
        task.setWaitForAccept(null);
        task_table.save(task);
        String[] recId = task.getAllRecipientId();
        for (int i = 0; i < recId.length; i++) {
            sendMessage(Long.parseLong(recId[i]), "Задание не принято");
        }
        sendMessage(userId, "Задание не принято");// TODO заменить на edit
    }

    private void dropAllPointsQuestion(long userId) {
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Да✅", "/dropAllPoints"));
        list.get(0).add(new Pair<String, String>("Нет🚫", "/administration"));
        sendMessageWithInlineButtons(userId, "Вы уверены?", list);
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
        list.get(0).add(new Pair<String, String>("Пользователи👥", "/showUsers"));
        list.get(1).add(new Pair<String, String>("Сбросить очки🗑", "/dropAllPointsQuestion"));
        list.get(1).add(new Pair<String, String>("Задачи💼", "/adminTasks"));
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

    private void dropAllPoints(Long adminId) {
        UserSQL admin = user_table.findByUserId(adminId);
        Iterable<UserSQL> users = user_table.findAll();
        for (UserSQL user : users) {
            user.setPoints(0);
            user_table.save(user);
        }
        sendMessage(admin.getChatId(), "Все очки сброшены!");
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

    private void createItems() {
        List<ItemSQL> list = new ArrayList<>();
        // lvl1
        list.add(new ItemSQL((long) 0, "ничегошеньки", "all", 0, 0));
        list.add(new ItemSQL((long) 1, "яблоко", "heal", 1, 5));
        list.add(new ItemSQL((long) 2, "палка-убивалка", "weapon", 1, 5));
        list.add(new ItemSQL((long) 3, "клоунский колпак", "head", 1, 1));
        list.add(new ItemSQL((long) 4, "алмазный нагрудник", "chest", 1, 2));
        list.add(new ItemSQL((long) 5, "штаны из берёзовый коры", "legs", 1, 2));
        list.add(new ItemSQL((long) 6, "сапоги-скороходы", "foots", 1, 1));
        list.add(new ItemSQL((long) 7, "кольцо всевластия", "talisman", 1, 1));
        list.add(new ItemSQL((long) 8, "тетрадь в горошек", "loot", 1, 5));
        // lvl 2
        list.add(new ItemSQL((long) 9, "атаковальня", "weapon", 2, 8));
        list.add(new ItemSQL((long) 10, "ай-кьюдри", "head", 2, 7));
        list.add(new ItemSQL((long) 11, "бронежеле", "chest", 2, 6));
        list.add(new ItemSQL((long) 12, "балласты", "legs", 2, 10));
        list.add(new ItemSQL((long) 13, "олимпийские икры", "foots", 2, 9));
        list.add(new ItemSQL((long) 14, "магнитик на доспех", "talisman", 2, 8));
        list.add(new ItemSQL((long) 15, "бабушкин пирог", "heal", 2, 9));
        list.add(new ItemSQL((long) 16, "кадрило", "loot", 2, 11));
        // lvl 3
        list.add(new ItemSQL((long) 17, "имба-ланс", "weapon", 3, 17));
        list.add(new ItemSQL((long) 18, "моднокль", "head", 3, 10));
        list.add(new ItemSQL((long) 19, "ремень безопасности", "chest", 3, 11));
        list.add(new ItemSQL((long) 20, "малиновые штаны", "legs", 3, 11));
        list.add(new ItemSQL((long) 21, "икронужные мышцы", "foots", 3, 12));
        list.add(new ItemSQL((long) 22, "фартбилет", "talisman", 3, 16));
        list.add(new ItemSQL((long) 23, "хилюля", "heal", 3, 13));
        list.add(new ItemSQL((long) 24, "искра разума", "loot", 3, 14));
        // lvl 4
        list.add(new ItemSQL((long) 25, "железобатон", "weapon", 4, 15));
        list.add(new ItemSQL((long) 26, "оптоволоконный парик", "head", 4, 16));
        list.add(new ItemSQL((long) 27, "драпательное пальто", "chest", 4, 18));
        list.add(new ItemSQL((long) 28, "лосины ассасина", "legs", 4, 17));
        list.add(new ItemSQL((long) 29, "до-колен-вал", "foots", 4, 17));
        list.add(new ItemSQL((long) 30, "ожирелье", "talisman", 4, 22));
        list.add(new ItemSQL((long) 31, "иконка со свечкой", "heal", 4, 5));
        list.add(new ItemSQL((long) 32, "окаменевший пельмень", "loot", 4, 20));

        // imba
        list.add(new ItemSQL((long) 27, "Доспех Императора Человечества", "chest", 26, 150));
        for (ItemSQL item : list) {
            item_table.save(item);
        }
    }

    private void createPhrases() {
        Long i = (long) 0;
        List<PhraseSQL> list = new ArrayList<>();
        // shop on enter
        list.add(new PhraseSQL(i++,
                "Заходя в магазин вы видите надпись:\n В машазин срочно требуются покупатели\n (пол и возраст значения не имеют!)",
                "shop"));
        list.add(new PhraseSQL(i++,
                "Заходя в магазин вы видите надпись:\n У нас опять завелась крыса, бестолочь в человеческом обличие.\n Ловим дружно, мнем шкурку!",
                "shop"));
        list.add(new PhraseSQL(i++,
                "Заходя в магазин вас встречает старичок-торговец со словами\n Ну наконец-то мой ЛУЧШИЙ покупатель!",
                "shop"));
        list.add(new PhraseSQL(i++,
                "У входа в магазин, вы видите разборку, видимо семейной пары, вы решаете быстрее забежать внутрь, пока вам не досталось",
                "shop"));
        // shop after buying
        list.add(new PhraseSQL(i++,
                "Старичок-торговец, хитро улыбаясь говорит - поздравляю с покупкой столь дивной вещицы😈",
                "shop_after_buying"));
        list.add(new PhraseSQL(i++, "Вот это я понимаю, ВЕЩЬ!!!", "shop_after_buying"));
        list.add(new PhraseSQL(i++,
                "В лавку забежал 'странный человек', прокричав \n- ЭТО МОЁЁЁ \n он попытался отобрать вашу покупку, но потерпев неудачу сбежал!",
                "shop_after_buying"));
        list.add(new PhraseSQL(i++,
                "Увидев вашу покупку другой покупатель с напыщенным видом прокомментировал \n - А вот это я брать бы не стал🤭",
                "shop_after_buying"));
        // town
        list.add(new PhraseSQL(i++,
                "Эх, старый добрый город, кто бы ты ни был, куда бы ни шёл, кто-нибудь в этом городе хочет убить тебя.",
                "town"));
        list.add(new PhraseSQL(i++,
                "Добро пожаловать! Добро пожаловать в Сити 17.\n Сами вы его выбрали, или его выбрали за вас — это лучший город из оставшихся.\n Я такого высокого мнения о Сити 17, что решил разместить свое правительство здесь, в Цитадели, столь заботливо предоставленной нашими Покровителями.\n Я горжусь тем, что называю Сити 17 своим домом.\n Итак, собираетесь ли вы остаться здесь, или же вас ждут неизвестные дали, добро пожаловать в Сити 17. Здесь безопасно.",
                "town"));
        list.add(new PhraseSQL(i++,
                "К вам подошел 'странный человек'\n Я не тебе не доверяю.\n Затем он убегает\n Про себя вы думаете - И что это было?",
                "town"));
        list.add(new PhraseSQL(i++,
                "Зайдя в город вы увидели надпись\n Жить у нас интересно, но нервно.\n Поэтому мы веселые, но злые",
                "town"));
        // library

        // healer
        list.add(new PhraseSQL(i++,
                "Как только вы зашли к целителю он улыбнувшись подозвал вас к себе и спросил\n - Что привело вас ко мне, дорогой друг?",
                "healer"));
        list.add(new PhraseSQL(i++,
                "У целителя сегодня уйма народу, постояв пол часа, вы наконец зашли",
                "healer"));
        list.add(new PhraseSQL(i++,
                "Заходя к целителю вы спросили \n - Доктор, сколько стоит вырвать зуб? \n - 20 долларов. \n- 20 долларов за три минуты?\n- Ну, хорошо, я буду тянуть дольше! \n - Нет, пожалуй не сегодня.",
                "healer"));
        list.add(new PhraseSQL(i++,
                "- Доктор, я работаю как лошадь, ем как свинья, устаю как собака - что мне делать? \n- Э, батенька, да вам к ветеринару...",
                "healer"));
        // healer_if_full_hp
        // healer_if_heal
        for (PhraseSQL phrase : list) {
            phrase_table.save(phrase);
        }
    }

    private void giveItem(Long userId, Long itemId) {
        UserHero hero = user_hero.findById(userId).get();
        hero.addToInventory(itemId);
        user_hero.save(hero);

        sendMessage(userId, "Вы получили предмет <b>%s</b>!".formatted(item_table.findById(itemId).get().toString()));
    }

    private void adminTasks(long userId) {
        if (!user_table.findById(userId).get().isAdmin()) {
            sendMessage(userId, "Вы не обладаете правами администратора!");
            return;
        }
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Мои задачи", "/show_creators_tasks"));
        list.get(1).add(new Pair<String, String>("Создать задачу", "/create_task"));
        list.get(2).add(new Pair<String, String>("Назад", "/administration"));
        editMessage(userId, "Меню администратора:", list);

        UserState userS = user_state.findById(userId).get();
        userS.setLastUserMessage("/administration");
        user_state.save(userS);
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
        UserSQL usersql = user_table.findById(message.getFrom().getId()).get();
        if (message.getChat().isUserChat()) {
            if (!user_table.findById(message.getFrom().getId()).isEmpty()) {
                if (user_table.findById(message.getFrom().getId()).get().isAdmin()) {
                    String[] desc;
                    switch (step) {
                        case 1:
                            sendMessage(message.getFrom().getId(),
                                    "Давайте приступим к созданию задания");
                            log.info("Start creating task " + message.getChat().getFirstName());
                            createTask(message, (byte) 2);
                            break;
                        case 2:
                            sendMessage(message.getFrom().getId(),
                                    "Введите название задания!");
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
                                        sendMessage(message.getFrom().getId(),
                                                "Такое название задания уже существует, введите иное название!");
                                        createTask(message, (byte) 2);
                                    } else {
                                        sendMessage(message.getFrom().getId(),
                                                "Введите описание задания!");
                                        user.setWaitForRequest(true);
                                        user.setStep((byte) 5);
                                        user_state.save(user);
                                    }

                                    break;
                                case "Нет", "нет":
                                    createTask(message, (byte) 2);
                                    break;
                            }
                            break;
                        case 5:
                            user.setLastUserMessage(user.getLastUserMessage() + ";" + message.getText());
                            desc = user.getLastUserMessage().split(";");

                            sendMessage(message.getFrom().getId(), "Проверьте, все так? <b><i>%s</i></b>!"
                                    .formatted(desc[1]), new String[][] { { "Да", "Нет" } });
                            user.setWaitForRequest(true);
                            user.setStep((byte) 6);
                            user_state.save(user);
                            break;
                        case 6:
                            sendMessage(message.getFrom().getId(),
                                    "Введите награду за задание!");
                            user.setWaitForRequest(true);
                            user.setStep((byte) 7);
                            user_state.save(user);
                            break;
                        case 7:
                            try {
                                Integer.parseInt(message.getText());
                                user.setLastUserMessage(user.getLastUserMessage() + ";" + message.getText());
                                desc = user.getLastUserMessage().split(";");

                                sendMessage(message.getFrom().getId(), "Проверьте, все так? <b><i>%s</i></b>!"
                                        .formatted(desc[2]), new String[][] { { "Да", "Нет" } });
                                user.setWaitForRequest(true);
                                user.setStep((byte) 8);
                                user_state.save(user);
                            } catch (Exception e) {
                                sendMessage(message.getFrom().getId(), "Вводите целые числа\n Начнем создание заново!");
                                createTask(message, (byte) 2);
                            }

                            break;
                        case 8:
                            sendMessage(message.getFrom().getId(),
                                    "Введите количество человек для этого задания!");
                            user.setWaitForRequest(true);
                            user.setStep((byte) 9);
                            user_state.save(user);
                            break;
                        case 9:
                            try {
                                user.setLastUserMessage(user.getLastUserMessage() + ";" + message.getText());
                                desc = user.getLastUserMessage().split(";");

                                sendMessage(message.getFrom().getId(), "Проверьте, все так? <b><i>%s</i></b>!"
                                        .formatted(desc[3]), new String[][] { { "Да", "Нет" } });
                                user.setWaitForRequest(true);
                                user.setStep((byte) 10);
                                user_state.save(user);
                            } catch (Exception e) {
                                sendMessage(message.getFrom().getId(), "Вводите целые числа\n Начнем создание заново!");
                                createTask(message, (byte) 2);
                            }

                            break;
                        case 10:
                            sendMessage(message.getFrom().getId(),
                                    "Введите дату окончания задания в форме год-месяц-день");
                            user.setWaitForRequest(true);
                            user.setStep((byte) 11);
                            user_state.save(user);
                            break;
                        case 11:
                            user.setLastUserMessage(user.getLastUserMessage() + ";" + message.getText());
                            desc = user.getLastUserMessage().split(";");

                            sendMessage(message.getFrom().getId(), "Проверьте, все так? <b><i>%s</i></b>!"
                                    .formatted(desc[4]), new String[][] { { "Да", "Нет" } });
                            user.setWaitForRequest(true);
                            user.setStep((byte) 12);
                            user_state.save(user);
                            break;
                        case 12:
                            switch (message.getText()) {
                                case "Да", "да":
                                    LocalDate ldt = LocalDate.now();
                                    TaskSQL task = new TaskSQL();
                                    String[] arr = user.getLastUserMessage().split(";");

                                    Long randomInt;
                                    do {
                                        randomInt = ThreadLocalRandom.current().nextLong(0, 10000);
                                    } while (!task_table.findById((long) randomInt).isEmpty());

                                    task.setTaskId(randomInt);
                                    task.setCreatorId(message.getFrom().getId());
                                    task.setTaskDescription(arr[1]);
                                    task.setTaskName(arr[0]);
                                    task.setPoints(Integer.parseInt(arr[2]));
                                    task.setDateStart(ldt.toString());
                                    task.setDateEnd(arr[4]);
                                    task.setCapacity(Integer.parseInt(arr[3]));
                                    task_table.save(task);

                                    user.setProcess(null);
                                    user_state.save(user);
                                    sendMessage(message.getFrom().getId(), "Создание окончено!");
                                    sendMessage(message.getFrom().getId(), "Создание окончено!");
                                    List<List<Pair<String, String>>> list = new ArrayList<>();
                                    list.add(new ArrayList<>());
                                    list.get(0).add(
                                            new Pair<String, String>("Принять", "/task_agree " + task.getTaskId()));

                                    adminTasks(message.getFrom().getId());
                                    sendMessageWithLastMessageId(usersql.getChatId(),
                                            "Задание: " + task.getTaskName() + "\n" + "Описание: "
                                                    + task.getTaskDescription() + "\n"
                                                    + "Награда: " + task.getPoints() + "\n" + "Дата начала: "
                                                    + task.getDateStart() + "\n" + "Дата конца: "
                                                    + task.getDateEnd() + "\n" + "Количество людей взявших задание: 0/"
                                                    + task.getCapacity(),
                                            task.getTaskId(), list);
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
        } else

        {
            sendMessage(message.getChatId(), "Используйте эту команду в личных сообщениях с ботом!");
            user.setProcess(null);
            user_state.save(user);
        }
    }

    private void agreeTask(Message message, Long taskId) {

        TaskSQL task = task_table.findByTaskId(taskId);
        UserSQL user = user_table.findById(message.getFrom().getId()).get();
        String[] arr;
        if (task.getRecipientId() == null) {
            if (task.getCapacity().equals(1)) {
                task.addRecipientId(String.valueOf(message.getFrom().getId()));
                user.addActiveTask(taskId);
                user_table.save(user);
                task_table.save(task);
                editMessage(message.getChatId(), "Задание: " + task.getTaskName() + " полностью разобрано!",
                        taskId);

            } else {
                task.addRecipientId(String.valueOf(message.getFrom().getId()));
                user.addActiveTask(taskId);
                user_table.save(user);
                task_table.save(task);
                arr = task.getRecipientId().split(";");

                List<List<Pair<String, String>>> list = new ArrayList<>();
                list.add(new ArrayList<>());
                list.get(0).add(
                        new Pair<String, String>("Принять", "/task_agree " + task.getTaskId()));
                editMessage(message.getChatId(), "Задание: " + task.getTaskName() + "\n" + "Описание: "
                        + task.getTaskDescription() + "\n"
                        + "Награда: " + task.getPoints() + "\n" + "Дата начала: "
                        + task.getDateStart() + "\n" + "Дата конца: "
                        + task.getDateEnd() + "\n" + "Колличество людей взявших задание: " + arr.length + "/"
                        + task.getCapacity(), list, taskId);

                sendMessage(user.getUserId(), "Вы взяли задание: " + task.getTaskName());
            }

        } else {
            arr = task.getRecipientId().split(";");
            boolean fl = false;
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].equals(String.valueOf(message.getFrom().getId()))) {
                    fl = true;
                    break;
                }
            }
            if (!fl) {
                task.addRecipientId(String.valueOf(message.getFrom().getId()));
                user.addActiveTask(taskId);
                user_table.save(user);
                task_table.save(task);
                List<List<Pair<String, String>>> list = new ArrayList<>();
                list.add(new ArrayList<>());
                list.get(0).add(
                        new Pair<String, String>("Принять", "/task_agree " + task.getTaskId()));
                if (arr.length == task.getCapacity() - 1) {
                    editMessage(message.getChatId(), "Задание: " + task.getTaskName() + " полностью разобрано!",
                            taskId);
                } else {

                    editMessage(message.getChatId(), "Задание: " + task.getTaskName() + "\n" + "Описание: "
                            + task.getTaskDescription() + "\n"
                            + "Награда: " + task.getPoints() + "\n" + "Дата начала: "
                            + task.getDateStart() + "\n" + "Дата конца: "
                            + task.getDateEnd() + "\n" + "Колличество людей взявших задание: " + arr.length + "/"
                            + task.getCapacity(), list, taskId);
                }
                sendMessage(user.getUserId(), "Вы взяли задание: " + task.getTaskName());
            } else {
                sendMessage(user.getUserId(), "Вы уже брали задание: " + task.getTaskName());
            }

        }

    }

    private void showCreatorsTasks(Long userId) {

        List<List<Pair<String, String>>> list = new ArrayList<>();

        List<TaskSQL> taskList = new ArrayList<>();
        taskList = task_table.findAllByCreatorId(userId);
        for (int i = 0; i < taskList.size(); i++) {
            list.add(new ArrayList<>());
            list.get(i).add(new Pair<String, String>(taskList.get(i).getTaskName(),
                    "/showAdminTask " + taskList.get(i).getTaskId()));
        }
        list.add(new ArrayList<>());
        list.get(list.size() - 1).add(new Pair<String, String>("Назад", "/adminTasks"));

        editMessage(userId, "Список заданий", list);
    }

    private void showAdminTask(Long userId, Long taskId) {
        TaskSQL task = task_table.findByTaskId(taskId);
        List<List<Pair<String, String>>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(new Pair<String, String>("Изменить задание",
                "/edit_task " + taskId));
        list.get(1).add(new Pair<String, String>("Удалить задание",
                "/delete_task " + taskId));
        list.get(2).add(new Pair<String, String>("Назад",
                "/show_creators_tasks"));
        editMessage(userId, "Задание: " + task.getTaskName() + "\n" + "Описание: " + task.getTaskDescription() + "\n"
                + "Награда: " + task.getPoints() + "\n" + "Дата начала: " + task.getDateStart() + "\n" + "Дата конца: "
                + task.getDateEnd(), list);
    }

    private void editTask(Message message, byte step, Long taskId) {
        UserState user = user_state.findById(message.getFrom().getId()).get();
        if (message.getChat().isUserChat()) {
            if (!user_table.findById(message.getFrom().getId()).isEmpty()) {
                if (user_table.findById(message.getFrom().getId()).get().isAdmin()) {
                    TaskSQL task;
                    user.setLastUserMessage(String.valueOf(taskId));
                    switch (step) {
                        case 1:
                            task = task_table.findByTaskId(taskId);
                            sendMessage(message.getChatId(), "Напишите что вы хотите изменить",
                                    new String[][] { { "Название", "Описание", "Награда" } });
                            user.setWaitForRequest(true);
                            user.setStep((byte) 2);
                            user_state.save(user);

                            break;
                        case 2:
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
                            task = task_table.findByTaskId(taskId);
                            task.setTaskName(message.getText());
                            task_table.save(task);
                            user.setProcess(null);
                            user_state.save(user);
                            sendMessage(message.getChatId(), "Изменение завершено!");
                            adminTasks(message.getFrom().getId());
                            break;
                        case 52:
                            task = task_table.findByTaskId(taskId);
                            task.setTaskDescription(message.getText());
                            task_table.save(task);
                            user.setProcess(null);
                            user_state.save(user);
                            sendMessage(message.getChatId(), "Изменение завершено!");
                            adminTasks(message.getFrom().getId());
                            break;
                        case 53:
                            try {
                                task = task_table.findByTaskId(taskId);
                                task.setPoints(Integer.parseInt(message.getText()));
                                task_table.save(task);
                                user.setProcess(null);
                                user_state.save(user);
                                sendMessage(message.getChatId(), "Изменение завершено!");
                                adminTasks(message.getFrom().getId());
                            } catch (Exception e) {
                                sendMessage(message.getFrom().getId(), "Вводите целые числа\n Начнем создание заново!");
                                editTask(message, (byte) 1, taskId);
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

    private void deleteTask(Message message, Long taskId) {
        if (message.getChat().isUserChat()) {
            if (!user_table.findById(message.getFrom().getId()).isEmpty()) {
                if (user_table.findById(message.getFrom().getId()).get().isAdmin()) {
                    TaskSQL task;

                    task = task_table.findByTaskId(taskId);
                    task_table.delete(task);
                    editMessage(message.getChatId(), "Задание удалено");
                    adminTasks(message.getFrom().getId());

                } else {
                    sendMessage(message.getChatId(), "Вы не администратор");
                }
            } else {
                sendMessage(message.getFrom().getId(), "Вы не зарегистрированны");
            }
        } else {
            sendMessage(message.getChatId(), "Используйте эту команду в личных сообщениях с ботом!");
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

    private Message sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableHtml(true);
        Message msg = new Message();
        try {

            msg = execute(message);

            if (!user_state.findById(chatId).isEmpty()) {
                UserState user = user_state.findById(chatId).get();
                user.setIdLastBotMessage(msg.getMessageId());
                user_state.save(user);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
        return msg;
    }

    private Message sendMessage(long chatId, String textToSend, String[][] arrStr) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableHtml(true);
        message.setReplyMarkup(createReplyKeyboard(arrStr));
        Message msg = new Message();

        try {
            msg = execute(message);

            if (!user_state.findById(chatId).isEmpty()) {
                UserState user = user_state.findById(chatId).get();
                user.setIdLastBotMessage(msg.getMessageId());
                user_state.save(user);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
        return msg;
    }

    private Message sendMenuMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableHtml(true);
        Message msg = new Message();

        try {
            msg = execute(message);

            if (!user_state.findById(chatId).isEmpty()) {
                UserState user = user_state.findById(chatId).get();
                user.setIdLastBotMessage(msg.getMessageId());
                user.setIdMenuMessage(msg.getMessageId());
                user_state.save(user);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
        return msg;
    }

    private Message sendMenuMessage(long chatId, String textToSend, List<List<Pair<String, String>>> buttons) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableHtml(true);
        message.setReplyMarkup(createInlineKeyboard(buttons));
        Message msg = new Message();

        try {
            msg = execute(message);

            if (!user_state.findById(chatId).isEmpty()) {
                UserState user = user_state.findById(chatId).get();
                user.setIdLastBotMessage(msg.getMessageId());
                user.setIdMenuMessage(msg.getMessageId());
                user_state.save(user);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
        return msg;
    }

    private void sendMenuMessageWithPic(long chatId, String textToSend, List<List<Pair<String, String>>> buttons,
            String imageUrlToSend) {

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

    private void editMenuMessage(long chatId, int messageId, String newMessage) {
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

    private void editMessage(long chatId, int messageId, String newMessage) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(messageId);
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

    private void editMessage(long chatId, int messageId, String newMessage, List<List<Pair<String, String>>> buttons) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(messageId);
        editMessageText.setText(newMessage);
        editMessageText.setReplyMarkup(createInlineKeyboard(buttons));
        editMessageText.enableHtml(true);

        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void editMessage(long chatId, String newMessage, List<List<Pair<String, String>>> buttons, Long taskId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(task_table.findById(taskId).get().getMessageId());
        editMessageText.setText(newMessage);
        editMessageText.setReplyMarkup(createInlineKeyboard(buttons));
        editMessageText.enableHtml(true);

        try {
            execute(editMessageText);

        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void editMessage(long chatId, String newMessage, Long taskId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(task_table.findById(taskId).get().getMessageId());
        editMessageText.setText(newMessage);
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

    private Message sendMessageWithInlineButtons(long chatId, String textToSend,
            List<List<Pair<String, String>>> buttons) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableHtml(true);
        message.setReplyMarkup(createInlineKeyboard(buttons));
        Message msg = new Message();

        try {
            msg = execute(message);

            if (!user_state.findById(chatId).isEmpty()) {
                UserState user = user_state.findById(chatId).get();
                user.setIdLastBotMessage(msg.getMessageId());
                user_state.save(user);
            }
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
        return msg;
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

    private void sendMessageWithLastMessageId(long chatId, String textToSend, Long taskId,
            List<List<Pair<String, String>>> buttons) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableHtml(true);
        message.setReplyMarkup(createInlineKeyboard(buttons));

        try {
            Message msg = execute(message);
            TaskSQL task = task_table.findByTaskId(taskId);
            task.setMessageId(msg.getMessageId());
            task_table.save(task);

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

    private void sendMessageWithPhoto(Message message, long receiverId, String textToSend) {
        List<PhotoSize> photos = message.getPhoto();
        log.info(photos.get(0).getFileId());
        SendPhoto msg = new SendPhoto();

        msg.setChatId(String.valueOf(receiverId));
        msg.setPhoto(new InputFile(photos.get(0).getFileId()));
        msg.setCaption(textToSend);

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageWithPhotoAndInlineKB(Message message, long receiverId, String textToSend,
            List<List<Pair<String, String>>> buttons) {
        List<PhotoSize> photos = message.getPhoto();
        SendPhoto msg = new SendPhoto();
        msg.setChatId(String.valueOf(receiverId));
        msg.setPhoto(new InputFile(photos.get(0).getFileId()));
        msg.setCaption(textToSend);
        msg.setReplyMarkup(createInlineKeyboard(buttons));
        try {
            execute(msg);

        } catch (TelegramApiException e) {
            e.printStackTrace();
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