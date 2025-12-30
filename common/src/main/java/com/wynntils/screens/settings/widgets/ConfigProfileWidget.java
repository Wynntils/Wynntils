/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class ConfigProfileWidget extends WynntilsButton {
    private static final float MAX_SCALE = 1.05f;
    private static final float MIN_SCALE = 1f;

    private float currentScale = 1f;

    private final ConfigProfile profile;

    public ConfigProfileWidget(int x, int y, ConfigProfile profile) {
        super(x, y, 168, 210, Component.literal("Config Profile Widget"));
        this.profile = profile;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isHovered) {
            currentScale += 0.01f;
        } else {
            currentScale -= 0.01f;
        }

        currentScale = MathUtils.clamp(currentScale, MIN_SCALE, MAX_SCALE);

        PoseStack poseStack = guiGraphics.pose();
        float centerX = getX() + Texture.CONFIG_PROFILE_BACKGROUND.width() / 2f;
        float centerY = getY() + Texture.CONFIG_PROFILE_BACKGROUND.height() / 2f;

        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 0);
        poseStack.scale(currentScale, currentScale, 0);
        poseStack.translate(-centerX, -centerY, 0);

        boolean selectedProfile = profile == Managers.Config.getSelectedProfile();

        if (selectedProfile) {
            RenderUtils.drawRectBorders(
                    poseStack,
                    CommonColors.ORANGE,
                    getX(),
                    getY(),
                    getX() + Texture.CONFIG_PROFILE_BACKGROUND.width(),
                    getY() + Texture.CONFIG_PROFILE_BACKGROUND.height(),
                    1,
                    5);
        }

        RenderUtils.drawTexturedRect(poseStack, Texture.CONFIG_PROFILE_BACKGROUND, getX(), getY());

        // TODO: Background texture for profiles

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.literal(EnumUtils.toNiceString(profile))
                                .withStyle(Style.EMPTY.withFont(
                                        ResourceLocation.withDefaultNamespace("language/wynncraft")))),
                        getX() + Texture.CONFIG_PROFILE_BACKGROUND.width() / 2f,
                        getY() + 16,
                        selectedProfile ? CommonColors.PURPLE : CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE,
                        1.5f);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromComponent(Component.translatable(profile.getShortDescription())),
                        getX() + 8,
                        getX() + getWidth() - 16,
                        getY() + getHeight() - 80,
                        getY() + getHeight() - 20,
                        getWidth() - 24,
                        CommonColors.DARK_GRAY,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NONE);

        poseStack.popPose();

        if (isHovered) {
            McUtils.screen().setTooltipForNextRenderPass(Component.translatable(profile.getDescription()));
        }
    }

    public ConfigProfile getProfile() {
        return profile;
    }

    @Override
    public void onPress() {
        Managers.Config.setSelectedProfile(profile);
    }
}
