/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

@FeatureInfo(stability = Stability.STABLE, category = "Item Tooltips")
public class TooltipScaleFeature extends UserFeature {

    @Config
    public static float universalScale = 1f;

    @Config
    public static boolean fitToScreen = true;

    private boolean scaledLast = false;
    private Screen currentScreen = null;
    private int oldWidth = -1;
    private int oldHeight = -1;

    @SubscribeEvent
    public void onTooltipPre(ItemTooltipRenderEvent.Pre e) {
        if (!WynnUtils.onServer()) return;

        currentScreen = McUtils.mc().screen;
        if (currentScreen == null) return; // shouldn't be possible

        // calculate scale factor
        float scaleFactor = universalScale;

        if (fitToScreen) {
            int lines = e.getTooltips().size();
            // this is technically slightly larger than the actual height, but due to the tooltip offset/border, it
            // works to create a nice buffer at the top/bottom of the screen
            float tooltipHeight = 22 + (lines - 1) * 10;
            tooltipHeight *= universalScale;

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

        // if calculated after scaling, will result in text possibly going above screen
        // if calculated before scaling, will result in text leaving the screen on the sides
        int tooltipWidth = ComponentUtils.getOptimalTooltipWidth(currentScreen, e.getTooltips(), e.getMouseX());
        List<FormattedText> wrappedTooltips = ComponentUtils.wrapTooltips(e.getTooltips(), tooltipWidth);
        e.setTooltips(wrappedTooltips);

    }

    @SubscribeEvent
    public void onTooltipPost(ItemTooltipRenderEvent.Post e) {
        if (!WynnUtils.onServer()) return;
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
