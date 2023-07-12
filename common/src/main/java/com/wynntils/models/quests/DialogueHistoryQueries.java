/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.quests;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.models.content.ContentBookQueries;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class DialogueHistoryQueries {
    private static final int DIALOGUE_HISTORY_SLOT = 58;
    private static final Pattern DIALOGUE_HISTORY_PAGE_PATTERN = Pattern.compile("§8Page (\\d+) of (\\d+)");
    private static final StyledText DIALOGUE_HISTORY = StyledText.fromString("§2Dialogue History");

    private List<List<StyledText>> newDialogueHistory;

    protected void scanDialogueHistory() {
        // Check if a scan is already underway
        if (newDialogueHistory != null) return;

        newDialogueHistory = new ArrayList<>();

        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Dialogue History Query")
                .onError(msg -> {
                    WynntilsMod.warn("Problem getting dialogue history: " + msg);
                    McUtils.sendMessageToClient(
                            Component.literal("Dumping Dialogue History failed").withStyle(ChatFormatting.RED));
                })

                // Open content book
                .then(QueryStep.useItemInHotbar(InventoryUtils.CONTENT_BOOK_SLOT_NUM)
                        .expectContainerTitle(ContentBookQueries.CONTENT_BOOK_TITLE))

                // Repeatedly read the dialogue history from the lore of the history item,
                // and if it is on the last page, stop repeating, otherwise click the slot
                // to get to the next page
                .repeat(this::checkDialoguePage, QueryStep.clickOnSlot(DIALOGUE_HISTORY_SLOT))
                .execute(() -> {
                    Models.Quest.setDialogueHistory(newDialogueHistory);
                    newDialogueHistory = null;
                })
                .build();

        query.executeQuery();
    }

    private boolean checkDialoguePage(ContainerContent c) {
        ItemStack dialogueHistoryItem = c.items().get(DIALOGUE_HISTORY_SLOT);

        if (!StyledText.fromComponent(dialogueHistoryItem.getHoverName()).equals(DIALOGUE_HISTORY)) return false;

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

        // Fallback: If we failed to find the page line, stop looping
        return false;
    }
}
