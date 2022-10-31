/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.screens.TextboxScreen;
import com.wynntils.gui.widgets.SearchWidget;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.properties.ItemProperty;
import java.util.Locale;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HighlightMatchingItemsFeature extends UserFeature {
    private static final Pattern BANK_PATTERN = Pattern.compile("§0\\[Pg. (\\d+)\\] §8(.*)'s§0 Bank");
    private static final Pattern GUILD_BANK_PATTERN = Pattern.compile(".+: Bank \\(.+\\)");
    private static final Pattern MEMBER_LIST_PATTERN = Pattern.compile(".+: Members");

    @Config
    public boolean filterInBank = true;

    @Config
    public boolean filterInGuildBank = true;

    @Config
    public boolean filterInGuildMemberList = true;

    @Config
    public CustomColor highlightColor = CommonColors.MAGENTA;

    private SearchWidget lastSearchWidget;

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        assert screen instanceof TextboxScreen; // done by mixin

        String title = ComponentUtils.getCoded(screen.getTitle());

        // This is screen.topPos and screen.leftPos, but they are not calculated yet when this is called
        int renderX = (screen.width - screen.imageWidth) / 2;
        int renderY = (screen.height - screen.imageHeight) / 2;

        if (!shouldAddSearch(title)) return;

        addSearchWidget(screen, renderX, renderY);
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        ItemStack item = e.getSlot().getItem();

        if (!(item instanceof WynnItemStack wynnItemStack)) return;

        if (!wynnItemStack.getProperty(ItemProperty.SEARCH_OVERLAY).isSearched()) return;

        RenderUtils.drawArc(highlightColor, e.getSlot().x, e.getSlot().y, 200, 1f, 6, 8);
    }

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent event) {
        forceUpdateSearch();
    }

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent event) {
        forceUpdateSearch();
    }

    @SubscribeEvent
    public void onContainerClose(ContainerCloseEvent.Post event) {
        lastSearchWidget = null;
    }

    public boolean shouldAddSearch(String title) {
        if (filterInBank && BANK_PATTERN.matcher(title).matches()) {
            return true;
        }

        if (filterInGuildBank && GUILD_BANK_PATTERN.matcher(title).matches()) {
            return true;
        }

        if (filterInGuildMemberList && MEMBER_LIST_PATTERN.matcher(title).matches()) {
            return true;
        }

        return false;
    }

    private void addSearchWidget(AbstractContainerScreen<?> screen, int renderX, int renderY) {
        SearchWidget searchWidget = new SearchWidget(
                renderX + screen.imageWidth - 100,
                renderY - 20,
                100,
                20,
                s -> highlightMatchingItems(s, screen),
                (TextboxScreen) screen);

        if (lastSearchWidget != null) {
            searchWidget.setTextBoxInput(lastSearchWidget.getTextBoxInput());
        }

        lastSearchWidget = searchWidget;

        screen.addRenderableWidget(lastSearchWidget);
    }

    private void highlightMatchingItems(String search, AbstractContainerScreen<?> screen) {
        search = search.toLowerCase(Locale.ROOT);

        for (ItemStack item : screen.getMenu().getItems()) {
            if (!(item instanceof WynnItemStack wynnItemStack)) continue;

            String name = ComponentUtils.getUnformatted(item.getHoverName()).toLowerCase(Locale.ROOT);

            boolean filtered = !search.equals("") && name.contains(search) && item.getItem() != Items.AIR;

            wynnItemStack.getProperty(ItemProperty.SEARCH_OVERLAY).setSearched(filtered);
        }
    }

    private void forceUpdateSearch() {
        Screen screen = McUtils.mc().screen;
        if (lastSearchWidget != null && screen instanceof AbstractContainerScreen<?> abstractContainerScreen) {
            highlightMatchingItems(lastSearchWidget.getTextBoxInput(), abstractContainerScreen);
        }
    }
}
