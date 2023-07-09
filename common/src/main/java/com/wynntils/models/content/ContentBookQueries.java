/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.container.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.models.content.type.ContentInfo;
import com.wynntils.models.items.items.gui.ContentItem;
import com.wynntils.models.quests.QuestInfo;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class ContentBookQueries {
    private static final int NEXT_PAGE_SLOT = 69;
    private static final int CHANGE_VIEW = 66;
    private static final String CONTENT_BOOK_TITLE = "§f\uE000\uE072";
    private static final StyledText SCROLL_DOWN_TEXT = StyledText.fromString("§7Scroll Down");

    private List<ContentInfo> newQuests;
    private List<ContentInfo> newMiniQuests;
    private ContentInfo trackedQuest;

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
                .matchTitle(CONTENT_BOOK_TITLE)
/*                .processContainer(c -> { // ignore before we have fixed the filter
                })
                .clickOnSlotWithName(CHANGE_VIEW, Items.GOLDEN_PICKAXE, StyledText.fromString("§eFilter"))
                .expectSameContainer()

 */
                .processContainer(c -> processQuestBookPage(c, 1));

        for (int i = 2; i < 21; i++) {
            final int page = i; // Lambdas need final variables
            queryBuilder
                    .clickOnSlotWithName(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, SCROLL_DOWN_TEXT)
                    .expectSameContainer()
                    .processContainer(c -> processQuestBookPage(c, page));
        }

        queryBuilder.build().executeQuery();
    }

    private String getItemDesc(List<ItemStack> items) {
        StringBuilder sb = new StringBuilder();
        for (var item : items) {
            sb.append(StyledText.fromComponent(item.getHoverName()));
            sb.append(", ");
        }
        return sb.toString();
    }



    private void processQuestBookPage(ContainerContent container, int page) {

        System.out.println("items in PROCESS PAGE " + page + " :" + getItemDesc(container.items()));

        // Quests are in the top-left container area
        if (page == 1) {
            // Build new set of quests without disturbing current set
            newQuests = new ArrayList<>();
        }
        for (int slot = 0; slot < 54; slot++) {
            ItemStack itemStack = container.items().get(slot);
            Optional<ContentItem> contentItemOpt = Models.Item.asWynnItem(itemStack, ContentItem.class);
            if (contentItemOpt.isEmpty()) continue;

            ContentInfo contentInfo = contentItemOpt.get().getContentInfo();

            newQuests.add(contentInfo);
            if (contentInfo.isTracked()) {
                trackedQuest = contentInfo;
            }

        }

        // FIXME
        if (page == 20) {
            // Last page finished
            Models.Content.updateFromBookQuery(newQuests);
        }
    }


    protected void toggleTracking(QuestInfo questInfo) {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder("Quest Book Quest Pin Query")
                .onError(msg -> WynntilsMod.warn("Problem pinning quest in Quest Book: " + msg))
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(CONTENT_BOOK_TITLE);


        if (questInfo.getPageNumber() > 1) {
            for (int i = 2; i <= questInfo.getPageNumber(); i++) {
                queryBuilder
                        .processContainer(container -> {}) // we ignore this because this is not the correct page
                        .clickOnSlotWithName(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, SCROLL_DOWN_TEXT)
                        .matchTitle(CONTENT_BOOK_TITLE);
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

                String questName = "FIXME"; // QuestInfoParser.getQuestName(itemStack);
                if (Objects.equals(questName, questInfo.getName())) {
                    ContainerUtils.clickOnSlot(
                            slot, container.containerId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, container.items());
                    return;
                }
            }
        }
    }
}
