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
    @RegisterConfig
    public final Config<Boolean> invertScroll = new Config<>(false);

    @SubscribeEvent
    public void onInteract(MouseScrollEvent event) {
        Screen screen = McUtils.mc().screen;

        if (!(screen instanceof AbstractContainerScreen<?> gui)) return;

        boolean scrollUp = event.isScrollingUp() ^ invertScroll.get();

        Pair<Integer, Integer> slots = Models.Container.getScrollSlots(gui, scrollUp);

        if (slots == null) return;

        ContainerUtils.clickOnSlot(
                scrollUp ? slots.a() : slots.b(),
                gui.getMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                gui.getMenu().getItems());
    }
}
