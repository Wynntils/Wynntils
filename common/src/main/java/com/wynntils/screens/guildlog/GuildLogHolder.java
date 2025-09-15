/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guildlog;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.wrappedscreen.WrappedScreenHolder;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.guild.type.GuildLogType;
import com.wynntils.models.items.items.gui.GuildLogItem;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class GuildLogHolder extends WrappedScreenHolder<GuildLogScreen> {
    private static final Pattern TITLE_PATTERN = Pattern.compile(".+'s? Log: (.+)");
    public static final int BACK_BUTTON_SLOT = 0;
    // If a change is not loaded after this delay, force the next step
    private static final int FORCED_LOAD_DELAY = 20;
    private static final int LOGS_PER_PAGE = 32;
    private static final int NEXT_PAGE_SLOT = 45;
    private static final int REQUEST_TIMEOUT = 5;

    protected static final Map<GuildLogType, Integer> LOG_SLOTS_MAP = Map.of(
            GuildLogType.GENERAL,
            2,
            GuildLogType.OBJECTIVES,
            3,
            GuildLogType.WARS,
            4,
            GuildLogType.ECONOMY,
            5,
            GuildLogType.PUBLIC_BANK,
            6,
            GuildLogType.HIGH_RANKED_BANK,
            7);

    public List<GuildLogItem> guildLogItems = new ArrayList<>();

    private GuildLogScreen wrappedScreen;

    private int loadedLogs = 0;
    private long nextRequestTicks;
    private long lastItemLoadedTicks;

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent.Post event) {
        if (event.getContainerId() != wrappedScreen.getWrappedScreenInfo().containerId()) return;

        ItemStack itemStack = event.getItemStack();
        Optional<GuildLogItem> logItemOpt = Models.Item.asWynnItem(itemStack, GuildLogItem.class);

        if (logItemOpt.isPresent()) {
            loadedLogs++;

            guildLogItems.add(logItemOpt.get());

            lastItemLoadedTicks = McUtils.player().tickCount;

            wrappedScreen.updateLogItems();

            if (loadedLogs >= LOGS_PER_PAGE) {
                nextRequestTicks = McUtils.player().tickCount + REQUEST_TIMEOUT;
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!hasNextPage()) return;

        if (McUtils.player().tickCount >= lastItemLoadedTicks + FORCED_LOAD_DELAY) {
            // Force the next page load
            WynntilsMod.warn("Forcing the next page load, as the previous one did not finish properly.");
            nextRequestTicks = McUtils.player().tickCount;
            lastItemLoadedTicks = Integer.MAX_VALUE;
        }

        if (McUtils.player().tickCount < nextRequestTicks) return;

        ContainerUtils.clickOnSlot(
                NEXT_PAGE_SLOT,
                wrappedScreen.getWrappedScreenInfo().containerId(),
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                wrappedScreen.getWrappedScreenInfo().containerMenu().getItems());

        loadedLogs = 0;
        nextRequestTicks = Integer.MAX_VALUE;
    }

    private boolean hasNextPage() {
        NonNullList<ItemStack> items =
                wrappedScreen.getWrappedScreenInfo().containerMenu().getItems();
        return !items.get(NEXT_PAGE_SLOT).isEmpty() && loadedLogs >= LOGS_PER_PAGE;
    }

    @Override
    protected Pattern getReplacedScreenTitlePattern() {
        return TITLE_PATTERN;
    }

    @Override
    protected GuildLogScreen createWrappedScreen(WrappedScreenInfo wrappedScreenInfo) {
        return new GuildLogScreen(wrappedScreenInfo, this);
    }

    @Override
    protected void setWrappedScreen(GuildLogScreen wrappedScreen) {
        this.wrappedScreen = wrappedScreen;
    }

    @Override
    protected void reset() {
        loadedLogs = 0;
        guildLogItems = new ArrayList<>();
        nextRequestTicks = Integer.MAX_VALUE;
    }
}
