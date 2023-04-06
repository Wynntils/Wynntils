/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
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
        this.title = StyledText.fromComponent(title).withoutFormatting();
        this.tooltip = tooltip;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
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
                        StyledText.fromString(title),
                        this.getX(),
                        this.getX() + this.width,
                        this.getY(),
                        this.getY() + this.height,
                        0,
                        getTextColor(isHovered),
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
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

    protected CustomColor getTextColor(boolean isHovered) {
        return isHovered ? CommonColors.YELLOW : CommonColors.WHITE;
    }

    @Override
    public void onPress() {
        onClick.run();
    }
}
