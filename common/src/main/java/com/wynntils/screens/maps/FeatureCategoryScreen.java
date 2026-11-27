/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.google.common.collect.Lists;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.managers.widgets.FeatureScrollBarWidget;
import com.wynntils.screens.maps.managers.widgets.ManagerSearchWidget;
import com.wynntils.screens.maps.managers.widgets.StyledButton;
import com.wynntils.screens.maps.managers.widgets.TexturedTextInputBoxWidget;
import com.wynntils.screens.maps.managers.widgets.categorymanager.CategoryTreeWidget;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
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
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class FeatureCategoryScreen extends WynntilsScreen {
    private static final int WIDTH_OFFSET = 15;
    private static final int HEIGHT_OFFSET = 18;

    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^[a-z0-9_-]+(:[a-z0-9_-]+)*$");

    private final Screen previous;
    private final String rootCategory;
    private final String treeRoot;
    private final Consumer<String> onSelect;
    private final Supplier<Stream<? extends MapFeature<?>>> features;
    private final Location referenceLocation;
    private final Set<String> categories = new TreeSet<>();

    private int offsetX;
    private int offsetY;

    // Relative to the root category, or a full category ID when no root is specified.
    private String selectedCategory;

    private ManagerSearchWidget categorySearchWidget;
    private CategoryTreeWidget categoryTreeWidget;
    private FeatureScrollBarWidget featureScrollBarWidget;

    private TexturedTextInputBoxWidget newCategoryInput;
    private StyledButton createCategoryButton;

    public FeatureCategoryScreen(
            Screen previous,
            String rootCategory,
            String category,
            Stream<String> categories,
            Supplier<Stream<? extends MapFeature<?>>> features,
            Location referenceLocation,
            Consumer<String> onSelect) {
        super(Component.translatable("screens.wynntils.featureCategory.title"));
        this.previous = previous;
        this.rootCategory = rootCategory;
        this.treeRoot = rootCategory.substring(rootCategory.lastIndexOf(':') + 1);
        this.selectedCategory = fromCategoryId(category);
        this.features = features;
        this.referenceLocation = referenceLocation;
        this.onSelect = onSelect;

        categories
                .filter(id -> rootCategory.isEmpty() || id.startsWith(rootCategory + ":"))
                .map(this::fromCategoryId)
                .forEach(this.categories::add);
        if (!selectedCategory.isEmpty()) {
            this.categories.add(selectedCategory);
        }
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
                offsetX + WIDTH_OFFSET + 200 + 5,
                offsetY + HEIGHT_OFFSET + 25 + 15,
                341,
                132,
                features,
                referenceLocation);
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
                Tooltip.create(Component.translatable("screens.wynntils.featureCategory.nameTooltip")));
        this.addRenderableWidget(newCategoryInput);

        createCategoryButton = new StyledButton(
                offsetX + WIDTH_OFFSET + 200 + 20 + 213 + 8,
                offsetY + HEIGHT_OFFSET + 177 + 57,
                100,
                20,
                Component.translatable("screens.wynntils.featureCategory.create"),
                Texture.MANAGER_WIDGET_BACKGROUND,
                this::createCategory);
        this.addRenderableWidget(createCategoryButton);

        this.addRenderableWidget(new StyledButton(
                offsetX + WIDTH_OFFSET + 200 + 5,
                offsetY + HEIGHT_OFFSET + 284 - 20,
                100,
                20,
                Component.translatable("screens.wynntils.featureCategory.back"),
                Texture.MANAGER_WIDGET_BACKGROUND_RED,
                this::onClose));

        this.addRenderableWidget(new StyledButton(
                offsetX + WIDTH_OFFSET + 200 + 5 + 100 + 8,
                offsetY + HEIGHT_OFFSET + 284 - 20,
                341 - 100 - 8,
                20,
                Component.translatable("screens.wynntils.featureCategory.useSelected"),
                Texture.MANAGER_WIDGET_BACKGROUND_GREEN,
                () -> {
                    onSelect.accept(getSelectedCategoryId());
                    onClose();
                }));

        // end region

        newCategoryInput.setTextBoxInput(inputText);
        createCategoryButton.active = isValidNewCategory(inputText);
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previous);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(guiGraphics, Texture.MANAGER_BACKGROUND, offsetX, offsetY);

        renderInfoPanel(guiGraphics);

        renderables.forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));
        renderTooltips(guiGraphics, mouseX, mouseY);
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
                                Component.translatable("screens.wynntils.featureCategory.selectedCategory")
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
                        StyledText.fromComponent(Component.translatable("screens.wynntils.featureCategory.categoryPath")
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
                        StyledText.fromComponent(Component.translatable("screens.wynntils.featureCategory.newCategory")
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
        if (!treeRoot.isEmpty()) {
            treeCategories.add(treeRoot);
        }
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
        if (rootCategory.isEmpty()) return selectedCategory;

        return selectedCategory.isEmpty() ? rootCategory : rootCategory + ":" + selectedCategory;
    }

    private String getSelectedCategoryName() {
        return selectedCategory.isEmpty()
                ? treeRoot
                : selectedCategory.substring(selectedCategory.lastIndexOf(':') + 1);
    }

    private String toTreeId(String category) {
        if (treeRoot.isEmpty()) return category;

        return category.isEmpty() ? treeRoot : treeRoot + ":" + category;
    }

    private String fromTreeId(String treeId) {
        if (treeRoot.isEmpty()) return treeId;

        return treeId.equals(treeRoot) ? "" : treeId.substring(treeRoot.length() + 1);
    }

    private String fromCategoryId(String categoryId) {
        if (rootCategory.isEmpty()) return categoryId;

        return categoryId.equals(rootCategory) ? "" : categoryId.substring(rootCategory.length() + 1);
    }
}
