/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.features.Configurable;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.network.chat.Component;

public class ConfigurableButton extends WynntilsButton {
    private final Configurable configurable;

    private final List<Component> descriptionTooltip;

    public ConfigurableButton(int x, int y, int width, int height, Configurable configurable) {
        super(x, y, width, height, Component.literal(configurable.getTranslatedName()));
        this.configurable = configurable;

        if (configurable instanceof Feature feature) {
            descriptionTooltip =
                    ComponentUtils.wrapTooltips(List.of(Component.literal(feature.getTranslatedDescription())), 150);
        } else {
            descriptionTooltip = List.of();
        }
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor color = isHovered ? CommonColors.YELLOW : CommonColors.WHITE;

        if (McUtils.mc().screen instanceof WynntilsBookSettingsScreen bookSettingsScreen) {
            if (bookSettingsScreen.getSelected() == configurable) {
                color = CommonColors.GRAY;
            }
        }

        boolean isOverlay = configurable instanceof Overlay;

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString((isOverlay ? "   " : "") + configurable.getTranslatedName()),
                        this.getX(),
                        this.getY(),
                        color,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        if (isHovered && configurable instanceof Feature) {
            RenderUtils.drawTooltipAt(
                    poseStack,
                    mouseX,
                    mouseY,
                    0,
                    descriptionTooltip,
                    FontRenderer.getInstance().getFont(),
                    false);
        }
    }

    @Override
    public void onPress() {
        if (McUtils.mc().screen instanceof WynntilsBookSettingsScreen bookSettingsScreen) {
            bookSettingsScreen.setSelected(configurable);
        }
    }
}
