/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.screens.TextboxScreen;
import com.wynntils.gui.widgets.SearchWidget;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.handleditems.WynnItem;
import com.wynntils.wynn.handleditems.WynnItemCache;
import com.wynntils.wynn.objects.SearchableContainerType;
import com.wynntils.wynn.utils.ContainerUtils;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class ContainerSearchFeature extends UserFeature {
    @Config
    public boolean filterInBank = true;

    @Config
    public boolean filterInGuildBank = true;

    @Config
    public boolean filterInGuildMemberList = true;

    @Config
    public CustomColor highlightColor = CommonColors.MAGENTA;

    private SearchWidget lastSearchWidget;
    private SearchableContainerType currentSearchableContainerType;
    private boolean autoSearching = false;

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        assert screen instanceof TextboxScreen; // done by mixin

        String title = ComponentUtils.getCoded(screen.getTitle());

        // This is screen.topPos and screen.leftPos, but they are not calculated yet when this is called
        int renderX = (screen.width - screen.imageWidth) / 2;
        int renderY = (screen.height - screen.imageHeight) / 2;

        SearchableContainerType searchableContainerType = getCurrentSearchableContainerType(title);
        if (searchableContainerType == null) return;

        currentSearchableContainerType = searchableContainerType;

        addSearchWidget(screen, renderX, renderY);
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        ItemStack item = e.getSlot().getItem();
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(item);
        if (wynnItemOpt.isEmpty()) return;

        boolean result = wynnItemOpt.get().getCache().get(WynnItemCache.SEARCHED_KEY);
        if (!result) return;

        RenderUtils.drawArc(highlightColor, e.getSlot().x, e.getSlot().y, 200, 1f, 6, 8);
    }

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent.Post event) {
        forceUpdateSearch();

        if (autoSearching && McUtils.mc().screen instanceof AbstractContainerScreen<?> abstractContainerScreen) {
            tryAutoSearch(abstractContainerScreen);
        }
    }

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent event) {
        forceUpdateSearch();
    }

    @SubscribeEvent
    public void onContainerClose(ContainerCloseEvent.Post event) {
        lastSearchWidget = null;
        currentSearchableContainerType = null;
        autoSearching = false;
    }

    @SubscribeEvent
    public void onInventoryKeyPress(InventoryKeyPressEvent event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_ENTER) return;
        if (lastSearchWidget == null
                || currentSearchableContainerType == null
                || currentSearchableContainerType.getNextItemSlot() == -1
                || !(McUtils.mc().screen instanceof AbstractContainerScreen<?> abstractContainerScreen)) return;

        autoSearching = true;
        matchItems(lastSearchWidget.getTextBoxInput(), abstractContainerScreen);

        tryAutoSearch(abstractContainerScreen);
    }

    private void tryAutoSearch(AbstractContainerScreen<?> abstractContainerScreen) {
        if (!autoSearching) return;

        String name = ComponentUtils.getCoded(abstractContainerScreen
                .getMenu()
                .getItems()
                .get(currentSearchableContainerType.getNextItemSlot())
                .getHoverName());

        if (!currentSearchableContainerType.getNextItemPattern().matcher(name).matches()) {
            autoSearching = false;
            return;
        }

        ContainerUtils.clickOnSlot(
                currentSearchableContainerType.getNextItemSlot(),
                abstractContainerScreen.getMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                abstractContainerScreen.getMenu().getItems());
    }

    private SearchableContainerType getCurrentSearchableContainerType(String title) {
        SearchableContainerType containerType = SearchableContainerType.getContainerType(title);

        if (containerType == SearchableContainerType.BANK && filterInBank) {
            return SearchableContainerType.BANK;
        }

        if (containerType == SearchableContainerType.GUILD_BANK && filterInGuildBank) {
            return SearchableContainerType.BANK;
        }

        if (containerType == SearchableContainerType.MEMBER_LIST && filterInGuildMemberList) {
            return SearchableContainerType.BANK;
        }

        return null;
    }

    private void addSearchWidget(AbstractContainerScreen<?> screen, int renderX, int renderY) {
        SearchWidget searchWidget = new SearchWidget(
                renderX + screen.imageWidth - 100, renderY - 20, 100, 20, s -> matchItems(s, screen), (TextboxScreen)
                        screen);

        if (lastSearchWidget != null) {
            searchWidget.setTextBoxInput(lastSearchWidget.getTextBoxInput());
        }

        lastSearchWidget = searchWidget;

        screen.addRenderableWidget(lastSearchWidget);
    }

    private void matchItems(String search, AbstractContainerScreen<?> screen) {
        search = search.toLowerCase(Locale.ROOT);

        NonNullList<ItemStack> playerItems = McUtils.inventory().items;
        for (ItemStack item : screen.getMenu().getItems()) {
            Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(item);
            if (wynnItemOpt.isEmpty()) return;
            if (playerItems.contains(item)) continue;

            String name = ComponentUtils.getUnformatted(item.getHoverName()).toLowerCase(Locale.ROOT);

            boolean filtered = !search.isEmpty() && name.contains(search) && item.getItem() != Items.AIR;
            wynnItemOpt.get().getCache().store(WynnItemCache.SEARCHED_KEY, filtered);
            if (filtered) {
                autoSearching = false;
            }
        }
    }

    private void forceUpdateSearch() {
        Screen screen = McUtils.mc().screen;
        if (lastSearchWidget != null && screen instanceof AbstractContainerScreen<?> abstractContainerScreen) {
            matchItems(lastSearchWidget.getTextBoxInput(), abstractContainerScreen);
        }
    }
}
