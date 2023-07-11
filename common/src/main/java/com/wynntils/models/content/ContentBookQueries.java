/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.models.content.type.ContentInfo;
import com.wynntils.models.content.type.ContentType;
import com.wynntils.models.items.items.gui.ContentItem;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
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

    private List<ContentInfo> newContent;

    /**
     * Trigger a rescan of the content book. When the rescan is done, Models.Content.updateFromContentBookQuery
     * will be called.
     */
    protected void queryContentBook(String filterName) {
        if (newContent != null) return;

        newContent = new ArrayList<>();

        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Content Book Query")
                .onError(msg -> {
                    WynntilsMod.warn("Problem querying Content Book: " + msg);
                    McUtils.sendMessageToClient(
                            Component.literal("Error updating content book.").withStyle(ChatFormatting.RED));
                })

                // Open content book
                .then(QueryStep.useItemInHotbar(InventoryUtils.CONTENT_BOOK_SLOT_NUM)
                        .expectContainerTitle(CONTENT_BOOK_TITLE))

                // Save filter state, and set it correctly
                .repeat(
                        c -> {
                            // FIXME
                            // if first time around, save current filter state

                            // check if our filter is of the requested type,
                            // if not return true
                            return false;
                        },
                        QueryStep.clickOnSlot(CHANGE_VIEW))

                // Process first page
                .reprocess(this::processContentBookPage)

                // Repeatedly click next page, if available, and process the following page
                .repeat(
                        c -> ScriptedContainerQuery.containerHasSlot(
                                c, NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, SCROLL_DOWN_TEXT),
                        QueryStep.clickOnSlot(NEXT_PAGE_SLOT).processIncomingContainer(this::processContentBookPage))

                // Restore filter to original value
                .repeat(
                        c -> {
                            // FIXME
                            // is filter the same as the one we saved?
                            return false;
                        },
                        QueryStep.clickOnSlot(CHANGE_VIEW))

                // Finally signal we're done
                .execute(() -> {
                    Models.Content.updateFromContentBookQuery(newContent);
                    newContent = null;
                })
                //
                .build();

        query.executeQuery();
    }

    private void processContentBookPage(ContainerContent container) {
        for (int slot = 0; slot < 54; slot++) {
            ItemStack itemStack = container.items().get(slot);
            Optional<ContentItem> contentItemOpt = Models.Item.asWynnItem(itemStack, ContentItem.class);
            if (contentItemOpt.isEmpty()) continue;

            ContentInfo contentInfo = contentItemOpt.get().getContentInfo();

            newContent.add(contentInfo);
        }
    }

    protected void toggleTracking(String name, ContentType contentType) {
        // We do not want to change filtering when tracking, since we get
        // no chance to reset it
        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Toggle Content Tracking Query")
                .onError(msg -> {
                    WynntilsMod.warn("Problem querying Content Book: " + msg);
                    McUtils.sendMessageToClient(
                            Component.literal("Error tracking content book.").withStyle(ChatFormatting.RED));
                })

                // Open compass/character menu
                .then(QueryStep.useItemInHotbar(InventoryUtils.CONTENT_BOOK_SLOT_NUM)
                        .expectContainerTitle(CONTENT_BOOK_TITLE))

                // Repeatedly check if the requested task is on this page,
                // if so, click it, otherwise click on next slot (if available)
                .repeat(
                        c -> {
                            int slot = findTrackedContent(c, name, contentType);
                            // Not found, try to go to next page
                            if (slot == -1) return true;

                            // Found it, now click it
                            ContainerUtils.clickOnSlot(slot, c.containerId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, c.items());
                            return false;
                        },
                        QueryStep.clickOnMatchingSlot(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, SCROLL_DOWN_TEXT))
                //
                .build();

        query.executeQuery();
    }

    private int findTrackedContent(ContainerContent container, String name, ContentType contentType) {
        for (int slot = 0; slot < 54; slot++) {
            ItemStack itemStack = container.items().get(slot);
            Optional<ContentItem> contentItemOpt = Models.Item.asWynnItem(itemStack, ContentItem.class);
            if (contentItemOpt.isEmpty()) continue;

            ContentInfo contentInfo = contentItemOpt.get().getContentInfo();
            if (contentInfo.type() == contentType && contentInfo.name().equals(name)) {
                // Found it!
                return slot;
            }
        }

        return -1;
    }
}
