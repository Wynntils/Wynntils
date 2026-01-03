/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.components.Managers;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

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
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isHovered) {
            currentScale += 0.01f;
        } else {
            currentScale -= 0.01f;
        }

        currentScale = MathUtils.clamp(currentScale, MIN_SCALE, MAX_SCALE);

        float centerX = getX() + Texture.CONFIG_PROFILE_BACKGROUND.width() / 2f;
        float centerY = getY() + Texture.CONFIG_PROFILE_BACKGROUND.height() / 2f;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(centerX, centerY);
        guiGraphics.pose().scale(currentScale, currentScale);
        guiGraphics.pose().translate(-centerX, -centerY);

        boolean selectedProfile = profile == Managers.Config.getSelectedProfile();

        if (selectedProfile) {
            RenderUtils.drawRectBorders(
                    guiGraphics,
                    CommonColors.ORANGE,
                    getX(),
                    getY(),
                    getX() + Texture.CONFIG_PROFILE_BACKGROUND.width(),
                    getY() + Texture.CONFIG_PROFILE_BACKGROUND.height(),
                    5);
        }

        RenderUtils.drawTexturedRect(guiGraphics, Texture.CONFIG_PROFILE_BACKGROUND, getX(), getY());

        // TODO: Background texture for profiles

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal(EnumUtils.toNiceString(profile))
                                .withStyle(Style.EMPTY.withFont(new FontDescription.Resource(
                                        Identifier.withDefaultNamespace("language/wynncraft"))))),
                        getX() + Texture.CONFIG_PROFILE_BACKGROUND.width() / 2f,
                        getY() + 16,
                        selectedProfile ? CommonColors.PURPLE : CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE,
                        1.5f);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
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

        guiGraphics.pose().popMatrix();

        if (isHovered) {
            guiGraphics.setTooltipForNextFrame(Component.translatable(profile.getDescription()), mouseX, mouseY);
        }
    }

    public ConfigProfile getProfile() {
        return profile;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        Managers.Config.setSelectedProfile(profile);
    }
}
