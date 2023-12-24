/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.skillpointloadouts.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.skillpoint.SavableSkillPointSet;
import com.wynntils.screens.skillpointloadouts.SkillPointLoadoutsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class LoadoutWidget extends AbstractWidget {
    private final float dividedWidth;
    private final String name;
    private final SavableSkillPointSet loadout;
    private final SkillPointLoadoutsScreen parent;

    public LoadoutWidget(
            int x,
            int y,
            int width,
            int height,
            float dividedWidth,
            String name,
            SavableSkillPointSet loadout,
            SkillPointLoadoutsScreen parent) {
        super(x, y, width, height, Component.literal(name));
        this.dividedWidth = dividedWidth;
        this.name = name;
        this.loadout = loadout;
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        if (this.isMouseOver(mouseX, mouseY)) {
            RenderUtils.drawRect(
                    poseStack, CommonColors.GRAY.withAlpha(100), this.getX(), this.getY(), 0, width, height);
        }
        if (parent.getSelectedLoadout() != null
                && parent.getSelectedLoadout().key().equals(this.name)) {
            RenderUtils.drawRectBorders(
                    poseStack,
                    CommonColors.WHITE,
                    this.getX(),
                    this.getY(),
                    this.getX() + this.getWidth(),
                    this.getY() + this.getHeight(),
                    1,
                    0.5f);
        }

        int nameYOffset = 2;
        if (this.loadout.isBuild()) {
            nameYOffset = 3;
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(String.join(", ", loadout.getArmourNames())),
                            dividedWidth * 4,
                            this.getY() + ((float) this.getHeight() / 4 * 3),
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            0.8f);
        }
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(name + " (" + loadout.getSkillPointsSum() + ")"),
                        dividedWidth * 4,
                        this.getY() + (float) this.getHeight() / nameYOffset,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
        for (int i = 0; i < 5; i++) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(
                                    Skill.values()[i].getColorCode() + "" + loadout.getSkillPointsAsArray()[i]),
                            dividedWidth * (21 + i * 2),
                            this.getY() + (float) this.getHeight() / 2,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        parent.setSelectedLoadout(Pair.of(name, loadout));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
