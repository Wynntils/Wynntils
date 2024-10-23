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
import com.wynntils.utils.mc.StyledTextUtils;
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
    public final Config<Boolean> autoCancel = new Config<>(true);

    @Persisted
    public final Config<Boolean> hidePrompt = new Config<>(false);

    @RegisterKeyBind
    private final KeyBind quickSearchKeyBind = new KeyBind(
            "Quick Search TM",
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            InputConstants.Type.MOUSE,
            true,
            null,
            this::tryQuickSearch);

    // Test in TradeMarketQuickSearchFeature_TYPE_TO_CHAT_PATTERN
    private static final Pattern TYPE_TO_CHAT_PATTERN =
            Pattern.compile("^§5(\uE00A\uE002|\uE001) Type the .* or type 'cancel' to cancel:");

    // The TM is very peculiar...
    // 'Emerald Pouch' results in all Tiers of Pouches, to find a specific tier one needs to search only the tier.
    // e.g. "Emerald Pouch [Tier V]" results in nothing, while "[Tier V]" results in the desired Pouches.
    // (This is also the reason replaceFirst is used,
    // as "Emerald Pouch" would be the first match, and "[Tier V]" would not get removed.)

    // '\\[.*?\\]' Many items have square brackets at the end displaying some kind of information.
    // e.g. durability on crafted items [50%], uses on consumables [3/3], stars on materials and ingredients [✫✫✫]

    // '\\[\\uE000-\\uF8FF\\]' This range contains special chars used by Wynncraft.
    // e.g. The prof icon at the beginning of Gathering tools

    // '\\u2B21' Just the hexagon char "⬡" used for Shinies.
    // 'Unidentified \\u2B21' As of now there is no known way of matching unided shinies, shiny is the next best thing.
    private static final Pattern CUT_PATTERN =
            Pattern.compile("(Emerald Pouch|\\[.*?\\]|[\\uE000-\\uF8FF]+|Unidentified \\u2B21|\\u2B21)\\s*");

    // Similarly to the Pouches, the elemental/skill potions cannot be found by their full name (Other potions can).
    // If we are searching such a potion, we simply search the second part. '.' matches the icon and '\w+' the skill.
    // e.g. "Potion of ✤ Strength [2/2]" -> "Potion of ✤ Strength" -> "✤ Strength"
    private static final Pattern POTION_PATTERN = Pattern.compile("^Potion of (. \\w+)");

    // 'À' this char is behind all the items names and needs to be trimmed before we search.
    // We cannot use WynnUtils.normalizeBadString() as this messes up dungeon keys.
    // e.g. "CorruptedÀÀÀGalleon'sÀÀÀGraveyard KeyÀ" -> "CorruptedÀÀÀGalleon'sÀÀÀGraveyard Key"
    private static final Pattern EOL_PATTERN = Pattern.compile("À+$");

    private static final int SEARCH_SLOT = 47;
    private String searchQuery;
    private boolean inTradeMarket = false;
    private boolean inSearchChat = false;
    private boolean openChatWhenContainerClosed = false;
    private boolean quickSearching = false;
    private boolean instantSearching = false;

    @SubscribeEvent
    public void onScreenOpen(ScreenOpenedEvent.Post event) {
        openChatWhenContainerClosed = false;
        inTradeMarket = (Models.Container.getCurrentContainer() instanceof TradeMarketContainer);
        if (!inTradeMarket) return;

        quickSearching = false;
        inSearchChat = false;
    }

    @SubscribeEvent
    public void onScreenClosed(ScreenClosedEvent event) {
        if (!inSearchChat && !openChatWhenContainerClosed) return;

        if (inSearchChat && event.getScreen() instanceof ChatScreen) {
            if (autoCancel.get() && KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
                McUtils.sendChat("cancel");
            }
            inSearchChat = false;
        } else if (openChatWhenContainerClosed) {
            if (quickSearching) {
                McUtils.mc().setScreen(new ChatScreen(searchQuery));
                quickSearching = false;
            } else {
                McUtils.mc().setScreen(new ChatScreen(""));
            }
            openChatWhenContainerClosed = false;
            inSearchChat = true;
        }
    }

    // EventPriority.HIGH so that InventoryEmeraldCountFeature does not render.
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMenuClosed(MenuClosedEvent event) {
        if (!inTradeMarket) return;

        inTradeMarket = false;
        if (instantSearching) {
            event.setCanceled(true);
            instantSearching = false;
        }
    }

    // Lower priority so features such as TradeMarketPriceConversionFeature won't have the messaged be cancelled
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChatMessageReceive(ChatMessageReceivedEvent event) {
        if (!Models.WorldState.onWorld()) return;

        StyledText styledText =
                StyledTextUtils.unwrap(event.getOriginalStyledText()).stripAlignment();
        if (!styledText.matches(TYPE_TO_CHAT_PATTERN)) return;

        if (instantSearching) {
            event.setCanceled(true);
            McUtils.sendChat(searchQuery);
            return;
        }

        openChatWhenContainerClosed = true;
        if (hidePrompt.get()) {
            event.setCanceled(true);
        }
    }

    private void tryQuickSearch(Slot hoveredSlot) {
        if (!inTradeMarket || hoveredSlot == null || !hoveredSlot.hasItem()) return;

        if (instantSearch.get() != KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            instantSearching = true;
        } else {
            quickSearching = true;
        }
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
