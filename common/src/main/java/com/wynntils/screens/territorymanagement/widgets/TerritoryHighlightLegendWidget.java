/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.widgets;

import com.wynntils.core.components.Managers;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.ui.CustomTerritoryManagementScreenFeature;
import com.wynntils.models.territories.type.TerritoryConnectionType;
import com.wynntils.models.territories.type.TerritoryUpgrade;
import com.wynntils.screens.territorymanagement.TerritoryManagementHolder;
import com.wynntils.screens.territorymanagement.highlights.TerritoryBonusEffectHighlighter;
import com.wynntils.screens.territorymanagement.highlights.TerritoryTypeHighlighter;
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
import net.minecraft.network.chat.Component;

public class TerritoryHighlightLegendWidget extends AbstractWidget {
    private final TerritoryManagementHolder holder;

    public TerritoryHighlightLegendWidget(int x, int y, int width, int height, TerritoryManagementHolder holder) {
        super(x, y, width, height, Component.literal("Territory Highlight Legend"));
        this.holder = holder;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Check if we should render the highlight legend
        if (!Managers.Feature.getFeatureInstance(CustomTerritoryManagementScreenFeature.class)
                .screenHighlightLegend
                .get()) {
            return;
        }

        // Render the background
        RenderUtils.drawRect(
                guiGraphics.pose(),
                CommonColors.BLACK.withAlpha(80),
                this.getX(),
                this.getY(),
                0,
                Texture.TERRITORY_MANAGEMENT_BACKGROUND.width(),
                110);

        // Render the title
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics.pose(),
                        StyledText.fromComponent(Component.literal("Highlight Legend")),
                        this.getX() + 5,
                        this.getX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width(),
                        this.getY() + 5,
                        this.getY() + 107,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        // Render the territory type highlight legends
        StyledText hqText = StyledText.fromComponent(Component.literal(
                "[%d] HQ Territory".formatted(holder.getCountForConnectionType(TerritoryConnectionType.HEADQUARTERS))));
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        hqText,
                        this.getX() + 5,
                        this.getY() + 40,
                        TerritoryTypeHighlighter.HEADQUARTERS_BORDER_COLOR,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);
        RenderUtils.drawRectBorders(
                guiGraphics.pose(),
                TerritoryTypeHighlighter.HEADQUARTERS_BORDER_COLOR,
                this.getX() + 3,
                this.getY() + 32,
                this.getX() + 7 + FontRenderer.getInstance().getFont().width(hqText.getComponent()),
                this.getY() + 46,
                0,
                1f);

        StyledText hqConnText = StyledText.fromComponent(Component.literal("[%d] HQ Connection"
                .formatted(holder.getCountForConnectionType(TerritoryConnectionType.HEADQUARTERS_CONNECTION))));
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        hqConnText,
                        this.getX() + 5,
                        this.getY() + 60,
                        TerritoryTypeHighlighter.HEADQUARTERS_CONNECTION_BORDER_COLOR,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);
        RenderUtils.drawRectBorders(
                guiGraphics.pose(),
                TerritoryTypeHighlighter.HEADQUARTERS_CONNECTION_BORDER_COLOR,
                this.getX() + 3,
                this.getY() + 53,
                this.getX() + 7 + FontRenderer.getInstance().getFont().width(hqConnText.getComponent()),
                this.getY() + 66,
                0,
                1f);

        StyledText unconnectedText = StyledText.fromComponent(Component.literal(
                "[%d] Unconnected".formatted(holder.getCountForConnectionType(TerritoryConnectionType.UNCONNECTED))));
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        unconnectedText,
                        this.getX() + 5,
                        this.getY() + 80,
                        TerritoryTypeHighlighter.NO_ROUTE_BORDER_COLOR,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);
        RenderUtils.drawRectBorders(
                guiGraphics.pose(),
                TerritoryTypeHighlighter.NO_ROUTE_BORDER_COLOR,
                this.getX() + 3,
                this.getY() + 73,
                this.getX() + 7 + FontRenderer.getInstance().getFont().width(unconnectedText.getComponent()),
                this.getY() + 86,
                0,
                1f);

        // Render the territory bonus effect highlight legends
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromComponent(Component.literal("Multi Attacks [%d]"
                                .formatted(holder.getCountForUpgrade(TerritoryUpgrade.TOWER_MULTI_ATTACKS)))),
                        this.getX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() - 5,
                        this.getY() + 25,
                        TerritoryBonusEffectHighlighter.MULTI_ATTACKS_COLOR,
                        HorizontalAlignment.RIGHT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromComponent(Component.literal("Emerald Seeking [%d]"
                                .formatted(holder.getCountForUpgrade(TerritoryUpgrade.EMERALD_SEEKING)))),
                        this.getX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() - 5,
                        this.getY() + 40,
                        TerritoryBonusEffectHighlighter.EMERALD_SEEKING_COLOR,
                        HorizontalAlignment.RIGHT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromComponent(Component.literal("Tome Seeking [%d]"
                                .formatted(holder.getCountForUpgrade(TerritoryUpgrade.TOME_SEEKING)))),
                        this.getX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() - 5,
                        this.getY() + 55,
                        TerritoryBonusEffectHighlighter.TOME_SEEKING_COLOR,
                        HorizontalAlignment.RIGHT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromComponent(Component.literal("Mob Experience [%d]"
                                .formatted(holder.getCountForUpgrade(TerritoryUpgrade.MOB_EXPERIENCE)))),
                        this.getX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() - 5,
                        this.getY() + 70,
                        TerritoryBonusEffectHighlighter.MOB_EXPERIENCE,
                        HorizontalAlignment.RIGHT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromComponent(Component.literal(
                                "Mob Damage [%d]".formatted(holder.getCountForUpgrade(TerritoryUpgrade.MOB_DAMAGE)))),
                        this.getX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() - 5,
                        this.getY() + 85,
                        TerritoryBonusEffectHighlighter.MOB_DAMAGE,
                        HorizontalAlignment.RIGHT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromComponent(Component.literal("Gathering Experience [%d]"
                                .formatted(holder.getCountForUpgrade(TerritoryUpgrade.GATHERING_EXPERIENCE)))),
                        this.getX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() - 5,
                        this.getY() + 100,
                        TerritoryBonusEffectHighlighter.GATHERING_EXPERIENCE,
                        HorizontalAlignment.RIGHT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
