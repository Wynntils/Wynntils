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
    public static final String CONTENT_BOOK_TITLE = "§f\uE000\uE072";
    private static final StyledText SCROLL_DOWN_TEXT = StyledText.fromString("§7Scroll Down");

    private List<ContentInfo> newQuests;
    private List<ContentInfo> newMiniQuests;
    private ContentInfo trackedQuest;

    /**
     * Trigger a rescan of the quest book. When the rescan is done, a QuestBookReloadedEvent will
     * be sent. The available quests are then available using getQuests.
     */
    protected void queryQuestBook() {
        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Quest Book Query")
                .onError(msg -> {
                    WynntilsMod.warn("Problem querying Quest Book: " + msg);
                    McUtils.sendMessageToClient(
                            Component.literal("Error updating quest book.").withStyle(ChatFormatting.RED));
                })

                // Open content book
                .then(ScriptedContainerQuery.QueryStep.useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                        .matchTitle(CONTENT_BOOK_TITLE)
                        .ignoreIncomingContainer())

                // Save filter state, and set it correctly
                .repeat(
                        c -> {
                            // if first time around, save current filter state

                            // check if our filter is of the requested type,
                            // if not return true
                            return false;
                        },
                        ScriptedContainerQuery.QueryStep.clickOnSlot(CHANGE_VIEW).expectSameMenu().ignoreIncomingContainer())

                // Process first page
                .execute(() -> {
                    newQuests = new ArrayList<>();
                })
                .reprocess(c -> processQuestBookPage(c))

                // Repeatedly click next page, if available, and process the following page
                .repeat(
                        c -> ScriptedContainerQuery.containerHasSlot(
                                c, NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, SCROLL_DOWN_TEXT),
                        ScriptedContainerQuery.QueryStep.clickOnSlot(NEXT_PAGE_SLOT)
                                .expectSameMenu()
                                .processIncomingContainer(c -> processQuestBookPage(c)))

                // Restore filter to original value
                .repeat(
                        c -> {
                            // is filter the same as the one we saved?
                            return false;
                        },
                        ScriptedContainerQuery.QueryStep.clickOnSlot(CHANGE_VIEW).expectSameMenu().ignoreIncomingContainer())

                // Finally signal we're done
                .execute(() -> {
                    Models.Content.updateFromBookQuery(newQuests);
                })
                //
                .build();

        query.executeQuery();
    }

    private String getItemDesc(List<ItemStack> items) {
        StringBuilder sb = new StringBuilder();
        for (var item : items) {
            sb.append(StyledText.fromComponent(item.getHoverName()));
            sb.append(", ");
        }
        return sb.toString();
    }

    private void processQuestBookPage(ContainerContent container) {

        System.out.println("items in PROCESS PAGE:" + getItemDesc(container.items()));

        // Quests are in the top-left container area
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
    }

    protected void toggleTracking(QuestInfo questInfo) {
        // We do not want to change filtering when tracking, since we get
        // no chance to reset it
        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Toggle Content Tracking Query")
                .onError(msg -> {
                    WynntilsMod.warn("Problem querying Content Book: " + msg);
                    McUtils.sendMessageToClient(
                            Component.literal("Error tracking content book.").withStyle(ChatFormatting.RED));
                })

                // Open compass/character menu
                .then(ScriptedContainerQuery.QueryStep.useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                        .matchTitle(CONTENT_BOOK_TITLE)
                        .ignoreIncomingContainer())

                // Repeatedly check if the requested task is on this page,
                // if so, click it, otherwise click on next slot (if available)
                .repeat(
                        c -> {
                            // is any of our new items the requested one?
                            // if so, click on it's slot and return false,
                            // otherwise return true to continue to next page
                            // also, .checkIfPreviousSlotWithName(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, SCROLL_DOWN_TEXT)
                            // otherwise return failure

                            return false;
                        },
                        ScriptedContainerQuery.QueryStep.clickOnSlot(NEXT_PAGE_SLOT).expectSameMenu().ignoreIncomingContainer())
                //
                .build();

        query.executeQuery();
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
