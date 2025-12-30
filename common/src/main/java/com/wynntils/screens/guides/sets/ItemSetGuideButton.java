/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.sets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ItemSetGuideButton extends WynntilsButton {
    private static final CustomColor BUTTON_COLOR = new CustomColor(181, 174, 151);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(121, 116, 101);

    private final SetInfo setInfo;

    private int equippedCount = 1;

    public ItemSetGuideButton(int x, int y, int width, int height, SetInfo setInfo) {
        super(x, y, width, height, Component.literal("Item Set Guide Button"));
        this.setInfo = setInfo;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawRect(
                poseStack,
                isHovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR,
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height);

        FontRenderer.getInstance()
                .renderScrollingText(
                        poseStack,
                        StyledText.fromString(setInfo.name()),
                        this.getX() + 2,
                        this.getY() + 1,
                        this.width - 3,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE,
                        1f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            equippedCount = Math.min(equippedCount + 1, setInfo.bonuses().size());
            return true;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            equippedCount = Math.max(1, equippedCount - 1);
            return true;
        }

        return false;
    }

    public SetInfo getSetInfo() {
        return setInfo;
    }

    public int getEquippedCount() {
        return equippedCount;
    }

    // Not called
    @Override
    public void onPress() {}
}
