/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.trademarket;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.mc.event.InventoryMouseClickedEvent;
import com.wynntils.mc.event.MenuEvent.MenuClosedEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.trademarket.event.TradeMarketChatInputEvent;
import com.wynntils.models.trademarket.type.TradeMarketState;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.WynnUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ConfigCategory(Category.TRADEMARKET)
public class TradeMarketQuickSearchFeature extends Feature {
    @Persisted
    private final Config<Boolean> instantSearch = new Config<>(true);

    @Persisted
    private final Config<Boolean> autoCancel = new Config<>(true);

    @Persisted
    private final Config<Boolean> hidePrompt = new Config<>(false);

    @RegisterKeyBind
    private final KeyBind quickSearchKeyBind = KeyBindDefinition.QUICK_SEARCH_TRADE_MARKET.create(this::tryQuickSearch);

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

    // Dungeon keys need to preserve internal ÀÀÀ separators for TM matching
    private static final Pattern DUNGEON_KEY_PATTERN = Pattern.compile(".* KeyÀ*$");
    private static final Pattern TRAILING_KEY_PADDING_PATTERN = Pattern.compile("À+$");

    private static final int SEARCH_SLOT = 47;
    private String searchQuery;
    private boolean openChatWhenContainerClosed = false;
    private boolean quickSearching = false;
    private boolean instantSearchingSendChat = false;
    private boolean instantSearchingCloseMenu = false;
    private final List<Button> guessedGearList = new ArrayList<>();

    public TradeMarketQuickSearchFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.LITE, ConfigProfile.MINIMAL)
                .build());
    }

    @SubscribeEvent
    public void onScreenClosed(ScreenClosedEvent.Post event) {
        Screen oldScreen = event.getScreen();

        if (!guessedGearList.isEmpty()) {
            guessedGearList.clear();
        }

        if (Models.TradeMarket.inChatInput() && oldScreen instanceof ChatScreen) {
            if (autoCancel.get() && KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
                McUtils.sendChat("cancel");
            }
            return;
        }

        if (openChatWhenContainerClosed) {
            openChat();
            openChatWhenContainerClosed = false;
        }
    }

    // EventPriority.HIGH so that InventoryEmeraldCountFeature does not render.
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMenuClosed(MenuClosedEvent event) {
        if (!Models.TradeMarket.inTradeMarket()) return;

        if (instantSearchingCloseMenu) {
            event.setCanceled(true);
            instantSearchingCloseMenu = false;
        }
    }

    @SubscribeEvent
    public void onTradeMarketChatInput(TradeMarketChatInputEvent event) {
        if (event.getState() == TradeMarketState.SEARCH_CHAT_INPUT && instantSearchingSendChat) {
            event.setResponse(searchQuery);
            event.cancelChat();
            instantSearchingSendChat = false;
            return;
        }

        // If the GUI closed first, we can open the chat immediately, otherwise
        // tell the MenuClosedEvent listener to open it when the container closes.
        if (McUtils.screen() == null) {
            openChat();
        } else {
            openChatWhenContainerClosed = true;
        }

        if (hidePrompt.get()) {
            event.cancelChat();
        }
    }

    @SubscribeEvent
    public void onContainerRender(ContainerRenderEvent event) {
        if (!Models.TradeMarket.inTradeMarket()) return;

        if (guessedGearList.isEmpty()) return;

        guessedGearList.forEach(button -> button.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTicks()));
    }

    @SubscribeEvent
    public void onInventoryMouseClicked(InventoryMouseClickedEvent event) {
        if (!Models.TradeMarket.inTradeMarket()) return;

        if (guessedGearList.isEmpty()) return;

        guessedGearList.forEach(button -> {
            if (button.mouseClicked(event.getMouseButtonEvent(), event.isDoubleClick())) {
                event.setCanceled(true);
            }
        });
    }

    private void openChat() {
        if (quickSearching) {
            McUtils.openChatScreen(searchQuery);
            quickSearching = false;
        } else {
            McUtils.openChatScreen("");
        }
    }

    private void tryQuickSearch(Slot hoveredSlot) {
        if (!Models.TradeMarket.inTradeMarket() || hoveredSlot == null || !hoveredSlot.hasItem()) return;

        if (instantSearch.get() != KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            instantSearchingSendChat = true;
            instantSearchingCloseMenu = true;
        } else {
            quickSearching = true;
        }

        ItemStack itemStack = hoveredSlot.getItem();
        Optional<GearBoxItem> gearBoxItemOpt = Models.Item.asWynnItem(itemStack, GearBoxItem.class);
        if (gearBoxItemOpt.isPresent()) {
            guessedGearList.clear();
            GearBoxItem gearBoxItem = gearBoxItemOpt.get();
            List<GearInfo> possibleGear = Models.Gear.getPossibleGears(gearBoxItem);
            if (possibleGear.isEmpty()) {
                WynntilsMod.warn("Tried Quick Searching gear box item " + gearBoxItem + ", but found no possible gear.");
                return;
            }
            Screen screen = McUtils.screen();
            Font font = McUtils.mc().font;
            final float scale = 1.25f;
            final int buttonHeight = Math.round(font.lineHeight * scale);
            final int yStart = screen.height / 2 - buttonHeight * possibleGear.size();
            for (int i = 0; i < possibleGear.size(); i++) {
                // TODO: check if can't fit vertically (leave some padding above and below) and then use multiple columns
                Component itemName = Component.literal(possibleGear.get(i).name());
                int buttonWidth = Math.round(font.width(itemName) * scale);
                guessedGearList.add(
                        new Button.Builder(itemName, this::onGuessGearPress)
                                .pos((screen.width - buttonWidth) / 2, yStart + buttonHeight * i)
                                .size(buttonWidth, buttonHeight)
                                .build()
                );
            }
            return;
        }

        searchQuery =
                StyledText.fromComponent(itemStack.getHoverName()).getStringWithoutFormatting();
        searchQuery = getSearchQuery(searchQuery);

        clickOnSearchSlot();
    }

    private String getSearchQuery(String rawName) {
        String searchTerm = CUT_PATTERN.matcher(rawName).replaceFirst("");
        Matcher potionMatcher = POTION_PATTERN.matcher(searchTerm);
        if (potionMatcher.matches()) {
            searchTerm = potionMatcher.group(1);
        } else if (DUNGEON_KEY_PATTERN.matcher(searchTerm).matches()) {
            searchTerm = TRAILING_KEY_PADDING_PATTERN.matcher(searchTerm).replaceAll("");
        } else {
            searchTerm = WynnUtils.stripItemNameMarkers(searchTerm);
        }

        searchTerm = searchTerm.trim();

        WynntilsMod.info("Quick Searching: " + rawName + " -> " + searchTerm);
        return searchTerm;
    }

    private void onGuessGearPress(Button button) {
        guessedGearList.clear();
        searchQuery = button.getMessage().getString();
        clickOnSearchSlot();
    }

    private void clickOnSearchSlot() {
        if (searchQuery == null || searchQuery.isBlank()) return;
        ContainerUtils.clickOnSlot(
                SEARCH_SLOT,
                McUtils.containerMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                McUtils.containerMenu().getItems());
    }
}
