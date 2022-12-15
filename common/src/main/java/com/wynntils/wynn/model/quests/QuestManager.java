/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.quests;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Manager;
import com.wynntils.core.managers.Managers;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlId;
import com.wynntils.mc.objects.Location;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.QuestBookReloadedEvent;
import com.wynntils.wynn.event.TrackedQuestUpdateEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class QuestManager extends Manager {
    public static final QuestScoreboardHandler SCOREBOARD_HANDLER = new QuestScoreboardHandler();
    private static final QuestContainerQueries CONTAINER_QUERIES = new QuestContainerQueries();
    private static final DialogueHistoryQueries DIALOGUE_HISTORY_QUERIES = new DialogueHistoryQueries();

    private List<QuestInfo> quests = List.of();
    private List<QuestInfo> miniQuests = List.of();
    private List<List<String>> dialogueHistory = List.of();
    private QuestInfo currentQuest = null;

    public QuestManager(NetManager netManager) {
        super(List.of(netManager));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWorldStateChanged(WorldStateEvent e) {
        quests = List.of();
        dialogueHistory = List.of();
    }

    public void rescanQuestBook(boolean includeQuests, boolean includeMiniQuests) {
        WynntilsMod.info("Requesting rescan of Quest Book");
        if (includeQuests) {
            CONTAINER_QUERIES.queryQuestBook();
        }
        if (includeMiniQuests) {
            CONTAINER_QUERIES.queryMiniQuests();
        }
    }

    public void rescanDialogueHistory() {
        DIALOGUE_HISTORY_QUERIES.scanDialogueHistory();
    }

    public List<QuestInfo> getQuests(QuestSortOrder sortOrder) {
        return sortQuestInfoList(sortOrder, quests);
    }

    public List<QuestInfo> getMiniQuests(QuestSortOrder sortOrder) {
        return sortQuestInfoList(sortOrder, miniQuests);
    }

    private List<QuestInfo> sortQuestInfoList(QuestSortOrder sortOrder, List<QuestInfo> questList) {
        // All quests are always sorted by status (available then unavailable), and then
        // the given sort order, and finally a third way if the given sort order is equal.
        return switch (sortOrder) {
            case LEVEL -> questList.stream()
                    .sorted(Comparator.comparing(QuestInfo::getStatus)
                            .thenComparing(QuestInfo::getSortLevel)
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
                            .thenComparing(QuestInfo::getSortLevel))
                    .toList();
        };
    }

    public Optional<QuestInfo> getQuestFromName(String name) {
        return quests.stream().filter(quest -> quest.getName().equals(name)).findFirst();
    }

    public List<List<String>> getDialogueHistory() {
        return dialogueHistory;
    }

    public void toggleTracking(QuestInfo questInfo) {
        CONTAINER_QUERIES.toggleTracking(questInfo);
    }

    public void openQuestOnWiki(QuestInfo questInfo) {
        if (questInfo.isMiniQuest()) {
            String type = questInfo.getName().split(" ")[0];

            String wikiName = "Quests#" + type + "ing_Posts";

            Managers.Net.openLink(UrlId.LINK_WIKI_LOOKUP, Map.of("title", wikiName));
            return;
        }

        ApiResponse apiResponse =
                Managers.Net.callApi(UrlId.API_WIKI_QUEST_PAGE_QUERY, Map.of("name", questInfo.getName()));
        apiResponse.handleJsonArray(json -> {
            String pageTitle = json.get(0).getAsJsonObject().get("_pageTitle").getAsString();
            Managers.Net.openLink(UrlId.LINK_WIKI_LOOKUP, Map.of("title", pageTitle));
        });
    }

    public QuestInfo getCurrentQuest() {
        return currentQuest;
    }

    public Location getCurrentQuestLocation() {
        QuestInfo questInfo = getCurrentQuest();

        if (questInfo == null) return null;

        Optional<Location> location = questInfo.getNextLocation();

        if (location.isEmpty()) return null;

        return location.get();
    }

    protected void setCurrentQuest(QuestInfo questInfo) {
        currentQuest = questInfo;
        WynntilsMod.postEvent(new TrackedQuestUpdateEvent(currentQuest));
    }

    protected void setQuests(List<QuestInfo> newQuests) {
        quests = newQuests;
        WynntilsMod.postEvent(new QuestBookReloadedEvent.QuestsReloaded());
    }

    protected void setMiniQuests(List<QuestInfo> newMiniQuests) {
        miniQuests = newMiniQuests;
        WynntilsMod.postEvent(new QuestBookReloadedEvent.MiniQuestsReloaded());
    }

    protected void setDialogueHistory(List<List<String>> newDialogueHistory) {
        dialogueHistory = newDialogueHistory;
        WynntilsMod.postEvent(new QuestBookReloadedEvent.DialogueHistoryReloaded());
    }

    /** Shared between the container query classes */
    public String getQuestBookTitle(int pageNum) {
        return "^§0\\[Pg. " + pageNum + "\\] §8.*§0 Quests$";
    }

    private static class LocationComparator implements Comparator<QuestInfo> {
        private final Vec3 playerLocation = McUtils.player().position();

        private double getDistance(Optional<Location> loc) {
            // Quests with no location always counts as closest
            if (loc.isEmpty()) return 0f;

            Location location = loc.get();
            return playerLocation.distanceToSqr(location.toVec3());
        }

        @Override
        public int compare(QuestInfo quest1, QuestInfo quest2) {
            Optional<Location> loc1 = quest1.getNextLocation();
            Optional<Location> loc2 = quest2.getNextLocation();
            return (int) (getDistance(loc1) - getDistance(loc2));
        }
    }
}
