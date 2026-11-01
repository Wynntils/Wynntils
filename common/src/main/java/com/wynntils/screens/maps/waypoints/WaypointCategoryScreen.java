/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.waypoints;

import com.google.common.collect.Lists;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.managers.widgets.ManagerSearchWidget;
import com.wynntils.screens.maps.managers.widgets.StyledButton;
import com.wynntils.screens.maps.managers.widgets.TexturedTextInputBoxWidget;
import com.wynntils.screens.maps.managers.widgets.categorymanager.CategoryTreeWidget;
import com.wynntils.screens.maps.waypoints.widgets.FeatureScrollBarWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class WaypointCategoryScreen extends WynntilsScreen {
    private static final int WIDTH_OFFSET = 15;
    private static final int HEIGHT_OFFSET = 18;

    private static final String DEFAULT_CATEGORY = "wynntils:personal:waypoint";
    private static final String ROOT_CATEGORY = "waypoint";
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^[a-z0-9-]+(:[a-z0-9-]+)*$");

    public final WaypointCreationScreen creationScreen;
    private final Set<String> categories = new TreeSet<>();

    private int offsetX;
    private int offsetY;

    // Relative to the default category, empty for the default category itself
    private String selectedCategory;

    public ManagerSearchWidget categorySearchWidget;
    public CategoryTreeWidget categoryTreeWidget;
    public FeatureScrollBarWidget featureScrollBarWidget;

    private TexturedTextInputBoxWidget newCategoryInput;
    private StyledButton createCategoryButton;

    private WaypointCategoryScreen(WaypointCreationScreen creationScreen, String category) {
        super(Component.literal("Waypoint Category Screen"));

        this.creationScreen = creationScreen;
        this.selectedCategory = category;

        Services.Waypoints.getCategories().stream()
                .filter(waypointCategory -> waypointCategory.startsWith(DEFAULT_CATEGORY + ":"))
                .map(waypointCategory -> waypointCategory.substring(DEFAULT_CATEGORY.length() + 1))
                .forEach(categories::add);

        if (!category.isEmpty()) {
            categories.add(category);
        }
    }

    public static Screen create(WaypointCreationScreen creationScreen, String category) {
        return new WaypointCategoryScreen(creationScreen, category);
    }

    @Override
    protected void doInit() {
        super.doInit();

        offsetX = (int) ((this.width - Texture.MANAGER_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.MANAGER_BACKGROUND.height()) / 2f);

        // category tree menu

        categorySearchWidget = new ManagerSearchWidget(
                offsetX + WIDTH_OFFSET,
                offsetY + HEIGHT_OFFSET,
                200 - 5 - Texture.BUILD_LOADOUTS_SEARCH_ICON.width(),
                20,
                (text) -> categoryTreeWidget.filter(text),
                this);
        this.addRenderableWidget(categorySearchWidget);

        categoryTreeWidget = new CategoryTreeWidget(
                offsetX + WIDTH_OFFSET,
                offsetY + HEIGHT_OFFSET + 25,
                200,
                284 - 25,
                this::onCategorySelected,
                category -> true);
        this.addRenderableWidget(categoryTreeWidget);

        // end region

        // category info menu

        featureScrollBarWidget = new FeatureScrollBarWidget(
                offsetX + WIDTH_OFFSET + 200 + 5, offsetY + HEIGHT_OFFSET + 25 + 15, 341, 132, this);
        this.addRenderableWidget(featureScrollBarWidget);

        rebuildTree();

        String inputText = newCategoryInput != null ? newCategoryInput.getTextBoxInput() : selectedCategory;

        newCategoryInput = new TexturedTextInputBoxWidget(
                offsetX + WIDTH_OFFSET + 200 + 5 + 5,
                offsetY + HEIGHT_OFFSET + 177 + 58,
                226,
                18,
                this::onNewCategoryChanged,
                this,
                TexturedTextInputBoxWidget.Mode.STRING);
        newCategoryInput.setTooltip(
                Tooltip.create(Component.translatable("screens.wynntils.waypointCategory.nameTooltip")));
        this.addRenderableWidget(newCategoryInput);

        createCategoryButton = new StyledButton(
                offsetX + WIDTH_OFFSET + 200 + 20 + 213 + 8,
                offsetY + HEIGHT_OFFSET + 177 + 57,
                100,
                20,
                Component.translatable("screens.wynntils.waypointCategory.create"),
                Texture.MANAGER_WIDGET_BACKGROUND,
                this::createCategory);
        this.addRenderableWidget(createCategoryButton);

        this.addRenderableWidget(new StyledButton(
                offsetX + WIDTH_OFFSET + 200 + 5,
                offsetY + HEIGHT_OFFSET + 284 - 20,
                100,
                20,
                Component.translatable("screens.wynntils.waypointCategory.back"),
                Texture.MANAGER_WIDGET_BACKGROUND_RED,
                this::onClose));

        this.addRenderableWidget(new StyledButton(
                offsetX + WIDTH_OFFSET + 200 + 5 + 100 + 8,
                offsetY + HEIGHT_OFFSET + 284 - 20,
                341 - 100 - 8,
                20,
                Component.translatable("screens.wynntils.waypointCategory.useSelected"),
                Texture.MANAGER_WIDGET_BACKGROUND_GREEN,
                () -> {
                    creationScreen.setCategory(selectedCategory);
                    onClose();
                }));

        // end region

        newCategoryInput.setTextBoxInput(inputText);
        createCategoryButton.active = isValidNewCategory(inputText);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(guiGraphics, Texture.MANAGER_BACKGROUND, offsetX, offsetY);

        renderInfoPanel(guiGraphics);

        renderables.forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));
        renderTooltips(guiGraphics, mouseX, mouseY);
    }

    private void renderInfoPanel(GuiGraphics guiGraphics) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_WIDGET_BACKGROUND,
                offsetX + WIDTH_OFFSET + 200 + 5,
                offsetY + HEIGHT_OFFSET + 177,
                341,
                82);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_TEXT_BACKGROUND_RED,
                offsetX + WIDTH_OFFSET + 200 + 5 + 10 - 5,
                offsetY + HEIGHT_OFFSET + 177 + 12 - 14 / 2f,
                105,
                14);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.waypointCategory.selectedCategory")
                                        .append(":")),
                        offsetX + WIDTH_OFFSET + 200 + 5 + 10,
                        offsetY + HEIGHT_OFFSET + 177 + 12,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_TEXT_BORDER_BROWN,
                offsetX + WIDTH_OFFSET + 200 + 5 + 120 - 5,
                offsetY + HEIGHT_OFFSET + 177 + 12 - 14 / 2f,
                341 - 120 - 5 + 5,
                14);

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromString(getSelectedCategoryName()),
                        offsetX + WIDTH_OFFSET + 200 + 5 + 120,
                        offsetY + HEIGHT_OFFSET + 177 + 12,
                        341 - 120 - 10,
                        CommonColors.LIGHT_GRAY,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_TEXT_BACKGROUND_RED,
                offsetX + WIDTH_OFFSET + 200 + 5 + 10 - 5,
                offsetY + HEIGHT_OFFSET + 177 + 32 - 14 / 2f,
                105,
                14);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.waypointCategory.categoryPath")
                                        .append(":")),
                        offsetX + WIDTH_OFFSET + 200 + 5 + 10,
                        offsetY + HEIGHT_OFFSET + 177 + 32,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_TEXT_BORDER_BROWN,
                offsetX + WIDTH_OFFSET + 200 + 5 + 120 - 5,
                offsetY + HEIGHT_OFFSET + 177 + 32 - 14 / 2f,
                341 - 120 - 5 + 5,
                14);

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromString(toTreeId(selectedCategory)),
                        offsetX + WIDTH_OFFSET + 200 + 5 + 120,
                        offsetY + HEIGHT_OFFSET + 177 + 32,
                        341 - 120 - 10,
                        CommonColors.LIGHT_GRAY,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.waypointCategory.newCategory")
                                .append(":")),
                        offsetX + WIDTH_OFFSET + 200 + 5 + 5,
                        offsetY + HEIGHT_OFFSET + 177 + 51,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (GuiEventListener child : children()) {
            if (!(child instanceof TooltipProvider tooltipProvider) || !child.isMouseOver(mouseX, mouseY)) continue;

            List<Component> lines = tooltipProvider.getTooltipLines();
            if (lines.isEmpty()) continue;

            guiGraphics.setTooltipForNextFrame(Lists.transform(lines, Component::getVisualOrderText), mouseX, mouseY);
            break;
        }
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        TextInputBoxWidget focusedBefore = getFocusedTextInput();
        boolean handled = super.doMouseClicked(event, isDoubleClick);

        TextInputBoxWidget focusedAfter = getFocusedTextInput();
        if (focusedAfter != null && focusedAfter == focusedBefore && !focusedAfter.isMouseOver(event.x(), event.y())) {
            setFocusedTextInput(null);
        }

        return handled;
    }

    @Override
    public void onClose() {
        McUtils.setScreen(creationScreen);
    }

    private void createCategory() {
        String category = newCategoryInput.getTextBoxInput();
        if (!isValidNewCategory(category)) return;

        categories.add(category);
        selectedCategory = category;

        categorySearchWidget.setTextBoxInput("");
        rebuildTree();
        newCategoryInput.setTextBoxInput(category);
    }

    private void rebuildTree() {
        List<String> treeCategories = new ArrayList<>();
        treeCategories.add(ROOT_CATEGORY);
        categories.forEach(category -> treeCategories.add(toTreeId(category)));

        categoryTreeWidget.setCategories(treeCategories);
        categoryTreeWidget.selectCategory(toTreeId(selectedCategory));

        updateFeatures();
    }

    private void onCategorySelected(String treeId) {
        selectedCategory = fromTreeId(treeId);
        newCategoryInput.setTextBoxInput(selectedCategory);

        updateFeatures();
    }

    private void updateFeatures() {
        featureScrollBarWidget.populateWidgets(getSelectedCategoryId());
    }

    private void onNewCategoryChanged(String input) {
        if (createCategoryButton != null) {
            createCategoryButton.active = isValidNewCategory(input);
        }
    }

    private boolean isValidNewCategory(String input) {
        return CATEGORY_PATTERN.matcher(input).matches() && !categoryExists(input);
    }

    private boolean categoryExists(String input) {
        return categories.stream().anyMatch(category -> category.equals(input) || category.startsWith(input + ":"));
    }

    private String getSelectedCategoryId() {
        return selectedCategory.isEmpty() ? DEFAULT_CATEGORY : DEFAULT_CATEGORY + ":" + selectedCategory;
    }

    private String getSelectedCategoryName() {
        return selectedCategory.isEmpty()
                ? ROOT_CATEGORY
                : selectedCategory.substring(selectedCategory.lastIndexOf(':') + 1);
    }

    private String toTreeId(String category) {
        return category.isEmpty() ? ROOT_CATEGORY : ROOT_CATEGORY + ":" + category;
    }

    private String fromTreeId(String treeId) {
        return treeId.equals(ROOT_CATEGORY) ? "" : treeId.substring(ROOT_CATEGORY.length() + 1);
    }
}
