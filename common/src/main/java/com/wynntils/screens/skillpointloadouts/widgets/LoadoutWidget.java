/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.skillpointloadouts.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.SavableSkillPointSet;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.screens.skillpointloadouts.SkillPointLoadoutsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
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
        if (loadout.isBuild()) {
            nameYOffset = 3;
            String text = RenderedStringUtils.getMaxFittingText(
                    String.join(
                            ", ", (loadout.armourNames().isEmpty() ? loadout.accessoryNames() : loadout.armourNames())),
                    this.getWidth(),
                    FontRenderer.getInstance().getFont());
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(text),
                            dividedWidth * 4,
                            this.getY() + ((float) this.getHeight() / 4 * 3),
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            0.8f);
        }
        // Renders "name (skillPointsSum - Level minLevel)". Level is red if minLevel is higher than current level.
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(name + " (" + loadout.getSkillPointsSum() + " - "
                                + (loadout.getMinimumCombatLevel()
                                                > Models.CombatXp.getCombatLevel()
                                                        .current()
                                        ? ChatFormatting.RED
                                        : "")
                                + I18n.get(
                                        "screens.wynntils.skillPointLoadouts.widgetLevelText",
                                        loadout.getMinimumCombatLevel())
                                + ChatFormatting.WHITE
                                + ")"),
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
