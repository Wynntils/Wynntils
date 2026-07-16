package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.utils.colors.CommonColors;
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

public class LoadoutMenuOverviewWidget extends AbstractWidget {
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;


    public LoadoutMenuOverviewWidget(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 98, 68, Component.literal("Loadout Menu Overview Widget"));
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

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_MENU_RIBBON,
                x + 6,
                y + 2,
                this.width - 12,
                17);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString("Overview"),
                        this.x + 5 + (this.width - 10) / 2f,
                        this.y + 11,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        int startY = 26;
        Loadout selectedLoadout = parent.getSelectedLoadout();
        if (selectedLoadout == null) return;

        if (selectedLoadout.hasClassType()) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(selectedLoadout.getClassType().getName()),
                            this.x + this.width / 2f,
                            this.y + startY,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
            startY += 11;
        }

        String archetype = selectedLoadout.getMainArchetype();
        if (archetype != null) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(archetype),
                            this.x + this.width / 2f,
                            this.y + startY,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
            startY += 11;
        }

        int nodeCount = selectedLoadout.getNodeCount();
        if (nodeCount > 0) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString("Nodes: " + nodeCount),
                            this.x + this.width / 2f,
                            this.y + startY,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
            startY += 11;
        }

        int level = selectedLoadout.getMaxLevel();
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString("Level: " + level),
                        this.x + this.width / 2f,
                        this.y + startY,
                        level > Models.CombatXp.getCombatLevel().current() ? parent.errorColor : CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
        startY += 11;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
