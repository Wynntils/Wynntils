/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class UnsavedChangesWidget extends AbstractWidget {
    private final Button yesButton;
    private final Button noButton;

    public UnsavedChangesWidget(int x, int y, WynntilsBookSettingsScreen settingsScreen) {
        super(
                x,
                y,
                Texture.SETTINGS_WARNING_BACKGROUND.width(),
                Texture.SETTINGS_WARNING_BACKGROUND.height(),
                Component.literal("Unsaved Changes Widget"));

        yesButton = new Button.Builder(Component.literal("Yes"), (button -> settingsScreen.handleSaveChoice(true)))
                .pos(getX() + 10, getY() + 70)
                .size(60, 20)
                .build();

        noButton = new Button.Builder(Component.literal("No"), (button -> settingsScreen.handleSaveChoice(false)))
                .pos(getX() + getWidth() - 70, getY() + 70)
                .size(60, 20)
                .build();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.SETTINGS_WARNING_BACKGROUND, getX(), getY());

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics.pose(),
                        StyledText.fromComponent(Component.translatable("screens.wynntils.settingsScreen.saveChanges")
                                .withStyle(ChatFormatting.BOLD)),
                        getX() + 5,
                        getX() + getWidth() - 10,
                        getY() + 30,
                        getY() + getHeight() - 30,
                        Texture.SETTINGS_WARNING_BACKGROUND.width() - 10,
                        CommonColors.RED,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL,
                        1.25f);

        yesButton.render(guiGraphics, mouseX, mouseY, partialTick);
        noButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (yesButton.isMouseOver(mouseX, mouseY)) {
            return yesButton.mouseClicked(mouseX, mouseY, button);
        } else if (noButton.isMouseOver(mouseX, mouseY)) {
            return noButton.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
