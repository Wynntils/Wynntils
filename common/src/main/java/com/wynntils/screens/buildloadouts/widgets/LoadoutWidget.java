/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.abilitytree.type.AbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.screens.buildloadouts.type.Loadout;
import com.wynntils.screens.buildloadouts.type.LoadoutType;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class LoadoutWidget extends AbstractWidget {
    private final float dividedWidth;
    private final BuildLoadoutsScreen parent;
    private final List<String> gearNames = new ArrayList<>();
    private final Loadout loadout;

    public LoadoutWidget(
            int x, int y, int width, int height, float dividedWidth, Loadout loadout, BuildLoadoutsScreen parent) {
        super(x, y, width, height, Component.literal(loadout.name()));
        this.dividedWidth = dividedWidth;
        this.loadout = loadout;
        this.parent = parent;
        if (loadout.hasSkillPoints() && loadout.skillPoints().weapon() != null) {
            gearNames.add(loadout.skillPoints().weapon());
        }
        if (loadout.hasSkillPoints()) {
            gearNames.addAll(loadout.skillPoints().armourNames());
            gearNames.addAll(loadout.skillPoints().accessoryNames());
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isMouseOver(mouseX, mouseY)) {
            RenderUtils.drawRect(
                    guiGraphics, CommonColors.GRAY.withAlpha(100), this.getX(), this.getY(), width, height);
        }
        if (parent.getSelectedLoadout() != null
                && parent.getSelectedLoadout().name().equals(loadout.name())) {
            RenderUtils.drawRectBorders(
                    guiGraphics,
                    CommonColors.WHITE,
                    this.getX(),
                    this.getY(),
                    this.getX() + this.getWidth(),
                    this.getY() + this.getHeight(),
                    0.5f);
        }

        int nameYOffset = 2;
        if (loadout.hasSkillPoints() && loadout.skillPoints().isBuild()) {
            nameYOffset = 3;
            String text = RenderedStringUtils.getMaxFittingText(
                    String.join(", ", gearNames),
                    dividedWidth * 19,
                    FontRenderer.getInstance().getFont());
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(text),
                            dividedWidth * 4,
                            this.getY() + ((float) this.getHeight() / 4 * 3),
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            0.8f);
        }

        int displayLevel = loadout.type() == LoadoutType.ABILITY_TREE && loadout.hasAbilityTree()
                ? loadout.abilityTree().getDisplayLevel()
                : (loadout.hasSkillPoints() ? loadout.skillPoints().getMinimumCombatLevel() : 1);

        String levelColor =
                displayLevel > Models.CombatXp.getCombatLevel().current() ? ChatFormatting.RED.toString() : "";
        String levelText = levelColor
                + I18n.get("screens.wynntils.buildLoadouts.widgetLevelText", displayLevel)
                + ChatFormatting.WHITE;

        String displayText;
        if (loadout.type() == LoadoutType.ABILITY_TREE) {
            String archetypePart =
                    loadout.hasAbilityTree() && loadout.abilityTree().getMainArchetype() != null
                            ? " (" + loadout.abilityTree().getMainArchetype() + ")"
                            : "";
            displayText = loadout.name() + " " + ChatFormatting.AQUA + "[AT]" + ChatFormatting.WHITE + archetypePart
                    + " (" + levelText + ")";
        } else if (loadout.type() == LoadoutType.BUILD) {
            String archetypePart =
                    loadout.hasAbilityTree() && loadout.abilityTree().getMainArchetype() != null
                            ? " (" + loadout.abilityTree().getMainArchetype() + ")"
                            : "";
            displayText = loadout.name() + archetypePart + " (" + levelText + ")";
        } else if (loadout.type() == LoadoutType.SKILL_POINT) {
            displayText = loadout.name() + " " + ChatFormatting.YELLOW + "[SP]" + ChatFormatting.WHITE + " ("
                    + levelText + ")";
        } else {
            displayText = loadout.name() + " (" + levelText + ")";
        }

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(displayText),
                        dividedWidth * 4,
                        this.getY() + (float) this.getHeight() / nameYOffset,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (loadout.hasSkillPoints()) {
            for (int i = 0; i < 5; i++) {
                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromString(Skill.values()[i].getColorCode() + ""
                                        + loadout.skillPoints().getSkillPointsAsArray()[i]),
                                dividedWidth * (21 + i * 2),
                                this.getY() + (float) this.getHeight() / 2,
                                CommonColors.WHITE,
                                HorizontalAlignment.CENTER,
                                VerticalAlignment.MIDDLE,
                                TextShadow.NORMAL);
            }
        }

        if (this.isHovered) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        parent.setSelectedLoadout(loadout);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
