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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

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
        // TODO populate loadoutWidgets from model or smth

        addRenderableWidget(new WynntilsButton(
                (int) dividedWidth * 32,
                (int) dividedHeight * 32,
                100,
                20,
                Component.literal("Refresh skill points")) {
                                @Override
                                public void onPress() {
                                    Models.SkillPoint.calculateGearSkillPoints();
                                    Models.SkillPoint.queryTomeSkillPoints();
                                }
                            }
        );
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
        // endregion

        for (int i = 0; i < 5; i++) {
            Skill skill = Skill.values()[i];
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(String.valueOf(Models.SkillPoint.getTotalSkillPoints(skill))),
                            dividedWidth * 4,
                            dividedHeight * (10 + i),
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(String.valueOf(Models.SkillPoint.getGearSkillPoints(skill))),
                            dividedWidth * 6,
                            dividedHeight * (10 + i),
                            CommonColors.RED,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(String.valueOf(Models.SkillPoint.getTomeSkillPoints(skill))),
                            dividedWidth * 8,
                            dividedHeight * (10 + i),
                            CommonColors.YELLOW,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(String.valueOf(Models.SkillPoint.getAssignedSkillPoints(skill))),
                            dividedWidth * 10,
                            dividedHeight * (10 + i),
                            CommonColors.GREEN,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }



        loadoutWidgets.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }
}
