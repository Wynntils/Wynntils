/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.entries;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.screens.settings.WynntilsSettingsScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

public final class FeatureSettingWidget extends AbstractWidget {
    private static final CustomColor BORDER_COLOR = new CustomColor(86, 75, 61, 255);
    private static final CustomColor FOREGROUND_COLOR = new CustomColor(177, 152, 120, 255);

    private WynntilsSettingsScreen settingsScreen;

    public FeatureSettingWidget(int x, int y, int width, int height, WynntilsSettingsScreen settingsScreen) {
        super(x, y, width, height, new TextComponent("Feature Setting Widget"));

        this.settingsScreen = settingsScreen;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        poseStack.pushPose();
        poseStack.translate(this.x, this.y, 0);

        renderBackground(poseStack);

        Feature selectedFeature = settingsScreen.getSelectedFeature();
        if (selectedFeature == null) return;

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        selectedFeature.getTranslatedName(),
                        0,
                        this.width,
                        8,
                        this.width,
                        CommonColors.WHITE,
                        FontRenderer.TextAlignment.CENTER_ALIGNED,
                        FontRenderer.TextShadow.OUTLINE);

        poseStack.popPose();
    }

    private void renderBackground(PoseStack poseStack) {
        RenderUtils.drawRoundedRectWithBorder(
                poseStack, BORDER_COLOR, FOREGROUND_COLOR, 2, 2, 0, this.width - 4, this.height - 4, 2, 6, 8);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
