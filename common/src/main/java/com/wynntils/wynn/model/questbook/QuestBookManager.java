/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.questbook;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.QuestBookReloadedEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.model.container.ContainerContent;
import com.wynntils.wynn.model.container.ScriptedContainerQuery;
import com.wynntils.wynn.utils.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class QuestBookManager extends CoreManager {
    private static final int NEXT_PAGE_SLOT = 8;

    private static List<QuestInfo> quests = List.of();
    private static List<QuestInfo> newQuests;

    public static void init() {}

    @SubscribeEvent
    public static void onWorldChange(WorldStateEvent e) {
        if (e.getNewState() == WorldStateManager.State.WORLD) {
            rescanQuestBook();
        }
    }

    /**
     * Trigger a rescan of the quest book. When the rescan is done, a QuestBookReloadedEvent will
     * be sent. The available quests are then available using getQuests.
     */
    private static void queryQuestBook() {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder("Quest Book Query")
                .onError(msg -> WynntilsMod.warn("Problem querying Quest Book: " + msg))
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(getQuestBookTitle(1))
                .processContainer(c -> processQuestBookPage(c, 1));

        for (int i = 2; i < 5; i++) {
            final int page = i; // Lambdas need final variables
            queryBuilder
                    .clickOnSlotMatching(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(page))
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
                QuestInfo questInfo = QuestInfo.parseItem(item);
                if (questInfo == null) continue;

                newQuests.add(questInfo);
            }
        }

        if (page == 4) {
            // Last page finished
            quests = newQuests;
            WynntilsMod.getEventBus().post(new QuestBookReloadedEvent());
        }
    }

    public static void rescanQuestBook() {
        WynntilsMod.info("Requesting rescan of Quest Book");
        McUtils.player().sendMessage(new TextComponent("Scanning Quest Book...").withStyle(ChatFormatting.GRAY), null);
        QuestBookManager.queryQuestBook();
        McUtils.player().sendMessage(new TextComponent("Quest Book scanned.").withStyle(ChatFormatting.GRAY), null);
    }

    private static String getNextPageButtonName(int nextPageNum) {
        return "[§f§lPage " + nextPageNum + "§a >§2>§a>§2>§a>]";
    }

    private static String getQuestBookTitle(int pageNum) {
        return "^§0\\[Pg. " + pageNum + "\\] §8.*§0 Quests$";
    }

    public static List<QuestInfo> getQuests() {
        return quests;
    }
}
