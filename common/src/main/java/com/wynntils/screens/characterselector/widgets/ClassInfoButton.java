/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.characterselector.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.ClassInfo;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.characterselector.CharacterSelectorScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ClassInfoButton extends WynntilsButton {
    private final ClassInfo classInfo;
    private final CharacterSelectorScreen characterSelectorScreen;

    public ClassInfoButton(
            int x, int y, int width, int height, ClassInfo classInfo, CharacterSelectorScreen characterSelectorScreen) {
        super(x, y, width, height, Component.literal("Class Info Button"));
        this.classInfo = classInfo;
        this.characterSelectorScreen = characterSelectorScreen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.CHARACTER_BUTTON.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                0,
                this.isHovered || characterSelectorScreen.getSelected() == this
                        ? Texture.CHARACTER_BUTTON.height() / 2
                        : 0,
                Texture.CHARACTER_BUTTON.width(),
                Texture.CHARACTER_BUTTON.height() / 2,
                Texture.CHARACTER_BUTTON.width(),
                Texture.CHARACTER_BUTTON.height());

        poseStack.pushPose();
        poseStack.translate(this.getX() + this.width * 0.038f, this.getY() + this.height * 0.12f, 0f);
        float itemScale = this.height * 0.03f;
        poseStack.scale(itemScale, itemScale, itemScale);
        RenderUtils.renderItem(guiGraphics, classInfo.itemStack(), 0, 0);
        poseStack.popPose();

        float textScale = this.height * 0.032f;

        FontRenderer.getInstance()
                .renderScrollingText(
                        poseStack,
                        StyledText.fromString(classInfo.name()),
                        this.getX() + this.width * 0.25f,
                        this.getY() + this.height * 0.16f,
                        this.width - this.width * 0.288f,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE,
                        textScale);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString("Level " + classInfo.level()),
                        this.getX() + this.width * 0.25f,
                        this.getY() + this.height * 0.45f,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE,
                        textScale * 0.8f);

        RenderUtils.drawProgressBar(
                poseStack,
                Texture.XP_BAR,
                this.getX() + 5,
                this.getY() + this.height * 0.8f,
                this.getX() + 5 + this.width * 0.9f,
                this.getY() + this.height * 0.9f,
                0,
                0,
                Texture.XP_BAR.width(),
                Texture.XP_BAR.height(),
                classInfo.xp() / 100f);
    }

    @Override
    public void onPress() {
        if (characterSelectorScreen.getSelected() == this) {
            Models.CharacterSelection.playWithCharacter(classInfo.slot());
        }
    }

    @Override
    protected boolean isValidClickButton(int button) {
        // Every mouse button is valid
        return true;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }
}
