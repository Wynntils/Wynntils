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
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.properties.ItemProperty;
import java.util.Locale;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ContainerSearchFeature extends UserFeature {
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

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        assert screen instanceof TextboxScreen; // done by mixin

        String title = ComponentUtils.getCoded(screen.getTitle());

        // This is screen.topPos and screen.leftPos, but they are not calculated yet when this is called
        int renderX = (screen.width - screen.imageWidth) / 2;
        int renderY = (screen.height - screen.imageHeight) / 2;

        if (filterInBank && BANK_PATTERN.matcher(title).matches()) {
            addSimpleSearchWidget(screen, renderX, renderY);
        }

        if (filterInGuildBank && GUILD_BANK_PATTERN.matcher(title).matches()) {
            addSimpleSearchWidget(screen, renderX, renderY);
        }

        if (filterInGuildMemberList && MEMBER_LIST_PATTERN.matcher(title).matches()) {
            addSimpleSearchWidget(screen, renderX, renderY);
        }
    }

    private void addSimpleSearchWidget(AbstractContainerScreen<?> screen, int renderX, int renderY) {
        screen.addRenderableWidget(new SearchWidget(
                renderX + screen.imageWidth - 100,
                renderY - 20,
                100,
                20,
                s -> highlightMatchingItems(s, screen),
                (TextboxScreen) screen));
    }

    private void highlightMatchingItems(String search, AbstractContainerScreen<?> screen) {
        search = search.toLowerCase(Locale.ROOT);

        for (ItemStack item : screen.getMenu().getItems()) {
            if (!(item instanceof WynnItemStack wynnItemStack)) continue;

            String name = ComponentUtils.getUnformatted(item.getHoverName()).toLowerCase(Locale.ROOT);

            boolean filtered = !search.equals("") && name.contains(search);

            wynnItemStack.getProperty(ItemProperty.SEARCH_OVERLAY).setSearched(filtered);
        }
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        ItemStack item = e.getSlot().getItem();

        if (!(item instanceof WynnItemStack wynnItemStack)) return;

        if (!wynnItemStack.getProperty(ItemProperty.SEARCH_OVERLAY).isSearched()) return;

        RenderUtils.drawArc(highlightColor, e.getSlot().x, e.getSlot().y, 200, 1f, 6, 8);
    }
}
