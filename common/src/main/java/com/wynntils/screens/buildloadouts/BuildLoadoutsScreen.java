/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts;

import com.google.common.collect.Lists;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.buildloadouts.type.MenuCategory;
import com.wynntils.screens.buildloadouts.widgets.ItemTooltipProvider;
import com.wynntils.screens.buildloadouts.widgets.LoadoutMenuDeleteButton;
import com.wynntils.screens.buildloadouts.widgets.LoadoutMenuFavouriteButton;
import com.wynntils.screens.buildloadouts.widgets.LoadoutMenuItemWidget;
import com.wynntils.screens.buildloadouts.widgets.LoadoutMenuLoadButton;
import com.wynntils.screens.buildloadouts.widgets.LoadoutMenuNameWidget;
import com.wynntils.screens.buildloadouts.widgets.LoadoutMenuOverviewWidget;
import com.wynntils.screens.buildloadouts.widgets.BuildLoadoutScrollListWidget;
import com.wynntils.screens.buildloadouts.widgets.LoadoutMenuScrollListWidget;
import com.wynntils.screens.buildloadouts.widgets.LoadoutMenuSkillPointWidget;
import com.wynntils.screens.buildloadouts.widgets.LoadoutMenuUpdateButton;
import com.wynntils.screens.buildloadouts.widgets.LoadoutScrollListWidget;
import com.wynntils.screens.buildloadouts.widgets.LoadoutSearchWidget;
import com.wynntils.screens.buildloadouts.widgets.LoadoutSelectionButton;
import com.wynntils.screens.buildloadouts.widgets.MakeNewLoadoutButton;
import com.wynntils.screens.buildloadouts.widgets.NewLoadoutButton;
import com.wynntils.screens.buildloadouts.widgets.NewLoadoutInfoWidget;
import com.wynntils.screens.buildloadouts.widgets.NewLoadoutInputWidget;
import com.wynntils.screens.buildloadouts.widgets.NewLoadoutSelectionButton;
import com.wynntils.screens.buildloadouts.widgets.StatusWidget;
import com.wynntils.screens.buildloadouts.widgets.TitleWidget;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.services.loadout.type.LoadoutType;
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
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class BuildLoadoutsScreen extends WynntilsScreen {
    public static final CustomColor COMPLETED_COLOR = CustomColor.fromInt(0x4e9850);
    public static final CustomColor BUSY_COLOR = CustomColor.fromInt(0xffdf00);
    public static final CustomColor ERROR_COLOR = CustomColor.fromInt(0xbf3b46);

    private static final int WIDTH_OFFSET = 17;
    private static final int HEIGHT_OFFSET = 18;
    private static final int WIDGET_HOLDER_TWO_WIDTH_OFFSET = WIDTH_OFFSET + 133 + 3;
    private static final int WIDGET_HOLDER_THREE_WIDTH_OFFSET = WIDGET_HOLDER_TWO_WIDTH_OFFSET + 133 + 4;
    private static final int RIGHT_PAGE_WIDTH = 271;
    private static final int RIGHT_PAGE_HEIGHT = 279;

    private int offsetX;
    private int offsetY;

    public List<AbstractWidget> loadoutWidgets = new ArrayList<>();
    private boolean firstInit = true;

    public StatusWidget statusWidget;
    public LoadoutSearchWidget searchWidget;
    public LoadoutScrollListWidget loadoutScrollListWidget;
    public NewLoadoutInputWidget newLoadoutInputWidget;
    public NewLoadoutSelectionButton newBuildLoadoutButton;
    public NewLoadoutSelectionButton newAbilityTreeLoadoutButton;
    public NewLoadoutSelectionButton newSkillPointLoadoutButton;
    public NewLoadoutSelectionButton newAspectLoadoutButton;
    public NewLoadoutInfoWidget newLoadoutInfoWidget;
    public MakeNewLoadoutButton makeNewLoadoutButton;
    public LoadoutMenuNameWidget loadoutMenuNameWidget;
    public LoadoutMenuLoadButton loadoutMenuLoadButton;
    public LoadoutMenuUpdateButton loadoutMenuUpdateButton;
    public LoadoutMenuDeleteButton loadoutMenuDeleteButton;
    public LoadoutMenuSkillPointWidget loadoutMenuSkillPointWidget;
    public LoadoutMenuOverviewWidget loadoutMenuOverviewWidget;
    public LoadoutMenuItemWidget loadoutMenuItemWidget;
    public BuildLoadoutScrollListWidget buildLoadoutScrollListWidget;
    public LoadoutMenuFavouriteButton loadoutMenuFavouriteButton;
    public LoadoutMenuScrollListWidget loadoutMenuScrollListWidget;

    private MenuCategory currentCategory = MenuCategory.BUILD_LOADOUT;
    private LoadoutType newLoadoutType;
    private Loadout selectedLoadout;

    private BuildLoadoutsScreen() {
        super(Component.literal("Build Loadouts Screen"));
    }

    public static Screen create() {
        return new BuildLoadoutsScreen();
    }

    @Override
    protected void doInit() {
        super.doInit();
        if (firstInit) {
            firstInit = false;
            //closes the background container and gets skillpoints.
            Models.SkillPoint.populateSkillPoints();
        }

        offsetX = (int) ((this.width - Texture.BUILD_LOADOUTS_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.BUILD_LOADOUTS_BACKGROUND.height()) / 2f);

        //region Widget Holder 1
        int selectionY = 0;
        this.addRenderableWidget(new TitleWidget(
                StyledText.fromString("Loadouts"),
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY));
        selectionY += 20 + 10;

        this.addRenderableWidget(new LoadoutSelectionButton(
                StyledText.fromString("Build Loadouts"),
                MenuCategory.BUILD_LOADOUT,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                this));
        selectionY += 31 + 3;

        this.addRenderableWidget(new LoadoutSelectionButton(
                StyledText.fromString("Ability Tree Loadouts"),
                MenuCategory.ABILITY_TREE_LOADOUT,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                this));
        selectionY += 31 + 3;

        this.addRenderableWidget(new LoadoutSelectionButton(
                StyledText.fromString("Skill Point Loadouts"),
                MenuCategory.SKILL_POINT_LOADOUT,
                offsetX + WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                this));
        selectionY += 31 + 3;

        this.addRenderableWidget(new LoadoutSelectionButton(
                StyledText.fromString("Aspect Loadouts"),
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
                offsetY + HEIGHT_OFFSET + selectionY,
                this);
        this.addRenderableWidget(statusWidget);
        // end region

        // region Widget Holder 2
        selectionY = 5;
        searchWidget = new LoadoutSearchWidget(
                offsetX + WIDGET_HOLDER_TWO_WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + selectionY,
                (s) -> {
                    loadoutScrollListWidget.scrollOffset = 0;
                    loadoutScrollListWidget.populateLoadouts();
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
                (s) -> {},
                this);
        this.addRenderableWidget(newLoadoutInputWidget);

        newBuildLoadoutButton = new NewLoadoutSelectionButton(
                StyledText.fromString("Build Loadouts"),
                StyledText.fromString("This will create a new build loadout from the current items, ability tree, skill points, and aspects."),
                LoadoutType.BUILD,
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + 102,
                this);
        this.addRenderableWidget(newBuildLoadoutButton);

        newAbilityTreeLoadoutButton = new NewLoadoutSelectionButton(
                StyledText.fromString("Ability Tree Loadouts"),
                StyledText.fromString("This will create a new ability tree loadout from the current ability tree."),
                LoadoutType.ABILITY_TREE,
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 5 + 128 + 5,
                offsetY + HEIGHT_OFFSET + 102,
                this);
        this.addRenderableWidget(newAbilityTreeLoadoutButton);

        newSkillPointLoadoutButton = new NewLoadoutSelectionButton(
                StyledText.fromString("Skill Point Loadouts"),
                StyledText.fromString("This will create a new skill point loadout from the current skill points."),
                LoadoutType.SKILL_POINT,
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + 102 + 40 + 5,
                this);
        this.addRenderableWidget(newSkillPointLoadoutButton);

        newAspectLoadoutButton = new NewLoadoutSelectionButton(
                StyledText.fromString("Aspect Loadouts"),
                StyledText.fromString("This will create a new aspect loadout from the current aspects."),
                LoadoutType.ASPECT,
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 5 + 128 + 5,
                offsetY + HEIGHT_OFFSET + 102 + 40 + 5,
                this);
        this.addRenderableWidget(newAspectLoadoutButton);

        newLoadoutInfoWidget = new NewLoadoutInfoWidget(
                (int) (offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + (RIGHT_PAGE_WIDTH / 2f) - (RIGHT_PAGE_WIDTH - 10) / 2f),
                offsetY + HEIGHT_OFFSET + 192 + 8,
                RIGHT_PAGE_WIDTH - 10,
                34,
                this
                );
        this.addRenderableWidget(newLoadoutInfoWidget);

        makeNewLoadoutButton = new MakeNewLoadoutButton(
                (int) (offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + (RIGHT_PAGE_WIDTH / 2f) - ((133 - 10) / 2f)),
                offsetY + RIGHT_PAGE_HEIGHT - 15,
                this
        );
        this.addRenderableWidget(makeNewLoadoutButton);
        // end region

        //region Loadout Menu
        loadoutMenuNameWidget = new LoadoutMenuNameWidget(
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + 5,
                180,
                (s) -> {},
                this,
                this);
        this.addRenderableWidget(loadoutMenuNameWidget);

        loadoutMenuFavouriteButton = new LoadoutMenuFavouriteButton(
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + RIGHT_PAGE_WIDTH - 25,
                offsetY + HEIGHT_OFFSET + 5,
                this
        );
        this.addRenderableWidget(loadoutMenuFavouriteButton);

        loadoutMenuLoadButton = new LoadoutMenuLoadButton(
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET,
                offsetY + HEIGHT_OFFSET + RIGHT_PAGE_HEIGHT - 20,
                this
        );
        this.addRenderableWidget(loadoutMenuLoadButton);

        loadoutMenuUpdateButton = new LoadoutMenuUpdateButton(
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 79 + 17,
                offsetY + HEIGHT_OFFSET + RIGHT_PAGE_HEIGHT - 20,
                this
        );
        this.addRenderableWidget(loadoutMenuUpdateButton);

        loadoutMenuDeleteButton = new LoadoutMenuDeleteButton(
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 79 * 2 + 17 * 2,
                offsetY + HEIGHT_OFFSET + RIGHT_PAGE_HEIGHT - 20,
                this
        );
        this.addRenderableWidget(loadoutMenuDeleteButton);

        loadoutMenuSkillPointWidget = new LoadoutMenuSkillPointWidget(
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + RIGHT_PAGE_HEIGHT - 20 - 66 - 6 - 5,
                this
        );
        this.addRenderableWidget(loadoutMenuSkillPointWidget);

        loadoutMenuOverviewWidget = new LoadoutMenuOverviewWidget(
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 68,
                offsetY + HEIGHT_OFFSET + RIGHT_PAGE_HEIGHT - 20 - 66 - 6 - 5,
                this
        );
        this.addRenderableWidget(loadoutMenuOverviewWidget);

        loadoutMenuItemWidget = new LoadoutMenuItemWidget(
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + RIGHT_PAGE_WIDTH - 98 - 5,
                offsetY + HEIGHT_OFFSET + RIGHT_PAGE_HEIGHT - 20 - 66 - 6 - 5,
                this
        );
        this.addRenderableWidget(loadoutMenuItemWidget);

        loadoutMenuScrollListWidget = new LoadoutMenuScrollListWidget(
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + 38,
                this
        );
        this.addRenderableWidget(loadoutMenuScrollListWidget);
        // end region

        //region Build Loadout Menu
        buildLoadoutScrollListWidget = new BuildLoadoutScrollListWidget(
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET + 5,
                offsetY + HEIGHT_OFFSET + 38 + 15,
                this
        );
        this.addRenderableWidget(buildLoadoutScrollListWidget);
        //end region

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
        } else if (getCurrentCategory() == MenuCategory.BUILD_LOADOUT && getSelectedLoadout() != null) {
            renderBuildLoadoutMenu(guiGraphics, mouseX, mouseY, partialTick);
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

    private void renderBuildLoadoutMenu(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND,
                offsetX + WIDGET_HOLDER_THREE_WIDTH_OFFSET,
                offsetY + HEIGHT_OFFSET + RIGHT_PAGE_HEIGHT - 20 - 66 - 6 - 10,
                RIGHT_PAGE_WIDTH,
                78);
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
            if (child instanceof ItemTooltipProvider itemTooltipProvider && child.isMouseOver(mouseX, mouseY)) {
                itemTooltipProvider.renderHoveredItemTooltip(guiGraphics, mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        TextInputBoxWidget focused = getFocusedTextInput();
        boolean handled = super.doMouseClicked(event, isDoubleClick);

        if (focused != null && !focused.isMouseOver(event.x(), event.y())) {
            setFocusedTextInput(null);
        }

        return handled;
    }

    private void updateMenu() {
        // new loadout
        newLoadoutInputWidget.visible = false;
        newBuildLoadoutButton.visible = false;
        newAbilityTreeLoadoutButton.visible = false;
        newSkillPointLoadoutButton.visible = false;
        newAspectLoadoutButton.visible = false;
        newLoadoutInfoWidget.visible = false;
        makeNewLoadoutButton.visible = false;

        // loadouts
        loadoutMenuNameWidget.visible = false;
        loadoutMenuFavouriteButton.visible = false;
        loadoutMenuLoadButton.visible = false;
        loadoutMenuUpdateButton.visible = false;
        loadoutMenuDeleteButton.visible = false;
        loadoutMenuSkillPointWidget.visible = false;
        loadoutMenuOverviewWidget.visible = false;
        loadoutMenuItemWidget.visible = false;
        loadoutMenuScrollListWidget.visible = false;

        // build loadouts
        buildLoadoutScrollListWidget.visible = false;

        if (getCurrentCategory() == MenuCategory.NEW_LOADOUT) {
            newLoadoutInputWidget.visible = true;
            newBuildLoadoutButton.visible = true;
            newAbilityTreeLoadoutButton.visible = true;
            newSkillPointLoadoutButton.visible = true;
            newAspectLoadoutButton.visible = true;
            newLoadoutInfoWidget.visible = true;
            makeNewLoadoutButton.visible = true;
        }

        if (getCurrentCategory() != MenuCategory.NEW_LOADOUT && getSelectedLoadout() != null) {
            loadoutMenuNameWidget.visible = true;
            loadoutMenuFavouriteButton.visible = true;
            loadoutMenuLoadButton.visible = true;
            loadoutMenuUpdateButton.visible = true;
            loadoutMenuDeleteButton.visible = true;
            loadoutMenuSkillPointWidget.visible = true;
            loadoutMenuOverviewWidget.visible = true;
            loadoutMenuItemWidget.visible = true;
        }

        if (getCurrentCategory() != MenuCategory.BUILD_LOADOUT && getCurrentCategory() != MenuCategory.NEW_LOADOUT  && getSelectedLoadout() != null) {
            loadoutMenuScrollListWidget.visible = true;
            loadoutMenuScrollListWidget.populateWidgets();
        }

        if (getCurrentCategory() == MenuCategory.BUILD_LOADOUT && getSelectedLoadout() != null) {
            buildLoadoutScrollListWidget.visible = true;
            buildLoadoutScrollListWidget.populateWidgets();
        }
    }

    public void setCurrentCategory(MenuCategory category) {
        this.currentCategory = category;
        setSelectedLoadout(null);
        searchWidget.setTextBoxInput("");
        loadoutScrollListWidget.populateLoadouts();
        loadoutScrollListWidget.scrollOffset = 0;
        loadoutMenuLoadButton.syncLoadType();
        loadoutMenuUpdateButton.syncUpdateType();
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

    public void setSelectedLoadout(Loadout loadout) {
        if (loadoutMenuNameWidget.isEditing()) {
            loadoutMenuNameWidget.cancelEditing();
        }
        this.selectedLoadout = loadout;
        updateMenu();
    }

    public Loadout getSelectedLoadout() {
        return this.selectedLoadout;
    }
}
