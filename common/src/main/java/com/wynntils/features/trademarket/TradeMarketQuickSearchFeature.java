package com.wynntils.features.trademarket;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
//import com.wynntils.mc.event.*;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.event.MenuEvent.MenuClosedEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.models.containers.containers.TradeMarketContainer;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.WynnUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import org.lwjgl.glfw.GLFW;
import java.util.Optional;
import java.util.regex.Pattern;

@ConfigCategory(Category.TRADEMARKET)
public class TradeMarketQuickSearchFeature extends Feature {

    @RegisterKeyBind
    private final KeyBind quickSearchKeyBind = new KeyBind(
            "Quick Search", GLFW.GLFW_MOUSE_BUTTON_MIDDLE, true, null, this::tryQuickSearch);
    private static final Pattern TYPE_TO_CHAT_PATTERN = Pattern.compile(
            "^§5(\uE00A\uE002|\uE001) \n\uE001 Type the .* or type (\n\uE001 'cancel' to|'cancel' to \n\uE001) cancel:\n\uE001 ");
    private static final int SEARCH_SLOT = 47;
    private String itemName;
    private boolean inTradeMarket = false;
    private boolean quickSearching = false;

    @SubscribeEvent
    public void onScreenOpen(ScreenOpenedEvent.Post event) {
        if (!(Models.Container.getCurrentContainer() instanceof TradeMarketContainer)) {
            inTradeMarket = false;
            quickSearching = false;
            return;
        }
        inTradeMarket = true;
        WynntilsMod.info("TM Opened!");
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
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
        itemName = WynnUtils.normalizeBadString(
                StyledText.fromComponent((hoveredSlot.getItem().getHoverName()))
                        .getStringWithoutFormatting());
        if (itemName == null || itemName.isBlank()) return;
        WynntilsMod.info(itemName);
        ContainerUtils.clickOnSlot(
                SEARCH_SLOT,
                McUtils.containerMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                McUtils.containerMenu().getItems());
    }

    @SubscribeEvent
    public void onChatMessageReceive(ChatMessageReceivedEvent event) {
        if (!quickSearching) return;
        if (event.getOriginalStyledText().stripAlignment().matches(TYPE_TO_CHAT_PATTERN)) {
            event.setCanceled(true);
            McUtils.sendChat(itemName);
        }
    }

   // private String getSearchTrem()
}