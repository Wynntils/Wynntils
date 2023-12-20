/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.skillpointloadouts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.screens.base.WynntilsGridLayoutScreen;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.skillpointloadouts.widgets.LoadoutWidget;
import com.wynntils.utils.colors.CommonColors;
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
        loadouts.put(
                "Test loadout",
                Map.of(
                        Skill.AGILITY,
                        1,
                        Skill.STRENGTH,
                        2,
                        Skill.DEFENCE,
                        3,
                        Skill.DEXTERITY,
                        4,
                        Skill.INTELLIGENCE,
                        5));
        loadouts.put(
                "large loadout name oaogkagokagkagk",
                Map.of(
                        Skill.AGILITY,
                        45,
                        Skill.STRENGTH,
                        15,
                        Skill.DEFENCE,
                        34,
                        Skill.DEXTERITY,
                        14,
                        Skill.INTELLIGENCE,
                        105));
        loadouts.put(
                "Test loadout 2",
                Map.of(
                        Skill.AGILITY,
                        1,
                        Skill.STRENGTH,
                        100,
                        Skill.DEFENCE,
                        3,
                        Skill.DEXTERITY,
                        4,
                        Skill.INTELLIGENCE,
                        5));
        loadouts.put(
                "asdf asdf ",
                Map.of(
                        Skill.AGILITY,
                        1,
                        Skill.STRENGTH,
                        2,
                        Skill.DEFENCE,
                        3,
                        Skill.DEXTERITY,
                        4,
                        Skill.INTELLIGENCE,
                        5));
        for (int i = 0; i < loadouts.size(); i++) {
            loadoutWidgets.add(new LoadoutWidget(
                    (int) (dividedWidth * 4),
                    (int) (dividedHeight * (9 + i * 4)),
                    (int) (dividedWidth * 24),
                    (int) (dividedHeight * 4),
                    dividedWidth,
                    loadouts.keySet().toArray()[i].toString(),
                    loadouts.get(loadouts.keySet().toArray()[i].toString())));
        }

        addRenderableWidget(
                new WynntilsButton(
                        (int) (dividedWidth * 32),
                        (int) (dividedHeight * 32),
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
                        (int) (dividedWidth * 40), (int) (dividedHeight * 32), 30, BUTTON_HEIGHT, Component.literal("Clear")) {
                    @Override
                    public void onPress() {
                        Models.SkillPoint.clearCurrentPoints();
                    }
                });
        addRenderableWidget(
                new WynntilsButton(
                        (int) (dividedWidth * 44),
                        (int) (dividedHeight * 32),
                        150,
                        BUTTON_HEIGHT,
                        Component.literal("clear saved loadouts")) {
                    @Override
                    public void onPress() {
                        Models.SkillPoint.getLoadouts().clear();
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
                dividedWidth * 28 - dividedWidth * 4,
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
                            dividedWidth * (19 + i * 2),
                            dividedHeight * 8,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }
        // endregion

        for (int i = 0; i < 5; i++) {
            Skill skill = Skill.values()[i];
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(String.valueOf(Models.SkillPoint.getTotalSkillPoints(skill))),
                            dividedWidth * 4,
                            dividedHeight * (40 + i),
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(String.valueOf(Models.SkillPoint.getGearSkillPoints(skill))),
                            dividedWidth * 6,
                            dividedHeight * (40 + i),
                            CommonColors.AQUA,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(String.valueOf(Models.SkillPoint.getCraftedSkillPoints(skill))),
                            dividedWidth * 8,
                            dividedHeight * (40 + i),
                            CommonColors.DARK_AQUA,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(String.valueOf(Models.SkillPoint.getTomeSkillPoints(skill))),
                            dividedWidth * 10,
                            dividedHeight * (40 + i),
                            CommonColors.YELLOW,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(String.valueOf(Models.SkillPoint.getAssignedSkillPoints(skill))),
                            dividedWidth * 12,
                            dividedHeight * (40 + i),
                            CommonColors.GREEN,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }

        loadoutWidgets.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }
}
