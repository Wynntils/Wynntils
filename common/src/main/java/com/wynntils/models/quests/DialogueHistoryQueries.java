/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.quests;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.container.ScriptedContainerQuery;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class DialogueHistoryQueries {
    private static final Pattern DIALOGUE_HISTORY_PAGE_PATTERN = Pattern.compile("§7Page \\[(\\d+)/(\\d+)\\]");
    public static final StyledText DIALOGUE_HISTORY = StyledText.fromString("§bDialogue History");

    private List<List<StyledText>> newDialogueHistory;

    protected void scanDialogueHistory() {
        findNumberOfPages();
    }

    private void findNumberOfPages() {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder(
                        "Quest Book Dialogue History Query")
                .onError(msg -> WynntilsMod.warn("Problem getting dialogue history in Quest Book: " + msg))
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(Models.Quest.getQuestBookTitleRegex(1))
                .processContainer((c) -> {
                    ItemStack dialogueHistoryItem = c.items().get(0);

                    if (!StyledText.fromComponent(dialogueHistoryItem.getHoverName())
                            .equals(DIALOGUE_HISTORY)) return;

                    for (StyledText line : LoreUtils.getLore(dialogueHistoryItem)) {
                        Matcher matcher = line.getMatcher(DIALOGUE_HISTORY_PAGE_PATTERN);

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
                .matchTitle(Models.Quest.getQuestBookTitleRegex(1))
                .setWaitForMenuReopen(false)
                .processContainer((c) -> {
                    ItemStack dialogueHistoryItem = c.items().get(0);

                    if (!StyledText.fromComponent(dialogueHistoryItem.getHoverName())
                            .equals(DIALOGUE_HISTORY)) return;

                    newDialogueHistory = new ArrayList<>();

                    List<StyledText> current = LoreUtils.getLore(dialogueHistoryItem).stream()
                            .dropWhile(StyledText::isBlank)
                            .takeWhile(s -> !s.isBlank())
                            .toList();

                    newDialogueHistory.add(current);
                });

        for (int i = 2; i <= pageCount; i++) {
            int page = i;
            queryBuilder
                    .clickOnSlot(0)
                    .matchTitle(Models.Quest.getQuestBookTitleRegex(1))
                    .setWaitForMenuReopen(false)
                    .processContainer((c) -> {
                        ItemStack dialogueHistoryItem = c.items().get(0);

                        if (!StyledText.fromComponent(dialogueHistoryItem.getHoverName())
                                .equals(DIALOGUE_HISTORY)) return;

                        List<StyledText> current = LoreUtils.getLore(dialogueHistoryItem).stream()
                                .dropWhile(StyledText::isBlank)
                                .takeWhile(s -> !s.isBlank())
                                .toList();

                        newDialogueHistory.add(current);

                        if (page == pageCount) {
                            Models.Quest.setDialogueHistory(newDialogueHistory);
                        }
                    });
        }

        queryBuilder.build().executeQuery();
    }
}
