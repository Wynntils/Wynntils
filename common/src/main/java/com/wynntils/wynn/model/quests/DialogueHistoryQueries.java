/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.quests;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.handlers.container.ScriptedContainerQuery;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wynn.utils.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class DialogueHistoryQueries {
    private static final Pattern DIALOGUE_HISTORY_PAGE_PATTERN = Pattern.compile("§7Page \\[(\\d+)/(\\d+)\\]");

    private List<List<String>> newDialogueHistory;

    protected void scanDialogueHistory() {
        findNumberOfPages();
    }

    private void findNumberOfPages() {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder(
                        "Quest Book Dialogue History Query")
                .onError(msg -> WynntilsMod.warn("Problem getting dialogue history in Quest Book: " + msg))
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(Managers.Quest.getQuestBookTitle(1))
                .processContainer((c) -> {
                    ItemStack dialogueHistoryItem = c.items().get(0);

                    if (!ComponentUtils.getCoded(dialogueHistoryItem.getHoverName())
                            .equals("§bDialogue History")) return;

                    for (String line : ItemUtils.getLore(dialogueHistoryItem)) {
                        Matcher matcher = DIALOGUE_HISTORY_PAGE_PATTERN.matcher(line);

                        if (matcher.matches()) {
                            int pageCount = Integer.parseInt(matcher.group(2));
                            // Now that we know the max number of pages, we can
                            // create the actual extraction query
                            createDialogueHistoryQuery(pageCount);
                            break;
                        }
                    }
                });

        queryBuilder.build().executeQuery();
    }

    private void createDialogueHistoryQuery(int pageCount) {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder(
                        "Quest Book Dialogue History Query 2")
                .onError(msg -> WynntilsMod.warn("Problem getting dialogue history (2) in Quest Book: " + msg))
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(Managers.Quest.getQuestBookTitle(1))
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
                    .matchTitle(Managers.Quest.getQuestBookTitle(1))
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
                            Managers.Quest.setDialogueHistory(newDialogueHistory);
                        }
                    });
        }

        queryBuilder.build().executeQuery();
    }
}
