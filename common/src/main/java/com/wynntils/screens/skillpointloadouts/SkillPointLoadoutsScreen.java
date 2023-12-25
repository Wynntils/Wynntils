/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.skillpointloadouts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.skillpoint.SavableSkillPointSet;
import com.wynntils.screens.base.WynntilsGridLayoutScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.skillpointloadouts.widgets.LoadoutWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
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

    private Pair<String, SavableSkillPointSet> selectedLoadout;
    private WynntilsButton loadButton;
    private WynntilsButton deleteButton;

    private SkillPointLoadoutsScreen() {
        super(Component.literal("Skill Point Loadouts Screen"));
    }

    public static Screen create() {
        return new SkillPointLoadoutsScreen();
    }

    @Override
    protected void doInit() {
        super.doInit();
        Models.SkillPoint.populateSkillPoints();

        populateLoadouts();

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

        saveAssignedButton =
                new WynntilsButton(
                        (int) (dividedWidth * 48) + 1,
                        (int) (dividedHeight * 24),
                        (int) ((dividedWidth * 54) - (dividedWidth * 48)) - 1,
                        BUTTON_HEIGHT,
                        Component.translatable("screens.wynntils.skillPointLoadouts.saveAssigned")) {
                    @Override
                    public void onPress() {
                        String name = saveNameInput.getTextBoxInput();
                        if (Models.SkillPoint.hasLoadout(name)) {
                            // todo deal with
                        }
                        Models.SkillPoint.saveCurrentSkillPoints(name);
                        populateLoadouts();
                        setSelectedLoadout(
                                new Pair<>(name, Models.SkillPoint.getLoadouts().get(name)));
                        saveNameInput.setTextBoxInput("");
                    }
                };
        this.addRenderableWidget(saveAssignedButton);
        saveAssignedButton.active = false;

        saveBuildButton =
                new WynntilsButton(
                        (int) (dividedWidth * 54) + 1,
                        (int) (dividedHeight * 24),
                        (int) ((dividedWidth * 59) - (dividedWidth * 54)),
                        BUTTON_HEIGHT,
                        Component.translatable("screens.wynntils.skillPointLoadouts.saveBuild")) {
                    @Override
                    public void onPress() {
                        String name = saveNameInput.getTextBoxInput();
                        if (Models.SkillPoint.hasLoadout(name)) {
                            // todo deal with
                        }
                        Models.SkillPoint.saveCurrentBuild(name);
                        populateLoadouts();
                        setSelectedLoadout(
                                new Pair<>(name, Models.SkillPoint.getLoadouts().get(name)));
                        saveNameInput.setTextBoxInput("");
                    }
                };
        this.addRenderableWidget(saveBuildButton);
        saveBuildButton.active = false;

        loadButton =
                new WynntilsButton(
                        (int) (dividedWidth * 35),
                        (int) (dividedHeight * 56),
                        (int) ((dividedWidth * 48) - (dividedWidth * 35)) - 1,
                        BUTTON_HEIGHT,
                        Component.translatable("screens.wynntils.skillPointLoadouts.load")) {
                    @Override
                    public void onPress() {
                        Models.SkillPoint.loadLoadout(selectedLoadout.key());
                    }
                };
        this.addRenderableWidget(loadButton);
        loadButton.active = false;
        loadButton.visible = false;

        deleteButton =
                new WynntilsButton(
                        (int) (dividedWidth * 48) + 1,
                        (int) (dividedHeight * 56),
                        (int) ((dividedWidth * 54) - (dividedWidth * 48)) - 1,
                        BUTTON_HEIGHT,
                        Component.translatable("screens.wynntils.skillPointLoadouts.delete")) {
                    @Override
                    public void onPress() {
                        Models.SkillPoint.deleteLoadout(selectedLoadout.key());
                        selectedLoadout = null;
                        loadButton.active = false;
                        deleteButton.active = false;
                        loadButton.visible = false;
                        deleteButton.visible = false;
                        populateLoadouts();
                    }
                };
        this.addRenderableWidget(deleteButton);
        deleteButton.active = false;
        deleteButton.visible = false;

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
        RenderUtils.drawRectBorders(
                poseStack,
                CommonColors.WHITE,
                dividedWidth * 34,
                dividedHeight * 8,
                dividedWidth * 60,
                dividedHeight * 28,
                1,
                1);
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
                        StyledText.fromString(I18n.get(
                                "screens.wynntils.skillPointLoadouts.assigned", Models.SkillPoint.getAssignedSum())),
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
                            StyledText.fromString(Skill.values()[i].getColorCode() + ""
                                    + Models.SkillPoint.getAssignedSkillPoints(Skill.values()[i])),
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
                        StyledText.fromString(
                                I18n.get("screens.wynntils.skillPointLoadouts.gear", Models.SkillPoint.getGearSum())),
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
                            StyledText.fromString(Skill.values()[i].getColorCode() + ""
                                    + Models.SkillPoint.getGearSkillPoints(Skill.values()[i])),
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
                        StyledText.fromString(
                                I18n.get("screens.wynntils.skillPointLoadouts.tomes", Models.SkillPoint.getTomeSum())),
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
                            StyledText.fromString(Skill.values()[i].getColorCode() + ""
                                    + Models.SkillPoint.getTomeSkillPoints(Skill.values()[i])),
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
                        StyledText.fromString(I18n.get(
                                "screens.wynntils.skillPointLoadouts.crafted", Models.SkillPoint.getCraftedSum())),
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
                            StyledText.fromString(Skill.values()[i].getColorCode() + ""
                                    + Models.SkillPoint.getCraftedSkillPoints(Skill.values()[i])),
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
                        StyledText.fromString(
                                I18n.get("screens.wynntils.skillPointLoadouts.total", Models.SkillPoint.getTotalSum())),
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
                            StyledText.fromString(Skill.values()[i].getColorCode() + ""
                                    + Models.SkillPoint.getTotalSkillPoints(Skill.values()[i])),
                            dividedWidth * (51 + i * 2),
                            dividedHeight * 19,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }
        // endregion

        // region Selected loadout
        if (selectedLoadout != null) {
            RenderUtils.drawRectBorders(
                    poseStack,
                    CommonColors.WHITE,
                    dividedWidth * 34,
                    dividedHeight * 34,
                    dividedWidth * 60,
                    dividedHeight * 60,
                    1,
                    1);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(selectedLoadout.key()),
                            dividedWidth * 34,
                            dividedHeight * 34,
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
                                dividedHeight * 34,
                                CommonColors.WHITE,
                                HorizontalAlignment.CENTER,
                                VerticalAlignment.BOTTOM,
                                TextShadow.NORMAL);
            }
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get(
                                    "screens.wynntils.skillPointLoadouts.assigned",
                                    selectedLoadout.value().getSkillPointsSum())),
                            dividedWidth * 35,
                            dividedHeight * 37,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
            for (int i = 0; i < 5; i++) {
                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                StyledText.fromString(Skill.values()[i].getColorCode() + ""
                                        + selectedLoadout.value().getSkillPointsAsArray()[i]),
                                dividedWidth * (51 + i * 2),
                                dividedHeight * 37,
                                CommonColors.WHITE,
                                HorizontalAlignment.CENTER,
                                VerticalAlignment.BOTTOM,
                                TextShadow.NORMAL);
            }
            if (selectedLoadout.value().isBuild()) {
                for (int i = 0; i < selectedLoadout.value().getArmourNames().size(); i++) {
                    String armour = selectedLoadout.value().getArmourNames().get(i);
                    FontRenderer.getInstance()
                            .renderText(
                                    poseStack,
                                    StyledText.fromString(armour),
                                    dividedWidth * 35,
                                    dividedHeight * (40 + i * 2),
                                    CommonColors.WHITE,
                                    HorizontalAlignment.LEFT,
                                    VerticalAlignment.BOTTOM,
                                    TextShadow.NORMAL);
                }
                for (int i = 0; i < selectedLoadout.value().getAccessoryNames().size(); i++) {
                    String accessory =
                            selectedLoadout.value().getAccessoryNames().get(i);
                    FontRenderer.getInstance()
                            .renderText(
                                    poseStack,
                                    StyledText.fromString(accessory),
                                    dividedWidth * 44,
                                    dividedHeight * (40 + i * 2),
                                    CommonColors.WHITE,
                                    HorizontalAlignment.LEFT,
                                    VerticalAlignment.BOTTOM,
                                    TextShadow.NORMAL);
                }
            }
        }
        // endregion

        loadoutWidgets.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        for (LoadoutWidget widget : loadoutWidgets) {
            if (widget.isMouseOver(mouseX, mouseY)) {
                widget.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }
        return super.doMouseClicked(mouseX, mouseY, button);
    }

    public void setSelectedLoadout(Pair<String, SavableSkillPointSet> loadout) {
        selectedLoadout = loadout;
        loadButton.active = true;
        deleteButton.active = true;
        loadButton.visible = true;
        deleteButton.visible = true;
    }

    public Pair<String, SavableSkillPointSet> getSelectedLoadout() {
        return selectedLoadout;
    }

    private void populateLoadouts() {
        loadoutWidgets.clear();
        Map<String, SavableSkillPointSet> loadouts = Models.SkillPoint.getLoadouts();
        for (Map.Entry<String, SavableSkillPointSet> entry : loadouts.entrySet()) {
            loadoutWidgets.add(new LoadoutWidget(
                    (int) (dividedWidth * 4),
                    (int) (dividedHeight * (9 + loadoutWidgets.size() * 4)),
                    (int) (dividedWidth * 26),
                    (int) (dividedHeight * 4),
                    dividedWidth,
                    entry.getKey(),
                    entry.getValue(),
                    this));
        }
    }
}
