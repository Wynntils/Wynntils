/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.models.character.type.ClassType;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
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

        setTooltip(Tooltip.create(Component.literal(buildFullTooltipText())));
        setTooltipDelay(Duration.ofMillis(500));
    }

    private String buildFullTooltipText() {
        StringBuilder tooltip = new StringBuilder(loadout.name());

        if (loadout.hasAbilityTree()) {
            ClassType classType = loadout.abilityTree().classType();
            if (classType != null && classType != ClassType.NONE) {
                tooltip.append(" (").append(classType.getName()).append(")");
            }
            String archetype = loadout.abilityTree().getMainArchetype();
            if (archetype != null) {
                tooltip.append(" - ").append(archetype);
            }
        }

        tooltip.append(" [Lv. ").append(loadout.getMaxLevel()).append("]");

        if (loadout.type() == LoadoutType.ABILITY_TREE) {
            tooltip.append(" [Ability Tree]");
        } else if (loadout.type() == LoadoutType.BUILD) {
            tooltip.append(" [Build]");
        } else if (loadout.type() == LoadoutType.SKILL_POINT) {
            tooltip.append(" [Skill Points]");
        } else if (loadout.type() == LoadoutType.ASPECT) {
            tooltip.append(" [Aspects]");
        }

        return tooltip.toString();
    }

    private StyledText truncateToVisibleLength(StyledText text, int maxVisibleChars) {
        if (maxVisibleChars <= 3) return text;

        int visibleLength = text.length(); // counts raw text, ignoring § codes
        if (visibleLength <= maxVisibleChars) {
            return text;
        }

        return text.substring(0, maxVisibleChars - 3, StyleType.NONE).append("...");
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

        int level = loadout.getMaxLevel();

        String levelColor = level > Models.CombatXp.getCombatLevel().current() ? ChatFormatting.RED.toString() : "";
        String levelText =
                levelColor + I18n.get("screens.wynntils.buildLoadouts.widgetLevelText", level) + ChatFormatting.WHITE;

        String classPart = "";
        if (loadout.hasAbilityTree()) {
            ClassType classType = loadout.abilityTree().classType();
            if (classType != null && classType != ClassType.NONE) {
                classPart = ChatFormatting.GRAY + " [" + classType.getName() + "]" + ChatFormatting.WHITE;
            }
        }

        StyledText displayText;
        if (loadout.type() == LoadoutType.ABILITY_TREE) {
            String archetypePart = loadout.hasAbilityTree()
                            && loadout.abilityTree().getMainArchetype() != null
                    ? ChatFormatting.WHITE + " (" + loadout.abilityTree().getMainArchetype() + ")"
                    : "";
            displayText = StyledText.fromString(loadout.name() + " " + ChatFormatting.AQUA + "[AT]" + archetypePart
                    + classPart + " (" + levelText + ")");
        } else if (loadout.type() == LoadoutType.BUILD) {
            String archetypePart =
                    loadout.hasAbilityTree() && loadout.abilityTree().getMainArchetype() != null
                            ? " (" + loadout.abilityTree().getMainArchetype() + ")"
                            : "";
            displayText = StyledText.fromString(loadout.name() + archetypePart + classPart + " (" + levelText + ")");
        } else if (loadout.type() == LoadoutType.SKILL_POINT) {
            displayText = StyledText.fromString(loadout.name() + " " + ChatFormatting.YELLOW + "[SP]" + classPart
                    + ChatFormatting.WHITE + " (" + levelText + ")");
        } else if (loadout.type() == LoadoutType.ASPECT) {
            String aspectClassPart = "";
            if (loadout.hasAspects()) {
                ClassType classType = loadout.aspect().classType();
                if (classType != null && classType != ClassType.NONE) {
                    aspectClassPart = ChatFormatting.GRAY + " [" + classType.getName() + "]" + ChatFormatting.WHITE;
                }
            }
            displayText = StyledText.fromString(loadout.name() + " " + ChatFormatting.GREEN + "[A]" + aspectClassPart
                    + ChatFormatting.WHITE + " (" + levelText + ")");
        } else {
            displayText = StyledText.fromString(loadout.name() + classPart + " (" + levelText + ")");
        }

        int maxChars = (loadout.type() == LoadoutType.ABILITY_TREE) ? 74 : 48;
        displayText = truncateToVisibleLength(displayText, maxChars);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        displayText,
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
                                (int) (dividedWidth * (23 + i * 1.5)),
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
