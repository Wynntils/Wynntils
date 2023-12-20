/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.skillpointloadouts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.WynntilsGridLayoutScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.skillpointloadouts.widgets.LoadoutWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class SkillPointLoadoutsScreen extends WynntilsGridLayoutScreen {
    private final List<LoadoutWidget> loadoutWidgets = new ArrayList<>();
    private TextInputBoxWidget saveNameInput;
    private WynntilsButton saveAssignedButton;
    private WynntilsButton saveBuildButton;

    private SkillPointLoadoutsScreen() {
        super(Component.literal("Skill Point Loadouts Screen"));
    }

    public static Screen create() {
        return new SkillPointLoadoutsScreen();
    }

    @Override
    protected void doInit() {
        super.doInit();

        loadoutWidgets.clear();
        Map<String, Map<Skill, Integer>> loadouts = Models.SkillPoint.getLoadouts();

        for (int i = 0; i < loadouts.size(); i++) {
            loadoutWidgets.add(new LoadoutWidget(
                    (int) (dividedWidth * 4),
                    (int) (dividedHeight * (9 + i * 4)),
                    (int) (dividedWidth * 26),
                    (int) (dividedHeight * 4),
                    dividedWidth,
                    loadouts.keySet().toArray()[i].toString(),
                    loadouts.get(loadouts.keySet().toArray()[i].toString())));
        }

        saveNameInput = new TextInputBoxWidget(
                (int) (dividedWidth * 35),
                (int) (dividedHeight * 24),
                (int) ((dividedWidth * 48) - (dividedWidth * 35)) - 1,
                BUTTON_HEIGHT,
                (x) -> {
                    saveAssignedButton.active = !x.isBlank();
                    saveBuildButton.active = !x.isBlank();
                },
                this,
                saveNameInput);
        this.addRenderableWidget(saveNameInput);

        saveAssignedButton = new WynntilsButton(
                (int) (dividedWidth * 48) + 1,
                (int) (dividedHeight * 24),
                (int) ((dividedWidth * 54) - (dividedWidth * 48)) - 1,
                BUTTON_HEIGHT,
                Component.translatable("screens.wynntils.skillPointLoadouts.saveAssigned")) {
            @Override
            public void onPress() {
                Models.SkillPoint.saveCurrentLoadout(saveNameInput.getTextBoxInput());
            }
        };
        this.addRenderableWidget(saveAssignedButton);
        saveAssignedButton.active = false;

        saveBuildButton = new WynntilsButton(
                (int) (dividedWidth * 54) + 1,
                (int) (dividedHeight * 24),
                (int) ((dividedWidth * 59) - (dividedWidth * 54)),
                BUTTON_HEIGHT,
                Component.translatable("screens.wynntils.skillPointLoadouts.saveBuild")) {
            @Override
            public void onPress() {
                // TODO
                McUtils.sendErrorToClient("not implemented");
            }
        };
        this.addRenderableWidget(saveBuildButton);
        saveBuildButton.active = false;

        addRenderableWidget(
                new WynntilsButton(
                        (int) (dividedWidth * 4),
                        (int) (dividedHeight * 54),
                        120,
                        BUTTON_HEIGHT,
                        Component.literal("Refresh skill points")) {
                    @Override
                    public void onPress() {
                        Models.SkillPoint.populateSkillPoints();
                    }
                });
        addRenderableWidget(
                new WynntilsButton(
                        (int) (dividedWidth * 4), (int) (dividedHeight * 50), 30, BUTTON_HEIGHT, Component.literal("Clear")) {
                    @Override
                    public void onPress() {
                        Models.SkillPoint.clearCurrentPoints();
                    }
                });
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        PoseStack poseStack = guiGraphics.pose();

        // region Loadout headers
        RenderUtils.drawRect(
                poseStack,
                CommonColors.WHITE,
                dividedWidth * 4,
                dividedHeight * 8,
                0,
                dividedWidth * 30 - dividedWidth * 4,
                1);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.skillPointLoadouts.loadoutName")),
                        dividedWidth * 4,
                        dividedHeight * 8,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        for (int i = 0; i < 5; i++) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(Skill.values()[i].getColorCode() + Skill.values()[i].getSymbol()),
                            dividedWidth * (21 + i * 2),
                            dividedHeight * 8,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }
        // endregion

        // region Summary
        RenderUtils.drawRectBorders(poseStack, CommonColors.WHITE, dividedWidth * 34, dividedHeight * 8, dividedWidth * 60, dividedHeight * 28, 1, 1);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.skillPointLoadouts.summary")),
                        dividedWidth * 34,
                        dividedHeight * 8,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        for (int i = 0; i < 5; i++) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(Skill.values()[i].getColorCode() + Skill.values()[i].getSymbol()),
                            dividedWidth * (51 + i * 2),
                            dividedHeight * 8,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.skillPointLoadouts.assigned", Models.SkillPoint.getAssignedSum())),
                        dividedWidth * 35,
                        dividedHeight * 11,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        for (int i = 0; i < 5; i++) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(Skill.values()[i].getColorCode() + "" + Models.SkillPoint.getAssignedSkillPoints(Skill.values()[i])),
                            dividedWidth * (51 + i * 2),
                            dividedHeight * 11,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.skillPointLoadouts.gear", Models.SkillPoint.getGearSum())),
                        dividedWidth * 35,
                        dividedHeight * 13,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        for (int i = 0; i < 5; i++) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(Skill.values()[i].getColorCode() + "" + Models.SkillPoint.getGearSkillPoints(Skill.values()[i])),
                            dividedWidth * (51 + i * 2),
                            dividedHeight * 13,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.skillPointLoadouts.tomes", Models.SkillPoint.getTomeSum())),
                        dividedWidth * 35,
                        dividedHeight * 15,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        for (int i = 0; i < 5; i++) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(Skill.values()[i].getColorCode() + "" + Models.SkillPoint.getTomeSkillPoints(Skill.values()[i])),
                            dividedWidth * (51 + i * 2),
                            dividedHeight * 15,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.skillPointLoadouts.crafted", Models.SkillPoint.getCraftedSum())),
                        dividedWidth * 35,
                        dividedHeight * 17,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        for (int i = 0; i < 5; i++) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(Skill.values()[i].getColorCode() + "" + Models.SkillPoint.getCraftedSkillPoints(Skill.values()[i])),
                            dividedWidth * (51 + i * 2),
                            dividedHeight * 17,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.skillPointLoadouts.total", Models.SkillPoint.getTotalSum())),
                        dividedWidth * 35,
                        dividedHeight * 19,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        for (int i = 0; i < 5; i++) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(Skill.values()[i].getColorCode() + "" + Models.SkillPoint.getTotalSkillPoints(Skill.values()[i])),
                            dividedWidth * (51 + i * 2),
                            dividedHeight * 19,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }
        // endregion

        loadoutWidgets.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }
}
