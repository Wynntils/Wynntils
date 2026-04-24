/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.models.containers.containers.StoreContainer;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.UI)
public class AutoExpandUseItems extends Feature {
    private static final int USE_ITEM_SLOT = 53;
    private static final Pattern USE_ITEM_PATTERN = Pattern.compile("§#a0c84bff§lUse Items(§f\uF001)?");

    private boolean shouldClick = false;

    public AutoExpandUseItems() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER, ConfigProfile.LITE)
                .build());
    }

    @SubscribeEvent
    public void onConnect(WorldStateEvent event) {
        shouldClick = event.getNewState() == WorldState.WORLD;
    }

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent.Post event) {
        /*
         * Due to clicking on the use items slot, the container changes and another ContainerSetContentEvent is
         * emitted. However, in that event the items are not updated so the use items item is detected. To avoid this
         * we use a flag that gets reset on world join.
         */
        if (!shouldClick) return;
        if (!(Models.Container.getCurrentContainer() instanceof StoreContainer)) return;

        List<ItemStack> items = event.getItems();
        if (items.size() <= USE_ITEM_SLOT + 1) return;
        if (!StyledText.fromComponent(items.get(USE_ITEM_SLOT).getHoverName()).matches(USE_ITEM_PATTERN)) return;

        shouldClick = false;
        ContainerUtils.clickOnSlot(USE_ITEM_SLOT, event.getContainerId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, items);
    }
}
