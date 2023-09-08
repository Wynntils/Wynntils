/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.tooltips;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.TOOLTIPS)
public class TooltipFittingFeature extends Feature {
    @Persisted
    public final Config<Float> universalScale = new Config<>(1f);

    @Persisted
    public final Config<Boolean> fitToScreen = new Config<>(true);

    @Persisted
    public final Config<Boolean> wrapText = new Config<>(true);

    private boolean scaledLast = false;
    private Screen currentScreen = null;
    private int oldWidth = -1;
    private int oldHeight = -1;

    // scaling should only happen after every other feature has updated tooltip
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltipPre(ItemTooltipRenderEvent.Pre e) {
        currentScreen = McUtils.mc().screen;
        if (currentScreen == null) return; // shouldn't be possible

        if (wrapText.get()) {
            // calculate optimal wrapping for scaled up tooltips
            int tooltipWidth = ComponentUtils.getOptimalTooltipWidth(
                    e.getTooltips(), (int) (currentScreen.width / universalScale.get()), (int)
                            (e.getMouseX() / universalScale.get()));
            List<Component> wrappedTooltips = ComponentUtils.wrapTooltips(e.getTooltips(), tooltipWidth);
            e.setTooltips(Collections.unmodifiableList(wrappedTooltips));
        }

        // calculate scale factor
        float scaleFactor = universalScale.get();

        if (fitToScreen.get()) {
            int lines = e.getTooltips().size();
            // this is technically slightly larger than the actual height, but due to the tooltip offset/border, it
            // works to create a nice buffer at the top/bottom of the screen
            float tooltipHeight = 22 + (lines - 1) * 10;
            tooltipHeight *= universalScale.get();

            if (tooltipHeight > currentScreen.height) scaleFactor *= (currentScreen.height / tooltipHeight);
        }

        // set new screen dimensions - this is done to avoid issues with tooltip offsets being wrong
        oldWidth = currentScreen.width;
        currentScreen.width = (int) (oldWidth / scaleFactor);
        oldHeight = currentScreen.height;
        currentScreen.height = (int) (oldHeight / scaleFactor);

        // scale mouse coordinates for same reason
        e.setMouseX((int) (e.getMouseX() / scaleFactor));
        e.setMouseY((int) (e.getMouseY() / scaleFactor));

        // push pose before scaling, so we can pop it afterwards
        PoseStack poseStack = e.getPoseStack();
        poseStack.pushPose();
        poseStack.scale(scaleFactor, scaleFactor, 1);

        scaledLast = true;
    }

    // highest priority to reset pose before other features start rendering
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTooltipPost(ItemTooltipRenderEvent.Post e) {
        if (!scaledLast) return;

        e.getPoseStack().popPose();
        scaledLast = false;

        // reset screen dimensions
        if (currentScreen != null) {
            currentScreen.width = oldWidth;
            currentScreen.height = oldHeight;
        }
    }
}
