/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.quests;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.request.Request;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.mc.objects.Location;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.Utils;
import com.wynntils.utils.WebUtils;
import com.wynntils.wynn.event.QuestBookReloadedEvent;
import com.wynntils.wynn.event.TrackedQuestUpdateEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.model.container.ContainerContent;
import com.wynntils.wynn.model.container.ScriptedContainerQuery;
import com.wynntils.wynn.model.scoreboard.ScoreboardHandler;
import com.wynntils.wynn.model.scoreboard.ScoreboardModel;
import com.wynntils.wynn.model.scoreboard.Segment;
import com.wynntils.wynn.utils.ContainerUtils;
import com.wynntils.wynn.utils.InventoryUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class QuestManager extends CoreManager {
    public static final QuestScoreboardHandler SCOREBOARD_HANDLER = new QuestScoreboardHandler();

    private static final int NEXT_PAGE_SLOT = 8;
    private static final int MINI_QUESTS_SLOT = 53;
    private static final Pattern DIALOGUE_HISTORY_PAGE_PATTERN = Pattern.compile("§7Page \\[(\\d+)/(\\d+)\\]");

    private static List<QuestInfo> quests = List.of();
    private static List<QuestInfo> newQuests;
    private static List<QuestInfo> miniQuests = List.of();
    private static List<QuestInfo> newMiniQuests;
    private static List<List<String>> dialogueHistory = List.of();
    private static List<List<String>> newDialogueHistory;
    private static QuestInfo currentQuest = null;

    public static void init() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onWorldStateChanged(WorldStateEvent e) {
        quests = List.of();
        dialogueHistory = List.of();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onWorldStateChangedLast(WorldStateEvent e) {
        // Rescan quest book in background as the last thing we do
        // when entering a world
        if (e.getNewState() == WorldStateManager.State.WORLD) {
            queryQuestBook();
        }
    }

    /**
     * Trigger a rescan of the quest book. When the rescan is done, a QuestBookReloadedEvent will
     * be sent. The available quests are then available using getQuests.
     */
    private static void queryQuestBook() {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder("Quest Book Query")
                .onError(msg -> {
                    WynntilsMod.warn("Problem querying Quest Book: " + msg);
                    McUtils.player()
                            .sendMessage(
                                    new TextComponent("Error updating quest book.").withStyle(ChatFormatting.RED),
                                    null);
                })
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(getQuestBookTitle(1))
                .processContainer(c -> processQuestBookPage(c, 1));

        for (int i = 2; i < 5; i++) {
            final int page = i; // Lambdas need final variables
            queryBuilder
                    .clickOnSlotWithName(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(page))
                    .matchTitle(getQuestBookTitle(page))
                    .processContainer(c -> processQuestBookPage(c, page));
        }

        queryBuilder.build().executeQuery();
    }

    private static void processQuestBookPage(ContainerContent container, int page) {
        // Quests are in the top-left container area
        if (page == 1) {
            // Build new set of quests without disturbing current set
            newQuests = new ArrayList<>();
        }
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int slot = row * 9 + col;

                // Very first slot is chat history
                if (slot == 0) continue;

                ItemStack item = container.items().get(slot);
                QuestInfo questInfo = QuestInfo.parseItem(item, page, false);
                if (questInfo == null) continue;

                newQuests.add(questInfo);
            }
        }

        if (page == 4) {
            // Last page finished
            quests = newQuests;
            WynntilsMod.postEvent(new QuestBookReloadedEvent.QuestsReloaded());
        }
    }

    public static void queryMiniQuests() {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder("Quest Book Mini Quest Query")
                .onError(msg -> {
                    WynntilsMod.warn("Problem querying Quest Book for mini quests: " + msg);
                    McUtils.player()
                            .sendMessage(
                                    new TextComponent("Error updating quest book.").withStyle(ChatFormatting.RED),
                                    null);
                })
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(getQuestBookTitle(1))
                .processContainer(c -> {})
                .clickOnSlot(MINI_QUESTS_SLOT)
                .matchTitle(getMiniQuestBookTitle(1))
                .processContainer(c -> processMiniQuestBookPage(c, 1));

        for (int i = 2; i < 4; i++) {
            final int page = i; // Lambdas need final variables
            queryBuilder
                    .clickOnSlotWithName(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(page))
                    .matchTitle(getMiniQuestBookTitle(page))
                    .processContainer(c -> processMiniQuestBookPage(c, page));
        }

        queryBuilder.build().executeQuery();
    }

    private static void processMiniQuestBookPage(ContainerContent container, int page) {
        // Quests are in the top-left container area
        if (page == 1) {
            // Build new set of quests without disturbing current set
            newMiniQuests = new ArrayList<>();
        }
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int slot = row * 9 + col;

                // Very first slot is chat history
                if (slot == 0) continue;

                ItemStack item = container.items().get(slot);
                QuestInfo questInfo = QuestInfo.parseItem(item, page, true);
                if (questInfo == null) continue;

                newMiniQuests.add(questInfo);
            }
        }

        if (page == 3) {
            // Last page finished
            miniQuests = newMiniQuests;
            WynntilsMod.postEvent(new QuestBookReloadedEvent.MiniQuestsReloaded());
        }
    }

    public static void trackQuest(QuestInfo questInfo) {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder("Quest Book Quest Pin Query")
                .onError(msg -> WynntilsMod.warn("Problem pinning quest in Quest Book: " + msg))
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(getQuestBookTitle(1));

        if (questInfo.isMiniQuest()) {
            queryBuilder.processContainer(c -> {}).clickOnSlot(MINI_QUESTS_SLOT).matchTitle(getMiniQuestBookTitle(1));
        }

        if (questInfo.getPageNumber() > 1) {
            for (int i = 2; i <= questInfo.getPageNumber(); i++) {
                queryBuilder
                        .processContainer(container -> {}) // we ignore this because this is not the correct page
                        .clickOnSlotWithName(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(i))
                        .matchTitle(getQuestBookTitle(i));
            }
        }
        queryBuilder
                .processContainer(c -> findQuestForTracking(c, questInfo))
                .build()
                .executeQuery();
    }

    private static void findQuestForTracking(ContainerContent container, QuestInfo questInfo) {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int slot = row * 9 + col;

                // Very first slot is chat history
                if (slot == 0) continue;

                ItemStack item = container.items().get(slot);

                String questName = QuestInfo.getQuestName(item);
                if (Objects.equals(questName, questInfo.getName())) {
                    ContainerUtils.clickOnSlot(
                            slot, container.containerId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, container.items());
                    return;
                }
            }
        }
    }

    public static void scanDialogueHistory() {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder(
                        "Quest Book Dialogue History Query")
                .onError(msg -> WynntilsMod.warn("Problem getting dialogue history in Quest Book: " + msg))
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(getQuestBookTitle(1))
                .processContainer((c) -> {
                    ItemStack dialogueHistoryItem = c.items().get(0);

                    if (!ComponentUtils.getCoded(dialogueHistoryItem.getHoverName())
                            .equals("§bDialogue History")) return;

                    for (String line : ItemUtils.getLore(dialogueHistoryItem)) {
                        Matcher matcher = DIALOGUE_HISTORY_PAGE_PATTERN.matcher(line);

                        if (matcher.matches()) {
                            int pageCount = Integer.parseInt(matcher.group(2));
                            createActualDialogueHistoryQuery(pageCount);
                            break;
                        }
                    }
                });

        queryBuilder.build().executeQuery();
    }

    private static void createActualDialogueHistoryQuery(int pageCount) {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder(
                        "Quest Book Dialogue History Query 2")
                .onError(msg -> WynntilsMod.warn("Problem getting dialogue history (2) in Quest Book: " + msg))
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(getQuestBookTitle(1))
                .setWaitForMenuReopen(false)
                .processContainer((c) -> {
                    ItemStack dialogueHistoryItem = c.items().get(0);

                    if (!ComponentUtils.getCoded(dialogueHistoryItem.getHoverName())
                            .equals("§bDialogue History")) return;

                    newDialogueHistory = new ArrayList<>();

                    List<String> current = ItemUtils.getLore(dialogueHistoryItem).stream()
                            .dropWhile(String::isBlank)
                            .takeWhile(s -> !s.isBlank())
                            .toList();

                    newDialogueHistory.add(current);
                });

        for (int i = 2; i <= pageCount; i++) {
            int page = i;
            queryBuilder
                    .clickOnSlot(0)
                    .matchTitle(getQuestBookTitle(1))
                    .setWaitForMenuReopen(false)
                    .processContainer((c) -> {
                        ItemStack dialogueHistoryItem = c.items().get(0);

                        if (!ComponentUtils.getCoded(dialogueHistoryItem.getHoverName())
                                .equals("§bDialogue History")) return;

                        List<String> current = ItemUtils.getLore(dialogueHistoryItem).stream()
                                .dropWhile(String::isBlank)
                                .takeWhile(s -> !s.isBlank())
                                .toList();

                        newDialogueHistory.add(current);

                        if (page == pageCount) {
                            dialogueHistory = newDialogueHistory;
                            WynntilsMod.postEvent(new QuestBookReloadedEvent.DialogueHistoryReloaded());
                        }
                    });
        }

        queryBuilder.build().executeQuery();
    }

    public static void rescanQuestBook() {
        WynntilsMod.info("Requesting rescan of Quest Book");
        queryQuestBook();
    }

    private static String getNextPageButtonName(int nextPageNum) {
        return "[§f§lPage " + nextPageNum + "§a >§2>§a>§2>§a>]";
    }

    private static String getQuestBookTitle(int pageNum) {
        return "^§0\\[Pg. " + pageNum + "\\] §8.*§0 Quests$";
    }

    private static String getMiniQuestBookTitle(int pageNum) {
        return "^§0\\[Pg. " + pageNum + "\\] §8.*§0 Mini-Quests$";
    }

    public static List<QuestInfo> getQuests() {
        return quests;
    }

    public static List<QuestInfo> getMiniQuests() {
        return miniQuests;
    }

    public static List<QuestInfo> getQuestsSorted(QuestSortOrder sortOrder) {
        return sortQuestInfoList(sortOrder, quests);
    }

    public static List<QuestInfo> getMiniQuestsSorted(QuestSortOrder sortOrder) {
        return sortQuestInfoList(sortOrder, miniQuests);
    }

    private static List<QuestInfo> sortQuestInfoList(QuestSortOrder sortOrder, List<QuestInfo> questList) {
        // All quests are always sorted by status (available then unavailable), and then
        // the given sort order, and finally a third way if the given sort order is equal.
        return switch (sortOrder) {
            case LEVEL -> questList.stream()
                    .sorted(Comparator.comparing(QuestInfo::getStatus)
                            .thenComparing(QuestInfo::getLevel)
                            .thenComparing(QuestInfo::getName))
                    .toList();
            case DISTANCE -> questList.stream()
                    .sorted(Comparator.comparing(QuestInfo::getStatus)
                            .thenComparing(new LocationComparator())
                            .thenComparing(QuestInfo::getName))
                    .toList();
            case ALPHABETIC -> questList.stream()
                    .sorted(Comparator.comparing(QuestInfo::getStatus)
                            .thenComparing(QuestInfo::getName)
                            .thenComparing(QuestInfo::getLevel))
                    .toList();
        };
    }

    private static Optional<QuestInfo> getQuestFromName(String name) {
        return quests.stream().filter(quest -> quest.getName().equals(name)).findFirst();
    }

    public static List<List<String>> getDialogueHistory() {
        return dialogueHistory;
    }

    public static void openQuestOnWiki(QuestInfo questInfo) {
        final String baseUrl = "https://wynncraft.fandom.com/wiki/";

        // TODO handle mini quest
        String name = questInfo.getName();
        String wikiQuestPageNameQuery = WebManager.getApiUrl("WikiQuestQuery");
        String url = wikiQuestPageNameQuery + WebUtils.encodeForCargoQuery(name);
        Request req = new RequestBuilder(url, "WikiQuestQuery")
                .handleJsonArray(jsonOutput -> {
                    String pageTitle = jsonOutput
                            .get(0)
                            .getAsJsonObject()
                            .get("_pageTitle")
                            .getAsString();
                    Utils.openUrl(baseUrl + WebUtils.encodeForWikiTitle(pageTitle));
                    return true;
                })
                .build();

        RequestHandler handler = new RequestHandler();

        handler.addAndDispatch(req, true);
    }

    public static QuestInfo getCurrentQuest() {
        return currentQuest;
    }

    public static class QuestScoreboardHandler implements ScoreboardHandler {
        @Override
        public void onSegmentChange(Segment newValue, ScoreboardModel.SegmentType segmentType) {
            List<String> content = newValue.getContent();

            if (content.isEmpty()) {
                WynntilsMod.error("QuestHandler: content was empty.");
            }

            StringBuilder questName = new StringBuilder();
            StringBuilder nextTask = new StringBuilder();

            for (String line : content) {
                if (line.startsWith("§e")) {
                    questName.append(ComponentUtils.stripFormatting(line)).append(" ");
                } else {
                    nextTask.append(line.replaceAll(ChatFormatting.WHITE.toString(), ChatFormatting.AQUA.toString())
                                    .replaceAll(ChatFormatting.GRAY.toString(), ChatFormatting.RESET.toString()))
                            .append(" ");
                }
            }

            Optional<QuestInfo> questInfoOpt =
                    QuestManager.getQuestFromName(questName.toString().trim());
            if (questInfoOpt.isEmpty()) return;

            QuestInfo questInfo = questInfoOpt.get();
            questInfo.setNextTask(nextTask.toString().trim());

            setCurrentQuest(questInfo);
        }

        @Override
        public void onSegmentRemove(Segment segment, ScoreboardModel.SegmentType segmentType) {
            setCurrentQuest(null);
        }

        @Override
        public void resetHandler() {
            setCurrentQuest(null);
        }

        private static void setCurrentQuest(QuestInfo questInfo) {
            currentQuest = questInfo;
            WynntilsMod.postEvent(new TrackedQuestUpdateEvent(currentQuest));
        }
    }

    private static class LocationComparator implements Comparator<QuestInfo> {
        private static final Vec3 PLAYER_LOCATION = McUtils.player().position();

        private double getDistance(Optional<Location> loc) {
            // Quests with no location always counts as closest
            if (loc.isEmpty()) return 0f;

            Location location = loc.get();
            return PLAYER_LOCATION.distanceToSqr(location.toVec3());
        }

        @Override
        public int compare(QuestInfo quest1, QuestInfo quest2) {
            Optional<Location> loc1 = quest1.getNextLocation();
            Optional<Location> loc2 = quest2.getNextLocation();
            return (int) (getDistance(loc1) - getDistance(loc2));
        }
    }

    public enum QuestSortOrder {
        LEVEL,
        DISTANCE,
        ALPHABETIC;

        public static QuestSortOrder fromString(String str) {
            if (str == null || str.isEmpty()) return LEVEL;

            try {
                return QuestSortOrder.valueOf(str.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return LEVEL;
            }
        }
    }
}
