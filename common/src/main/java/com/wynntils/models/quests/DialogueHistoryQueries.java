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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class DialogueHistoryQueries {
    private static final int DIALOGUE_HISTORY_SLOT = 58;
    private static final Pattern DIALOGUE_HISTORY_PAGE_PATTERN = Pattern.compile("§8Page (\\d+) of (\\d+)");
    private static final StyledText DIALOGUE_HISTORY = StyledText.fromString("§2Dialogue History");

    private List<List<StyledText>> newDialogueHistory;

    protected void scanDialogueHistory() {
        ScriptedContainerQuery query = ScriptedContainerQuery.builder(
                        "Quest Book Dialogue History Query")
                .onError(msg -> WynntilsMod.warn("Problem getting dialogue history in Quest Book: " + msg))
                // Open content book
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(Models.Quest.getQuestBookTitleRegex(1))
                .ignoreIncomingContainer()
                // Repeatedly read the dialogue history from the lore of the history item,
                // and if it is on the last page, stop repeating, otherwise click the slot
                // to get to the next page
                .repeat()
                .checkCurrentContainer((c) -> {
                    ItemStack dialogueHistoryItem = c.items().get(DIALOGUE_HISTORY_SLOT);

                    if (!StyledText.fromComponent(dialogueHistoryItem.getHoverName())
                            .equals(DIALOGUE_HISTORY)) return false;

                    List<StyledText> current = LoreUtils.getLore(dialogueHistoryItem).stream()
                            .dropWhile(s -> s.isBlank())
                            .takeWhile(s -> !s.isBlank())
                            .toList();

                    newDialogueHistory.add(current);

                    for (StyledText line : LoreUtils.getLore(dialogueHistoryItem)) {
                        Matcher matcher = line.getMatcher(DIALOGUE_HISTORY_PAGE_PATTERN);
                        if (matcher.matches()) {
                            int currentPage = Integer.parseInt(matcher.group(1));
                            int maxPage = Integer.parseInt(matcher.group(2));

                            // Continue with the processing loop until we are at the last page
                            return currentPage != maxPage;
                        }
                    }

                    return false;
                })
                .clickOnSlot(DIALOGUE_HISTORY_SLOT)
                .expectSameMenu()
                .ignoreIncomingContainer()
                .endRepeat()
                //
                .build();

        query.executeQuery();
    }
}
