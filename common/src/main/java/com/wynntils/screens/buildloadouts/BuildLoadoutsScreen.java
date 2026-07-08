/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts;

import com.google.common.collect.Lists;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.buildloadouts.type.LoadoutType;
import com.wynntils.screens.buildloadouts.type.MenuCategory;
import com.wynntils.screens.buildloadouts.widgets.LoadoutScrollListWidget;
import com.wynntils.screens.buildloadouts.widgets.LoadoutSearchWidget;
import com.wynntils.screens.buildloadouts.widgets.LoadoutSelectionWidget;
import com.wynntils.screens.buildloadouts.widgets.MakeNewLoadoutButton;
import com.wynntils.screens.buildloadouts.widgets.NewLoadoutButton;
import com.wynntils.screens.buildloadouts.widgets.NewLoadoutInfoWidget;
import com.wynntils.screens.buildloadouts.widgets.NewLoadoutInputWidget;
import com.wynntils.screens.buildloadouts.widgets.NewLoadoutSelectionWidget;
import com.wynntils.screens.buildloadouts.widgets.StatusWidget;
import com.wynntils.screens.buildloadouts.widgets.TitleWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
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
    private static final int WIDGET_HOLDER_THREE_WIDTH_OFFSET = WIDGET_HOLDER_TWO_WIDTH_OFFSET + 133 + 4;
    private static final int RIGHT_PAGE_WIDTH = 271;
    private static final int RIGHT_PAGE_HEIGHT = 279;

    private int offsetX;
    private int offsetY;

    public List<AbstractWidget> loadoutWidgets = new ArrayList<>();

    private StatusWidget statusWidget;
    private LoadoutSearchWidget searchWidget;
    private LoadoutScrollListWidget loadoutScrollListWidget;
    private NewLoadoutInputWidget newLoadoutInputWidget;
    private NewLoadoutSelectionWidget newBuildLoadoutWidget;
    private NewLoadoutSelectionWidget newAbilityTreeLoadoutWidget;
    private NewLoadoutSelectionWidget newSkillPointLoadoutWidget;
    private NewLoadoutSelectionWidget newAspectLoadoutWidget;
    public NewLoadoutInfoWidget newLoadoutInfoWidget;
    private MakeNewLoadoutButton makeNewLoadoutButton;

    private MenuCategory currentCategory = MenuCategory.BUILD_LOADOUT;
    private LoadoutType newLoadoutType;

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

        this.addRenderableWidget(new LoadoutSelectionWidget(
                StyledText.fromString("Build Loadouts"),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                MenuCategory.BUILD_LOADOUT,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                this));
        selectionY += 31 + 3;

        this.addRenderableWidget(new LoadoutSelectionWidget(
                StyledText.fromString("Ability Tree Loadouts"),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                MenuCategory.ABILITY_TREE_LOADOUT,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                this));
        selectionY += 31 + 3;

        this.addRenderableWidget(new LoadoutSelectionWidget(
                StyledText.fromString("Skill Point Loadouts"),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                MenuCategory.SKILL_POINT_LOADOUT,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                this));
        selectionY += 31 + 3;

        this.addRenderableWidget(new LoadoutSelectionWidget(
                StyledText.fromString("Aspect Loadouts"),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                MenuCategory.ASPECT_LOADOUT,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                this));
        selectionY += 31 + 3;

        this.addRenderableWidget(new NewLoadoutButton(
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                this));
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

        //region New Loadout Menu
        newLoadoutInputWidget = new NewLoadoutInputWidget(
                (int) (offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + (RIGHT_PAGE_WIDTH / 2f) - (180 / 2f)),
                offsetY + HEIGHT_OFFSET + 50,
                180,
                (s) -> {
                    WynntilsMod.info("text: " + s);
                },
                this);
        this.addRenderableWidget(newLoadoutInputWidget);

        newBuildLoadoutWidget = new NewLoadoutSelectionWidget(
                StyledText.fromString("Build Loadouts"),
                StyledText.fromString("This will create a new build loadout from the current items, ability tree, skill points, and aspects."),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                LoadoutType.BUILD,
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + 102,
                this);
        this.addRenderableWidget(newBuildLoadoutWidget);

        newAbilityTreeLoadoutWidget = new NewLoadoutSelectionWidget(
                StyledText.fromString("Ability Tree Loadouts"),
                StyledText.fromString("This will create a new ability tree loadout from the current ability tree."),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                LoadoutType.ABILITY_TREE,
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 5 + 128 + 5,
                offsetY + HEIGHT_OFFSET + 102,
                this);
        this.addRenderableWidget(newAbilityTreeLoadoutWidget);

        newSkillPointLoadoutWidget = new NewLoadoutSelectionWidget(
                StyledText.fromString("Skill Point Loadouts"),
                StyledText.fromString("This will create a new skill point loadout from the current skill points."),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                LoadoutType.SKILL_POINT,
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + 102 + 40 + 5,
                this);
        this.addRenderableWidget(newSkillPointLoadoutWidget);

        newAspectLoadoutWidget = new NewLoadoutSelectionWidget(
                StyledText.fromString("Aspect Loadouts"),
                StyledText.fromString("This will create a new aspect loadout from the current aspects."),
                Texture.BUILD_LOADOUTS_SKILL_POINT_LOADOUTS_ICON,
                LoadoutType.ASPECT,
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 5 + 128 + 5,
                offsetY + HEIGHT_OFFSET + 102 + 40 + 5,
                this);
        this.addRenderableWidget(newAspectLoadoutWidget);

        newLoadoutInfoWidget = new NewLoadoutInfoWidget(
                (int) (offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + (RIGHT_PAGE_WIDTH / 2f) - (RIGHT_PAGE_WIDTH - 10) / 2f),
                offsetY + HEIGHT_OFFSET + 192 + 8,
                RIGHT_PAGE_WIDTH - 10,
                34
                );
        this.addRenderableWidget(newLoadoutInfoWidget);

        makeNewLoadoutButton = new MakeNewLoadoutButton(
                (int) (offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + (RIGHT_PAGE_WIDTH / 2f) - ((133 - 10) / 2f)),
                offsetY + RIGHT_PAGE_HEIGHT - 15,
                this
        );
        this.addRenderableWidget(makeNewLoadoutButton);
        // end region

        loadoutScrollListWidget.populateLoadouts();
        updateMenu();
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
        if (getCurrentCategory() == MenuCategory.NEW_LOADOUT) {
            renderNewLoadoutMenu(guiGraphics, mouseX, mouseY, partialTick);
        }

        renderables.forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));
        renderTooltips(guiGraphics, mouseX, mouseY);
    }

    private void renderBackgroundTexture(GuiGraphics guiGraphics) {
        RenderUtils.drawTexturedRect(guiGraphics, Texture.BUILD_LOADOUTS_BACKGROUND, offsetX, offsetY);
    }

    private void renderNewLoadoutMenu(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString("§#1b3f94ffNew Loadout"),
                        offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + (RIGHT_PAGE_WIDTH / 2f),
                        offsetY + HEIGHT_OFFSET + 15,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NONE,
                        1.5f);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString("§#9e7f47ffEnter a name for your new loadout:"),
                        offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + (RIGHT_PAGE_WIDTH / 2f),
                        offsetY + HEIGHT_OFFSET + 40,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NONE);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString("§#9e7f47ffSelect a loadout type:"),
                        offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + (RIGHT_PAGE_WIDTH / 2f),
                        offsetY + HEIGHT_OFFSET + 95,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NONE);
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

    private void updateMenu() {
        newLoadoutInputWidget.visible = false;
        newBuildLoadoutWidget.visible = false;
        newAbilityTreeLoadoutWidget.visible = false;
        newSkillPointLoadoutWidget.visible = false;
        newAspectLoadoutWidget.visible = false;
        newLoadoutInfoWidget.visible = false;
        makeNewLoadoutButton.visible = false;

        if (getCurrentCategory() == MenuCategory.NEW_LOADOUT) {
            newLoadoutInputWidget.visible = true;
            newBuildLoadoutWidget.visible = true;
            newAbilityTreeLoadoutWidget.visible = true;
            newSkillPointLoadoutWidget.visible = true;
            newAspectLoadoutWidget.visible = true;
            newLoadoutInfoWidget.visible = true;
            makeNewLoadoutButton.visible = true;
        }
    }

    public void setCurrentCategory(MenuCategory category) {
        this.currentCategory = category;
        updateMenu();
    }

    public MenuCategory getCurrentCategory() {
        return this.currentCategory;
    }

    public void setNewLoadoutType(LoadoutType newLoadoutType) {
        this.newLoadoutType = newLoadoutType;
    }

    public LoadoutType getNewLoadoutType() {
        return this.newLoadoutType;
    }
}
