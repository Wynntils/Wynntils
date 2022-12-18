/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.Configurable;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.Translatable;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.screens.WynntilsScreenWrapper;
import com.wynntils.gui.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import java.util.Optional;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ConfigurableButton extends AbstractButton {
    private final Configurable configurable;

    public ConfigurableButton(int x, int y, int width, int height, Configurable configurable) {
        super(x, y, width, height, Component.literal(((Translatable) configurable).getTranslatedName()));
        this.configurable = configurable;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor color = isHovered ? CommonColors.YELLOW : CommonColors.WHITE;

        Optional<WynntilsBookSettingsScreen> screen =
                WynntilsScreenWrapper.instanceOf(WynntilsBookSettingsScreen.class);
        if (screen.isPresent()) {
            if (screen.get().getSelectedFeature() == configurable) {
                color = CommonColors.GRAY;
            } else if (screen.get().getSelectedOverlay() == configurable) {
                color = CommonColors.GRAY;
            }
        }

        boolean isOverlay = configurable instanceof Overlay;

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        (isOverlay ? "   " : "") + ((Translatable) configurable).getTranslatedName(),
                        this.getX(),
                        this.getY(),
                        color,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.NORMAL);
    }

    @Override
    public void onPress() {
        Optional<WynntilsBookSettingsScreen> screen =
                WynntilsScreenWrapper.instanceOf(WynntilsBookSettingsScreen.class);
        if (screen.isPresent()) {
            if (configurable instanceof Feature feature) {
                screen.get().setSelectedFeature(feature);
            } else if (configurable instanceof Overlay overlay) {
                screen.get().setSelectedOverlay(overlay);
            }
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
