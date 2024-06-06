/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.WynntilsGridLayoutScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.widgets.CategoryWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class WaypointCategoryScreen extends WynntilsGridLayoutScreen {
    private static final int CATEGORIES_PER_PAGE = 16;
    private static final String DEFAULT_CATEGORY = "wynntils:personal:waypoint";
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^[a-zA-Z0-9:-]+$");

    private final List<String> categoriesList = new ArrayList<>();
    private final PoiCreationScreen creationScreen;
    private List<CategoryWidget> categoryWidgets = new ArrayList<>();

    private Button useNewCategoryButton;
    private String categoryPath;
    private String currentCategory = "";
    private Set<String> allCategories;
    private TextInputBoxWidget newCategoryInput;

    private WaypointCategoryScreen(PoiCreationScreen creationScreen, String categoryPath) {
        super(Component.literal("Waypoint Category Screen"));

        this.creationScreen = creationScreen;
        this.categoryPath = categoryPath;

        String[] pathList = this.categoryPath.split(":");

        if (pathList.length == 1) {
            currentCategory = pathList[0];
        } else {
            currentCategory = pathList[pathList.length - 1];
        }

        allCategories = Services.Waypoints.getCategories();
        // Get all of the categories and their subcategories
        allCategories = allCategories.stream()
                .map(category -> {
                    if (category.equals(DEFAULT_CATEGORY)) {
                        return "";
                    } else {
                        return category.substring(DEFAULT_CATEGORY.length() + 1); // +1 for the ":" on end
                    }
                })
                .filter(category -> !category.isEmpty())
                .collect(Collectors.toSet());

        updateCategoryList();
    }

    public static Screen create(PoiCreationScreen creationScreen, String categoryPath) {
        return new WaypointCategoryScreen(creationScreen, categoryPath);
    }

    @Override
    protected void doInit() {
        super.doInit();

        newCategoryInput = new TextInputBoxWidget(
                (int) (dividedWidth * 49),
                (int) (dividedHeight * 30),
                (int) (dividedWidth * 14),
                BUTTON_HEIGHT,
                (s) -> useNewCategoryButton.active =
                        CATEGORY_PATTERN.matcher(s).matches() && !s.equals(categoryPath) && !s.endsWith(":"),
                this,
                newCategoryInput);
        newCategoryInput.setTooltip(
                Tooltip.create(Component.translatable("screens.wynntils.waypointCategory.nameTooltip")));

        this.addRenderableWidget(newCategoryInput);

        this.addRenderableWidget(
                new Button.Builder(Component.translatable("screens.wynntils.waypointCategory.useCurrent"), (button) -> {
                            creationScreen.setCategory(categoryPath);
                            onClose();
                        })
                        .pos((int) (dividedWidth * 35), (int) (dividedHeight * 36))
                        .size((int) (dividedWidth * 12), BUTTON_HEIGHT)
                        .build());

        useNewCategoryButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointCategory.useNew"), (button) -> {
                            creationScreen.setCategory(newCategoryInput.getTextBoxInput());
                            onClose();
                        })
                .pos((int) (dividedWidth * 50), (int) (dividedHeight * 36))
                .size((int) (dividedWidth * 12), BUTTON_HEIGHT)
                .build();
        useNewCategoryButton.active =
                CATEGORY_PATTERN.matcher(newCategoryInput.getTextBoxInput()).matches();
        this.addRenderableWidget(useNewCategoryButton);

        if (!categoryPath.isEmpty()) {
            newCategoryInput.setTextBoxInput(categoryPath);
        }

        populateCategories();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                guiGraphics.pose(),
                CommonColors.BLACK.withAlpha(100),
                dividedWidth * 33,
                dividedHeight * 22,
                0,
                dividedWidth * 31,
                dividedHeight * 20);

        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        for (CategoryWidget categoryWidget : categoryWidgets) {
            categoryWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (categoryWidgets.size() > CATEGORIES_PER_PAGE) {
            // Render scroll bar
        }

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics.pose(),
                        StyledText.fromString(
                                I18n.get("screens.wynntils.waypointCategory.currentCategory") + ": " + currentCategory),
                        dividedWidth * 34,
                        dividedHeight * 24,
                        dividedWidth * 30,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1);

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics.pose(),
                        StyledText.fromString(
                                I18n.get("screens.wynntils.waypointCategory.categoryPath") + ": " + categoryPath),
                        dividedWidth * 34,
                        dividedHeight * 28,
                        dividedWidth * 30,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString(I18n.get("screens.wynntils.waypointCategory.newCategory") + ":"),
                        dividedWidth * 34,
                        dividedHeight * 32,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1);
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(creationScreen);
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryWidget widget : categoryWidgets) {
            if (widget.isMouseOver(mouseX, mouseY)) {
                return widget.mouseClicked(mouseX, mouseY, button);
            }
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    public void selectCategory(String category) {
        currentCategory = category;

        if (categoryPath.isEmpty()) {
            categoryPath += category;
        } else {
            categoryPath += ":" + category;
        }

        newCategoryInput.setTextBoxInput(categoryPath);
        updateCategoryList();
        populateCategories();
    }

    public void selectPreviousCategory() {
        String[] pathList = categoryPath.split(":");

        if (pathList.length == 1) {
            currentCategory = "";
        } else {
            currentCategory = pathList[pathList.length - 2];
        }

        String[] newPathList = Arrays.copyOfRange(pathList, 0, pathList.length - 1);
        categoryPath = String.join(":", newPathList);

        newCategoryInput.setTextBoxInput(categoryPath);
        updateCategoryList();
        populateCategories();
    }

    private void populateCategories() {
        categoryWidgets = new ArrayList<>();

        int renderY = 0;

        if (!currentCategory.isEmpty()) {
            CategoryWidget rootCategoryWidget = new CategoryWidget(
                    renderY, (int) (dividedWidth * 32), (int) (dividedHeight * 4), currentCategory, true);
            categoryWidgets.add(rootCategoryWidget);

            renderY += (int) (dividedHeight * 4);
        }

        for (String category : categoriesList) {
            CategoryWidget categoryWidget =
                    new CategoryWidget(renderY, (int) (dividedWidth * 32), (int) (dividedHeight * 4), category, false);
            categoryWidgets.add(categoryWidget);

            renderY += (int) (dividedHeight * 4);
        }
    }

    private void updateCategoryList() {
        categoriesList.clear();

        categoriesList.addAll(allCategories.stream()
                .filter(category -> category.startsWith(categoryPath + ":") || categoryPath.isEmpty())
                .map(category -> {
                    if (categoryPath.isEmpty()) {
                        return category.split(":")[0];
                    } else {
                        return category.split(":")[categoryPath.split(":").length];
                    }
                })
                .filter(category -> !category.isEmpty())
                .collect(Collectors.toSet()));
    }
}
