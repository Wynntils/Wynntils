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

    public AutoExpandUseItems() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER, ConfigProfile.LITE)
                .build());
    }

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent.Post event) {
        if (!(Models.Container.getCurrentContainer() instanceof StoreContainer)) return;

        List<ItemStack> items = event.getItems();
        if (!StyledText.fromComponent(items.get(USE_ITEM_SLOT).getHoverName()).matches(USE_ITEM_PATTERN)) return;

        ContainerUtils.clickOnSlot(USE_ITEM_SLOT, event.getContainerId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, items);
    }
}
