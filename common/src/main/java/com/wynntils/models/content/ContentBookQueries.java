/*
 * Copyright © Wynntils 2022-2023.
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
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class ContentBookQueries {
    private static final int NEXT_PAGE_SLOT = 69;
    private static final int CHANGE_VIEW = 66;
    private static final StyledText SCROLL_DOWN_TEXT = StyledText.fromString("§7Scroll Down");
    private static final Pattern ACTIVE_FILTER = Pattern.compile("^§f- §7(.*)$");
    private static final int MAX_FILTERS = 11; // FIXME
    private String selectedFilter;
    private int filterLoopCount;

    /**
     * Trigger a rescan of the content book. When the rescan is done, Models.Content.updateFromContentBookQuery
     * will be called.
     */
    protected void queryContentBook(String filterName, Consumer<List<ContentInfo>> processResult) {
        List<ContentInfo> newContent = new ArrayList<>();

        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Content Book Query for " + filterName)
                .onError(msg -> {
                    WynntilsMod.warn("Problem querying Content Book: " + msg);
                    McUtils.sendMessageToClient(
                            Component.literal("Dumping Content Book failed").withStyle(ChatFormatting.RED));
                })

                // Open content book
                .then(QueryStep.useItemInHotbar(InventoryUtils.CONTENT_BOOK_SLOT_NUM)
                        .expectContainerTitle(Models.Content.CONTENT_BOOK_TITLE))

                // Save filter state, and set it correctly
                .execute(() -> {
                    filterLoopCount = 0;
                    selectedFilter = null;
                })
                .repeat(
                        c -> {
                            filterLoopCount++;
                            if (filterLoopCount > MAX_FILTERS)
                                // FIXME: exception
                                return false;

                            String activeFilter = getActiveFilter(c.items().get(CHANGE_VIEW));

                            // FIXME: exception
                            if (activeFilter == null) return false;

                            if (selectedFilter == null) {
                                selectedFilter = activeFilter;
                            }

                            // Continue looping until filter matches
                            return !activeFilter.equals(filterName);
                        },
                        QueryStep.clickOnSlot(CHANGE_VIEW))

                // Process first page
                .reprocess(c -> processContentBookPage(c, newContent))

                // Repeatedly click next page, if available, and process the following page
                .repeat(
                        c -> ScriptedContainerQuery.containerHasSlot(
                                c, NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, SCROLL_DOWN_TEXT),
                        QueryStep.clickOnSlot(NEXT_PAGE_SLOT)
                                .processIncomingContainer(c -> processContentBookPage(c, newContent)))

                // Restore filter to original value
                .execute(() -> filterLoopCount = 0)
                .repeat(
                        c -> {
                            filterLoopCount++;
                            if (filterLoopCount > MAX_FILTERS)
                                // FIXME: exception
                                return false;

                            String activeFilter = getActiveFilter(c.items().get(CHANGE_VIEW));

                            // FIXME: exception
                            if (activeFilter == null) return false;

                            // Continue looping until filter matches original value
                            return !activeFilter.equals(selectedFilter);
                        },
                        QueryStep.clickOnSlot(CHANGE_VIEW))

                // Finally signal we're done
                .execute(() -> {
                    processResult.accept(newContent);
                })
                .build();

        query.executeQuery();
    }

    private String getActiveFilter(ItemStack itemStack) {
        StyledText itemName = InventoryUtils.getItemName(itemStack);
        if (!itemName.equals(StyledText.fromString("§eFilter"))) return null;

        List<StyledText> lore = LoreUtils.getLore(itemStack);
        for (StyledText line : lore) {
            Matcher m = line.getMatcher(ACTIVE_FILTER);
            if (m.matches()) {
                return m.group(1);
            }
        }

        return null;
    }

    private void processContentBookPage(ContainerContent container, List<ContentInfo> newContent) {
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
                    WynntilsMod.warn("Problem querying Content Book for tracking: " + msg);
                    McUtils.sendMessageToClient(Component.literal("Setting tracking in Content Book failed")
                            .withStyle(ChatFormatting.RED));
                })

                // Open compass/character menu
                .then(QueryStep.useItemInHotbar(InventoryUtils.CONTENT_BOOK_SLOT_NUM)
                        .expectContainerTitle(Models.Content.CONTENT_BOOK_TITLE))

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
