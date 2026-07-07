/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts;

import com.google.common.collect.Lists;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.buildloadouts.widgets.LoadoutSelectionWidget;
import com.wynntils.screens.buildloadouts.widgets.TextWidget;
import com.wynntils.screens.buildloadouts.widgets.TitleWidget;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.stream.Stream;

public class BuildLoadoutsScreen extends WynntilsScreen {
    private final int WIDTH_OFFSET = 17;
    private final int HEIGHT_OFFSET = 18;

    private int offsetX;
    private int offsetY;
    private LoadoutSelectionWidget buildLoadouts;
    private LoadoutSelectionWidget abilityTreeLoadouts;
    private LoadoutSelectionWidget skillPointLoadouts;
    private LoadoutSelectionWidget aspectLoadouts;

    private BuildLoadoutsScreen() {
        super(Component.literal("Build Loadouts Screen"));
    }

    public static Screen create() {
        return new BuildLoadoutsScreen();
    }

    @Override
    protected void doInit() {
        super.doInit();

        offsetX = (int) ((this.width - Texture.BUILD_LOADOUTS_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.BUILD_LOADOUTS_BACKGROUND.height()) / 2f);

        int selectionY = 0;

        this.addRenderableWidget(new TitleWidget(
                StyledText.fromString("Loadouts"),
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY));
        selectionY += 20 + 10;

        buildLoadouts = new LoadoutSelectionWidget(
                StyledText.fromString("Build Loadouts"),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY);
        this.addRenderableWidget(buildLoadouts);
        selectionY += 39 + 3;

        abilityTreeLoadouts = new LoadoutSelectionWidget(
                StyledText.fromString("Ability Tree Loadouts"),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY);
        this.addRenderableWidget(abilityTreeLoadouts);
        selectionY += 39 + 3;

        skillPointLoadouts = new LoadoutSelectionWidget(
                StyledText.fromString("Skill Point Loadouts"),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY);
        this.addRenderableWidget(skillPointLoadouts);
        selectionY += 39 + 3;

        aspectLoadouts = new LoadoutSelectionWidget(
                StyledText.fromString("Aspect Loadouts"),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY);
        this.addRenderableWidget(aspectLoadouts);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(guiGraphics);
        // region Backgrounds
        // background for widgets 1
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND,
                offsetX + WIDTH_OFFSET,
                offsetY + HEIGHT_OFFSET + 24,
                133,
                279 - 24);

        // background for widgets 2
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND,
                offsetX + WIDTH_OFFSET + 133 + 3,
                offsetY + HEIGHT_OFFSET,
                133,
                279);
        // end region

        renderables.forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));

        renderTooltips(guiGraphics, mouseX, mouseY);
    }

    private void renderBackgroundTexture(GuiGraphics guiGraphics) {
        RenderUtils.drawTexturedRect(guiGraphics, Texture.BUILD_LOADOUTS_BACKGROUND, offsetX, offsetY);
    }

    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (GuiEventListener child : children()) {
            if (child instanceof TooltipProvider tooltipProvider && child.isMouseOver(mouseX, mouseY)) {
                guiGraphics.setTooltipForNextFrame(
                        Lists.transform(tooltipProvider.getTooltipLines(), Component::getVisualOrderText),
                        mouseX,
                        mouseY);
                break;
            }
        }
    }
}
