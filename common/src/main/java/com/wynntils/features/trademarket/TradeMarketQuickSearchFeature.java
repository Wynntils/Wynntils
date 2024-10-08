/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.trademarket;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.MenuEvent.MenuClosedEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.models.containers.containers.TradeMarketContainer;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.TRADEMARKET)
public class TradeMarketQuickSearchFeature extends Feature {
    @Persisted
    public final Config<Boolean> instantSearch = new Config<>(true);

    @Persisted
    public final Config<Boolean> hidePrompt = new Config<>(true);

    @Persisted
    public final Config<Boolean> autoCancel = new Config<>(true);

    @RegisterKeyBind
    private final KeyBind quickSearchKeyBind = new KeyBind(
            "Quick Search TM",
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            InputConstants.Type.MOUSE,
            true,
            null,
            this::tryQuickSearch);

    private static final Pattern TYPE_TO_CHAT_PATTERN = Pattern.compile(
            "^§5(\uE00A\uE002|\uE001) \n\uE001 Type the .* or type (\n\uE001 'cancel' to|'cancel' to \n\uE001) cancel:\n\uE001 ");
    // Maybe there is a better solution for this than regex, but the TM is very peculiar.
    // \\[.*?\\] Crafting stuff, \\[\\uE000-\\uF8FF\\] Gathering Tools, \\u2B21 Shiny
    private static final Pattern CUT_PATTERN =
            Pattern.compile("(Emerald Pouch|\\[.*?\\]|[\\uE000-\\uF8FF]+|\\u2B21)\\s*");

    private static final Pattern POTION_PATTERN = Pattern.compile("^Potion of (. \\w+)");
    private static final Pattern EOL_PATTERN = Pattern.compile("À+$");

    private static final int SEARCH_SLOT = 47;
    private String searchQuery;
    private boolean inTradeMarket = false;
    private boolean inSearchChat = false;
    private boolean quickSearching = false;

    @SubscribeEvent
    public void onScreenOpen(ScreenOpenedEvent.Post event) {
        if (!(Models.Container.getCurrentContainer() instanceof TradeMarketContainer)) {
            inTradeMarket = false;
            quickSearching = false;
            return;
        }
        inTradeMarket = true;
    }

    @SubscribeEvent
    public void onScreenClosed(ScreenClosedEvent event) {
        if (!inSearchChat || !(event.getScreen() instanceof ChatScreen)) return;
        if (autoCancel.get() && KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
            McUtils.sendChat("cancel");
        }
        inSearchChat = false;
    }

    // EventPriority.HIGH so that InventoryEmeraldCountFeature does not render.
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void MenuClosedEvent(MenuClosedEvent event) {
        if (!inTradeMarket) return;
        inTradeMarket = false;
        if (!quickSearching) return;
        event.setCanceled(true);
        quickSearching = false;
    }

    private void tryQuickSearch(Slot hoveredSlot) {
        if (!inTradeMarket || hoveredSlot == null || !hoveredSlot.hasItem()) return;
        quickSearching = true;
        searchQuery =
                StyledText.fromComponent((hoveredSlot.getItem().getHoverName())).getStringWithoutFormatting();
        searchQuery = getSearchQuery(searchQuery);
        if (searchQuery == null || searchQuery.isBlank()) return;
        ContainerUtils.clickOnSlot(
                SEARCH_SLOT,
                McUtils.containerMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                McUtils.containerMenu().getItems());
    }

    // EventPriority.HIGH so that this takes priority over TradeMarketAutoOpenChatFeature.hidePrompt.
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChatMessageReceive(ChatMessageReceivedEvent event) {
        if (!quickSearching || !event.getOriginalStyledText().stripAlignment().matches(TYPE_TO_CHAT_PATTERN)) return;
        if (!instantSearch.get() || KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            McUtils.mc().setScreen(new ChatScreen(searchQuery));
            if (hidePrompt.get()) {
                event.setCanceled(true);
            }
            inSearchChat = true;
        } else {
            event.setCanceled(true);
            McUtils.sendChat(searchQuery);
        }
    }

    private String getSearchQuery(String rawName) {
        String searchTerm = CUT_PATTERN.matcher(rawName).replaceFirst("");
        searchTerm = EOL_PATTERN.matcher(searchTerm).replaceFirst("").trim();
        Matcher potionMatcher = POTION_PATTERN.matcher(searchTerm);
        if (potionMatcher.matches()) {
            searchTerm = potionMatcher.group(1);
        }
        WynntilsMod.info("Quick Searching: " + rawName + " -> " + searchTerm);
        return searchTerm;
    }
}
