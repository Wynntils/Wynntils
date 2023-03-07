/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Quaternionf;

@ConfigCategory(Category.UI)
public class CosmeticsPreviewFeature extends UserFeature {
    private static final String GEAR_MENU_TITLE = "Gear Skins Menu";
    private static final String GUILD_GEAR_MENU_TITLE = "Guild Cosmetics";

    @SubscribeEvent
    public void onContainerRender(ContainerRenderEvent event) {
        AbstractContainerScreen<?> screen = event.getScreen();
        String title = ComponentUtils.getUnformatted(screen.getTitle());

        if (title.equals(GEAR_MENU_TITLE) || title.equals(GUILD_GEAR_MENU_TITLE)) {
            InventoryScreen.renderEntityInInventory(
                    new PoseStack(),
                    screen.leftPos + screen.imageWidth + 20,
                    screen.topPos + screen.imageHeight / 2,
                    30,
                    new Quaternionf(),
                    new Quaternionf(),
                    McUtils.player());
        }
    }
}
