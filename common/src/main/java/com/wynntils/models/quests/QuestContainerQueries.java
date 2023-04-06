/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.quests;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText2;
import com.wynntils.handlers.container.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class QuestContainerQueries {
    private static final int NEXT_PAGE_SLOT = 8;
    private static final int MINI_QUESTS_SLOT = 53;

    private List<QuestInfo> newQuests;
    private List<QuestInfo> newMiniQuests;
    private QuestInfo trackedQuest;

    /**
     * Trigger a rescan of the quest book. When the rescan is done, a QuestBookReloadedEvent will
     * be sent. The available quests are then available using getQuests.
     */
    protected void queryQuestBook() {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder("Quest Book Query")
                .onError(msg -> {
                    WynntilsMod.warn("Problem querying Quest Book: " + msg);
                    McUtils.sendMessageToClient(
                            Component.literal("Error updating quest book.").withStyle(ChatFormatting.RED));
                })
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(Models.Quest.getQuestBookTitleRegex(1))
                .processContainer(c -> processQuestBookPage(c, 1));

        for (int i = 2; i < 5; i++) {
            final int page = i; // Lambdas need final variables
            queryBuilder
                    .clickOnSlotWithName(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(page))
                    .matchTitle(Models.Quest.getQuestBookTitleRegex(page))
                    .processContainer(c -> processQuestBookPage(c, page));
        }

        queryBuilder.build().executeQuery();
    }

    private void processQuestBookPage(ContainerContent container, int page) {
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

                ItemStack itemStack = container.items().get(slot);
                QuestInfo questInfo = QuestInfoParser.parseItemStack(itemStack, page, false);
                if (questInfo == null) continue;

                newQuests.add(questInfo);
                if (questInfo.isTracked()) {
                    trackedQuest = questInfo;
                }
            }
        }

        if (page == 4) {
            // Last page finished
            Models.Quest.updateQuestsFromQuery(newQuests, trackedQuest);
        }
    }

    private StyledText2 getNextPageButtonName(int nextPageNum) {
        return StyledText2.of("[§f§lPage " + nextPageNum + "§a >§2>§a>§2>§a>]");
    }

    protected void queryMiniQuests() {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder("Quest Book Mini Quest Query")
                .onError(msg -> {
                    WynntilsMod.warn("Problem querying Quest Book for mini quests: " + msg);
                    McUtils.sendMessageToClient(
                            Component.literal("Error updating quest book.").withStyle(ChatFormatting.RED));
                })
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(Models.Quest.getQuestBookTitleRegex(1))
                .processContainer(c -> {})
                .clickOnSlot(MINI_QUESTS_SLOT)
                .matchTitle(getMiniQuestBookTitleRegex(1))
                .processContainer(c -> processMiniQuestBookPage(c, 1));

        for (int i = 2; i < 4; i++) {
            final int page = i; // Lambdas need final variables
            queryBuilder
                    .clickOnSlotWithName(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(page))
                    .matchTitle(getMiniQuestBookTitleRegex(page))
                    .processContainer(c -> processMiniQuestBookPage(c, page));
        }

        queryBuilder.build().executeQuery();
    }

    private void processMiniQuestBookPage(ContainerContent container, int page) {
        // Quests are in the top-left container area
        if (page == 1) {
            // Build new set of quests without disturbing current set
            newMiniQuests = new ArrayList<>();
        }
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int slot = row * 9 + col;

                ItemStack itemStack = container.items().get(slot);
                QuestInfo questInfo = QuestInfoParser.parseItemStack(itemStack, page, true);
                if (questInfo == null) continue;

                if (questInfo.isTracked()) {
                    trackedQuest = questInfo;
                }
                newMiniQuests.add(questInfo);
            }
        }

        if (page == 3) {
            // Last page finished
            Models.Quest.updateMiniQuestsFromQuery(newMiniQuests, trackedQuest);
        }
    }

    private String getMiniQuestBookTitleRegex(int pageNum) {
        return "^§0\\[Pg. " + pageNum + "\\] §8.*§0 Mini-Quests$";
    }

    protected void toggleTracking(QuestInfo questInfo) {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder("Quest Book Quest Pin Query")
                .onError(msg -> WynntilsMod.warn("Problem pinning quest in Quest Book: " + msg))
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(Models.Quest.getQuestBookTitleRegex(1));

        if (questInfo.isMiniQuest()) {
            queryBuilder
                    .processContainer(c -> {})
                    .clickOnSlot(MINI_QUESTS_SLOT)
                    .matchTitle(getMiniQuestBookTitleRegex(1));
        }

        if (questInfo.getPageNumber() > 1) {
            for (int i = 2; i <= questInfo.getPageNumber(); i++) {
                queryBuilder
                        .processContainer(container -> {}) // we ignore this because this is not the correct page
                        .clickOnSlotWithName(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(i))
                        .matchTitle(Models.Quest.getQuestBookTitleRegex(i));
            }
        }
        queryBuilder
                .processContainer(c -> findQuestForTracking(c, questInfo, questInfo.isMiniQuest()))
                .build()
                .executeQuery();
    }

    private void findQuestForTracking(ContainerContent container, QuestInfo questInfo, boolean isMiniQuest) {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int slot = row * 9 + col;

                // Very first slot is chat history, but only in the main quests page
                if (!isMiniQuest && slot == 0) continue;

                ItemStack itemStack = container.items().get(slot);

                String questName = QuestInfoParser.getQuestName(itemStack);
                if (Objects.equals(questName, questInfo.getName())) {
                    ContainerUtils.clickOnSlot(
                            slot, container.containerId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, container.items());
                    return;
                }
            }
        }
    }
}
