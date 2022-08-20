/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.questbook;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.event.QuestBookReloadedEvent;
import com.wynntils.wynn.model.container.ContainerContent;
import com.wynntils.wynn.model.container.ScriptedContainerQuery;
import com.wynntils.wynn.utils.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class QuestBookModel extends Model {
    private static final int NEXT_PAGE_SLOT = 8;

    private static List<QuestInfo> quests = List.of();
    private static List<QuestInfo> newQuests;

    public static List<QuestInfo> getQuests() {
        return quests;
    }

    public static void queryQuestBook() {
        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Quest Book Query")
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(getQuestBookTitle(1))
                .processContainer(c -> processQuestBookPage(c, 1))
                .clickOnSlotMatching(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(2))
                .matchTitle(getQuestBookTitle(2))
                .processContainer(c -> processQuestBookPage(c, 2))
                .clickOnSlotMatching(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(3))
                .matchTitle(getQuestBookTitle(3))
                .processContainer(c -> processQuestBookPage(c, 3))
                .clickOnSlotMatching(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(4))
                .matchTitle(getQuestBookTitle(4))
                .processContainer(c -> processQuestBookPage(c, 4))
                .onError(msg -> WynntilsMod.warn("Error querying Quest Book:" + msg))
                .build();

        query.executeQuery();
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
                QuestInfo questInfo = QuestInfo.fromItem(item);
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

    private static String getNextPageButtonName(int nextPageNum) {
        return "[§f§lPage " + nextPageNum + "§a >§2>§a>§2>§a>]";
    }

    private static String getQuestBookTitle(int pageNum) {
        return "^§0\\[Pg. " + pageNum + "\\] §8.*§0 Quests$";
    }
}
