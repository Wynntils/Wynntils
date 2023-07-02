/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.quests;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.models.characterstats.CombatXpModel;
import com.wynntils.models.quests.event.QuestBookReloadedEvent;
import com.wynntils.models.quests.type.QuestSortOrder;
import com.wynntils.models.tracker.TrackerScoreboardPart;
import com.wynntils.models.tracker.event.TrackerUpdatedEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

public final class QuestModel extends Model {
    private static final ScoreboardPart QUEST_SCOREBOARD_PART = new TrackerScoreboardPart();
    private static final QuestContainerQueries CONTAINER_QUERIES = new QuestContainerQueries();
    private static final DialogueHistoryQueries DIALOGUE_HISTORY_QUERIES = new DialogueHistoryQueries();
    private static final String MINI_QUEST_PREFIX = "Mini-Quest - ";

    private List<QuestInfo> quests = List.of();
    private List<QuestInfo> miniQuests = List.of();
    private List<List<StyledText>> dialogueHistory = List.of();
    private QuestInfo trackedQuest = null;
    private String afterRescanName;
    private StyledText afterRescanTask;

    public QuestModel(CombatXpModel combatXpModel) {
        super(List.of(combatXpModel));

        Handlers.Scoreboard.addPart(QUEST_SCOREBOARD_PART);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWorldStateChanged(WorldStateEvent e) {
        reset();
    }

    private void reset() {
        quests = List.of();
        miniQuests = List.of();
        dialogueHistory = List.of();
        trackedQuest = null;
        afterRescanName = null;
        afterRescanTask = null;
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

    public Optional<QuestInfo> getQuestFromName(String name) {
        return quests.stream().filter(quest -> quest.getName().equals(name)).findFirst();
    }

    public List<QuestInfo> getQuestsRaw() {
        return quests;
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

    public List<List<StyledText>> getDialogueHistory() {
        return dialogueHistory;
    }

    public void startTracking(QuestInfo questInfo) {
        CONTAINER_QUERIES.toggleTracking(questInfo);
    }

    public void stopTracking() {
        McUtils.sendCommand("tracking");
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

    public QuestInfo getTrackedQuest() {
        return trackedQuest;
    }

    public Location getTrackedQuestNextLocation() {
        QuestInfo questInfo = getTrackedQuest();

        if (questInfo == null) return null;

        Optional<Location> location = questInfo.getNextLocation();

        return location.orElse(null);
    }

    public void clearTrackedQuestFromScoreBoard() {
        updateTrackedQuest(null);
    }

    public void updateTrackedQuestFromScoreboard(String name, StyledText nextTask) {
        // If our quest book has not yet been scanned, we can't update now
        // but will do after scanning is complete
        if (updateAfterRescan(name, nextTask)) return;

        Optional<QuestInfo> questInfoOpt = getQuestInfoFromName(name);
        if (questInfoOpt.isEmpty()) {
            WynntilsMod.warn("Cannot match quest from scoreboard to actual quest: " + name);
            return;
        }

        QuestInfo questInfo = questInfoOpt.get();
        questInfo.setNextTask(nextTask);

        updateTrackedQuest(questInfo);
    }

    private void updateTrackedQuest(QuestInfo questInfo) {
        trackedQuest = questInfo;
        WynntilsMod.postEvent(new TrackerUpdatedEvent(trackedQuest));
    }

    private Optional<QuestInfo> getQuestInfoFromName(String name) {
        List<QuestInfo> questInfoList = name.startsWith(MINI_QUEST_PREFIX) ? miniQuests : quests;

        return questInfoList.stream()
                .filter(quest -> quest.getName().equals(stripPrefix(name)))
                .findFirst();
    }

    private boolean updateAfterRescan(String name, StyledText nextTask) {
        boolean isMiniQuest = name.startsWith(MINI_QUEST_PREFIX);
        List<QuestInfo> questInfoList = isMiniQuest ? miniQuests : quests;

        if (questInfoList.isEmpty()) {
            afterRescanTask = nextTask;
            afterRescanName = stripPrefix(name);
            rescanQuestBook(!isMiniQuest, isMiniQuest);
            return true;
        }

        return false;
    }

    private String stripPrefix(String name) {
        return StringUtils.replaceOnce(name, MINI_QUEST_PREFIX, "");
    }

    void updateQuestsFromQuery(List<QuestInfo> newQuests, QuestInfo trackedQuest) {
        quests = newQuests;
        maybeUpdateTrackedQuest(trackedQuest);
        WynntilsMod.postEvent(new QuestBookReloadedEvent.QuestsReloaded());
    }

    void updateMiniQuestsFromQuery(List<QuestInfo> newMiniQuests, QuestInfo trackedQuest) {
        miniQuests = newMiniQuests;
        maybeUpdateTrackedQuest(trackedQuest);
        WynntilsMod.postEvent(new QuestBookReloadedEvent.MiniQuestsReloaded());
    }

    private void maybeUpdateTrackedQuest(QuestInfo trackedQuest) {
        if (trackedQuest != this.trackedQuest) {
            if (trackedQuest != null && trackedQuest.getName().equals(afterRescanName)) {
                // We have stored the current task from last scoreboard update,
                // now we can finally present it
                trackedQuest.setNextTask(afterRescanTask);
                afterRescanName = null;
                afterRescanTask = null;
                Models.Quest.updateTrackedQuest(trackedQuest);
            }
            WynntilsMod.warn("Tracked Quest according to scoreboard is " + this.trackedQuest + " but query says "
                    + trackedQuest);
        }
    }

    void setDialogueHistory(List<List<StyledText>> newDialogueHistory) {
        dialogueHistory = newDialogueHistory;
        WynntilsMod.postEvent(new QuestBookReloadedEvent.DialogueHistoryReloaded());
    }

    /** Shared between the container query classes */
    public String getQuestBookTitleRegex(int pageNum) {
        return "§f\uE000\uE072";
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
