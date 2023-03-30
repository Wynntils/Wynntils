/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.MouseScrollEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.ContainerUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.UI)
public class ContainerScrollFeature extends Feature {
    private static final int abilityTreePreviousSlot = 57;
    private static final int abilityTreeNextSlot = 59;

    private static final int bankPreviousSlot = 17; // This works for Misc Bucket, Bank, and Block Bank
    private static final int bankNextSlot = 8;

    private static final int guildBankPreviousSlot = 9;
    private static final int guildBankNextSlot = 27;

    private static final int tradeMarketPreviousSlot = 17;
    private static final int tradeMarketNextSlot = 26;

    @RegisterConfig
    public final Config<Boolean> invertScroll = new Config<>(false);

    @SubscribeEvent
    public void onInteract(MouseScrollEvent event) {
        Screen screen = McUtils.mc().screen;

        if (!(screen instanceof AbstractContainerScreen<?> gui)) return;
        boolean previous = event.isScrollingUp() ^ invertScroll.get();

        Pair<Integer, Integer> slots;

        if (Models.Container.isAbilityTreeScreen(gui)) {
            slots = Models.Container.ABILITY_TREE_PREVIOUS_NEXT_SLOTS;
        } else if (Models.Container.isBankScreen(gui)
                || Models.Container.isMiscBucketScreen(gui)
                || Models.Container.isBlockBankScreen(gui)) {
            slots = Models.Container.BANK_PREVIOUS_NEXT_SLOTS;
            // Do not purchase new pages when scrolling
            if (!previous && Models.Container.isLastBankPage(screen)) return;
        } else if (Models.Container.isGuildBankScreen(gui)) {
            slots = Models.Container.GUILD_BANK_PREVIOUS_NEXT_SLOTS;
        } else if (Models.Container.isTradeMarketScreen(gui)) {
            slots = Models.Container.TRADE_MARKET_PREVIOUS_NEXT_SLOTS;
            // Do not click on "Reveal Item Names" on the first page of TM
            if (previous && Models.Container.isFirstTradeMarketPage(screen)) return;
        } else {
            return;
        }

        ContainerUtils.clickOnSlot(
                previous ? slots.a() : slots.b(),
                gui.getMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                gui.getMenu().getItems());
    }
}
