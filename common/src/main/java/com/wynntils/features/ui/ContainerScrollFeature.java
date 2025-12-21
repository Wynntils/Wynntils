/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.MouseScrollEvent;
import com.wynntils.models.containers.type.ScrollableContainerProperty;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.Optional;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.UI)
public class ContainerScrollFeature extends Feature {
    @Persisted
    private final Config<Boolean> invertScroll = new Config<>(false);

    public ContainerScrollFeature() {
        super(new ProfileDefault.Builder().disableFor(ConfigProfile.BLANK_SLATE).build());
    }

    @SubscribeEvent
    public void onInteract(MouseScrollEvent event) {
        Screen screen = McUtils.screen();

        if (!(screen instanceof AbstractContainerScreen<?> gui)) return;

        boolean scrollBack = event.isScrollingUp() ^ invertScroll.get();

        if (Models.Container.getCurrentContainer() instanceof ScrollableContainerProperty scrollableContainer) {
            Optional<Integer> slot = scrollableContainer.getScrollButton(gui, scrollBack);

            if (slot.isEmpty()) return;

            // Prevent the scroll from being handled by the game
            event.setCanceled(true);

            ContainerUtils.clickOnSlot(
                    slot.get(),
                    gui.getMenu().containerId,
                    GLFW.GLFW_MOUSE_BUTTON_LEFT,
                    gui.getMenu().getItems());
        }
    }
}
