/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.TextShadow;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.widgets.WynntilsButton;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.ComponentUtils;
import java.util.List;
import net.minecraft.network.chat.Component;

public class GeneralSettingsButton extends WynntilsButton {
    private static final CustomColor BACKGROUND_COLOR = new CustomColor(98, 34, 8);
    private static final CustomColor HOVER_BACKGROUND_COLOR = new CustomColor(158, 52, 16);
    private final Runnable onClick;
    private final String title;
    private final List<Component> tooltip;

    public GeneralSettingsButton(
            int x, int y, int width, int height, Component title, Runnable onClick, List<Component> tooltip) {
        super(x, y, width, height, title);
        this.onClick = onClick;
        this.title = ComponentUtils.getUnformatted(title);
        this.tooltip = tooltip;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRoundedRectWithBorder(
                poseStack,
                CommonColors.BLACK,
                isHovered ? HOVER_BACKGROUND_COLOR : BACKGROUND_COLOR,
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                1,
                3,
                3);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        title,
                        this.getX(),
                        this.getX() + this.width,
                        this.getY(),
                        this.getY() + this.height,
                        0,
                        isHovered ? CommonColors.YELLOW : CommonColors.WHITE,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Middle,
                        TextShadow.OUTLINE);

        if (isHovered) {
            RenderUtils.drawTooltipAt(
                    poseStack,
                    mouseX,
                    mouseY,
                    0,
                    tooltip,
                    FontRenderer.getInstance().getFont(),
                    true);
        }
    }

    @Override
    public void onPress() {
        onClick.run();
    }
}
