/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts;

import com.google.common.collect.Lists;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.abilitytree.type.SavableAbilityTree;
import com.wynntils.models.aspects.type.SavableAspectSet;
import com.wynntils.models.character.type.SavableSkillPointSet;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.buildloadouts.type.LoadoutCategory;
import com.wynntils.screens.buildloadouts.type.LoadoutType;
import com.wynntils.screens.buildloadouts.widgets.LoadoutScrollListWidget;
import com.wynntils.screens.buildloadouts.widgets.LoadoutSearchWidget;
import com.wynntils.screens.buildloadouts.widgets.LoadoutSelectionWidget;
import com.wynntils.screens.buildloadouts.widgets.NewLoadoutButton;
import com.wynntils.screens.buildloadouts.widgets.StatusWidget;
import com.wynntils.screens.buildloadouts.widgets.TitleWidget;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class BuildLoadoutsScreen extends WynntilsScreen {
    private static final int WIDTH_OFFSET = 17;
    private static final int HEIGHT_OFFSET = 18;
    private static final int WIDGET_HOLDER_TWO_WIDTH_OFFSET = WIDTH_OFFSET + 133 + 3;

    private int offsetX;
    private int offsetY;

    public List<AbstractWidget> loadoutWidgets = new ArrayList<>();

    private LoadoutSelectionWidget buildLoadoutsWidget;
    private LoadoutSelectionWidget abilityTreeLoadoutsWidget;
    private LoadoutSelectionWidget skillPointLoadoutsWidget;
    private LoadoutSelectionWidget aspectLoadoutsWidget;
    private StatusWidget statusWidget;

    private LoadoutSearchWidget searchWidget;
    private LoadoutScrollListWidget loadoutScrollListWidget;

    public LoadoutCategory currentCategory = LoadoutCategory.BUILD_LOADOUT;

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
        //region Widget Holder 1
        int selectionY = 0;
        this.addRenderableWidget(new TitleWidget(
                StyledText.fromString("Loadouts"),
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY));
        selectionY += 20 + 10;

        buildLoadoutsWidget = new LoadoutSelectionWidget(
                StyledText.fromString("Build Loadouts"),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                LoadoutCategory.BUILD_LOADOUT,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                this);
        this.addRenderableWidget(buildLoadoutsWidget);
        selectionY += 31 + 3;

        abilityTreeLoadoutsWidget = new LoadoutSelectionWidget(
                StyledText.fromString("Ability Tree Loadouts"),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                LoadoutCategory.ABILITY_TREE_LOADOUT,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                this);
        this.addRenderableWidget(abilityTreeLoadoutsWidget);
        selectionY += 31 + 3;

        skillPointLoadoutsWidget = new LoadoutSelectionWidget(
                StyledText.fromString("Skill Point Loadouts"),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                LoadoutCategory.SKILL_POINT_LOADOUT,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                this);
        this.addRenderableWidget(skillPointLoadoutsWidget);
        selectionY += 31 + 3;

        aspectLoadoutsWidget = new LoadoutSelectionWidget(
                StyledText.fromString("Aspect Loadouts"),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                LoadoutCategory.ASPECT_LOADOUT,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                this);
        this.addRenderableWidget(aspectLoadoutsWidget);
        selectionY += 31 + 3;

        this.addRenderableWidget(new NewLoadoutButton(
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY));
        selectionY += 20 + 3;

        statusWidget = new StatusWidget(
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY);
        this.addRenderableWidget(statusWidget);
        // end region

        // region Widget Holder 2
        selectionY = 5;
        searchWidget = new LoadoutSearchWidget(
                offsetX + WIDGET_HOLDER_TWO_WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                (s) -> {
                    WynntilsMod.info("text: " + s);
                }, this);
        this.addRenderableWidget(searchWidget);
        selectionY += 20 + 3;

        loadoutScrollListWidget = new LoadoutScrollListWidget(
                offsetX + WIDGET_HOLDER_TWO_WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                this
                );
        this.addRenderableWidget(loadoutScrollListWidget);
        // end region
        loadoutScrollListWidget.populateLoadouts();
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
