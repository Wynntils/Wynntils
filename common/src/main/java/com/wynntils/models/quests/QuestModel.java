/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.quests;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.characterstats.CombatXpModel;
import com.wynntils.models.content.type.ContentInfo;
import com.wynntils.models.content.type.ContentType;
import com.wynntils.models.quests.event.QuestBookReloadedEvent;
import com.wynntils.models.quests.type.QuestLength;
import com.wynntils.models.quests.type.QuestSortOrder;
import com.wynntils.models.quests.type.QuestStatus;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

public final class QuestModel extends Model {
    private static final DialogueHistoryQueries DIALOGUE_HISTORY_QUERIES = new DialogueHistoryQueries();
    private static final String MINI_QUEST_PREFIX = "Mini-Quest - ";

    private List<QuestInfo> quests = List.of();
    private List<QuestInfo> miniQuests = List.of();
    private List<List<StyledText>> dialogueHistory = List.of();

    public QuestModel(CombatXpModel combatXpModel) {
        super(List.of(combatXpModel));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWorldStateChanged(WorldStateEvent e) {
        reset();
    }

    private void reset() {
        quests = List.of();
        miniQuests = List.of();
        dialogueHistory = List.of();
    }

    public void rescanQuestBook(boolean includeQuests, boolean includeMiniQuests) {
        WynntilsMod.info("Requesting rescan of Quests in Content Book");
        if (includeQuests) {
            Models.Content.scanContentBook(ContentType.QUEST, this::updateQuestsFromQuery);
        }
        if (includeMiniQuests) {
            Models.Content.scanContentBook(ContentType.MINI_QUEST, this::updateMiniQuestsFromQuery);
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
        Models.Content.startTracking(
                questInfo.getName(), questInfo.isMiniQuest() ? ContentType.MINI_QUEST : ContentType.QUEST);
    }

    public void stopTracking() {
        Models.Content.stopTracking();
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

    public Optional<QuestInfo> getQuestInfoFromName(String name) {
        return Stream.concat(quests.stream(), miniQuests.stream())
                .filter(quest -> quest.getName().equals(stripPrefix(name)))
                .findFirst();
    }

    private String stripPrefix(String name) {
        return StringUtils.replaceOnce(name, MINI_QUEST_PREFIX, "");
    }

    private void updateQuestsFromQuery(List<ContentInfo> newContent, List<StyledText> progress) {
        List<QuestInfo> newQuests = new ArrayList<>();

        for (ContentInfo content : newContent) {
            if (content.type() != ContentType.QUEST && content.type() != ContentType.STORYLINE_QUEST) {
                WynntilsMod.warn("Incorrect quest content type recieved: " + content);
                continue;
            }
            QuestInfo questInfo = getQuestInfoFromContent(content);
            newQuests.add(questInfo);
        }
        quests = newQuests;
        WynntilsMod.postEvent(new QuestBookReloadedEvent.QuestsReloaded());
        WynntilsMod.info("Updated quests from query, got " + quests.size() + " quests.");
    }

    private void updateMiniQuestsFromQuery(List<ContentInfo> newContent, List<StyledText> progress) {
        List<QuestInfo> newMiniQuests = new ArrayList<>();

        for (ContentInfo content : newContent) {
            if (content.type() != ContentType.MINI_QUEST) {
                WynntilsMod.warn("Incorrect mini-quest content type recieved: " + content);
                continue;
            }
            QuestInfo questInfo = getQuestInfoFromContent(content);
            newMiniQuests.add(questInfo);
        }

        miniQuests = newMiniQuests;
        WynntilsMod.postEvent(new QuestBookReloadedEvent.MiniQuestsReloaded());
        WynntilsMod.info("Updated mini-quests from query, got " + miniQuests.size() + " mini-quests.");
    }

    private static QuestInfo getQuestInfoFromContent(ContentInfo content) {
        // We should always have a length, but if not, better fake one than crashing

        return new QuestInfo(
                content.name(),
                QuestStatus.fromContentStatus(content.status()),
                QuestLength.fromContentLength(content.length()),
                content.requirements().level().key(),
                content.description().orElse(StyledText.EMPTY),
                // FIXME! Additional requirements missing
                List.of(),
                content.type() == ContentType.MINI_QUEST);
    }

    void setDialogueHistory(List<List<StyledText>> newDialogueHistory) {
        dialogueHistory = newDialogueHistory;
        WynntilsMod.postEvent(new QuestBookReloadedEvent.DialogueHistoryReloaded());
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
