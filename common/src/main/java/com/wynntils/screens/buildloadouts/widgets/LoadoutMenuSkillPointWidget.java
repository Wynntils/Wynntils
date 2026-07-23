package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class LoadoutMenuSkillPointWidget extends AbstractWidget implements IconRenderer {
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;


    public LoadoutMenuSkillPointWidget(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 61, 68, Component.literal("Loadout Menu Skill Point Widget"));
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT,
                x,
                y,
                this.width,
                this.height);

        Loadout selectedLoadout = parent.getSelectedLoadout();
        if (selectedLoadout != null && selectedLoadout.hasSkillPoints()) {
            int startY = 10;

            for (int i = 0; i < 5; i++) {
                int points = selectedLoadout.skillPoints().getSkillPointsAsArray()[i];
                renderSkillIcon(guiGraphics, this.x + 10, this.y - 6 + startY, i);

                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromString(String.valueOf(points)),
                                this.x + 30,
                                this.y + startY,
                                CustomColor.fromInt(Skill.values()[i].getColorCode().getColor()),
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.MIDDLE,
                                TextShadow.NORMAL);
                startY += 12;
            }
        }

        if (selectedLoadout != null && !selectedLoadout.hasSkillPoints()) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            StyledText.fromComponent(Component.translatable("screens.wynntils.buildLoadouts.loadoutMenu.skillPointWidget.emptyText")),
                            this.x + this.width / 2f,
                            this.y + 5,
                            this.y + this.height - 10,
                            this.width - 10,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }

    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
