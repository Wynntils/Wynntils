/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.AbstractSideListScreen;
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
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class WaypointCategoryScreen extends AbstractSideListScreen {
    private static final String DEFAULT_CATEGORY = "wynntils:personal:waypoint";
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^[a-z0-9:-]+$");

    private final List<String> categoriesList = new ArrayList<>();
    private final WaypointCreationScreen creationScreen;

    private Button useNewCategoryButton;
    private String categoryPath;
    private String currentCategory;
    private Set<String> allCategories;
    private TextInputBoxWidget newCategoryInput;

    private WaypointCategoryScreen(WaypointCreationScreen creationScreen, String categoryPath) {
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

    public static Screen create(WaypointCreationScreen creationScreen, String categoryPath) {
        return new WaypointCategoryScreen(creationScreen, categoryPath);
    }

    @Override
    protected void doInit() {
        super.doInit();

        newCategoryInput = new TextInputBoxWidget(
                (int) (dividedWidth * 49),
                (int) (dividedHeight * 27),
                (int) (dividedWidth * 14),
                BUTTON_SIZE,
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
                        .pos((int) (dividedWidth * 35), (int) (dividedHeight * 33))
                        .size((int) (dividedWidth * 12), BUTTON_SIZE)
                        .build());

        useNewCategoryButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointCategory.useNew"), (button) -> {
                            creationScreen.setCategory(newCategoryInput.getTextBoxInput());
                            onClose();
                        })
                .pos((int) (dividedWidth * 50), (int) (dividedHeight * 33))
                .size((int) (dividedWidth * 12), BUTTON_SIZE)
                .build();
        useNewCategoryButton.active =
                CATEGORY_PATTERN.matcher(newCategoryInput.getTextBoxInput()).matches();
        this.addRenderableWidget(useNewCategoryButton);

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.waypointCategory.back"), (button) -> onClose())
                .pos((int) (dividedWidth * 45), (int) (dividedHeight * 39))
                .size((int) (dividedWidth * 7), BUTTON_SIZE)
                .build());

        if (!categoryPath.isEmpty()) {
            newCategoryInput.setTextBoxInput(categoryPath);
        }

        populateCategories();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawRect(
                poseStack,
                CommonColors.BLACK.withAlpha(100),
                dividedWidth * 33,
                dividedHeight * 18,
                0,
                dividedWidth * 31,
                dividedHeight * 26);

        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.waypointCategory.currentCategory") + ":"),
                        dividedWidth * 34,
                        dividedHeight * 21,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderScrollingText(
                        poseStack,
                        StyledText.fromString(currentCategory),
                        dividedWidth * 49,
                        dividedHeight * 21,
                        dividedWidth * 14,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.waypointCategory.categoryPath") + ":"),
                        dividedWidth * 34,
                        dividedHeight * 25,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderScrollingText(
                        poseStack,
                        StyledText.fromString(categoryPath),
                        dividedWidth * 49,
                        dividedHeight * 25,
                        dividedWidth * 14,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.waypointCategory.newCategory") + ":"),
                        dividedWidth * 34,
                        dividedHeight * 29,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(creationScreen);
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

    protected StyledText getEmptyListText() {
        return StyledText.fromComponent(Component.translatable("screens.wynntils.waypointCategory.noCategories"));
    }

    private void populateCategories() {
        sideListWidgets = new ArrayList<>();

        int renderY = 0;

        if (!currentCategory.isEmpty()) {
            CategoryWidget rootCategoryWidget = new CategoryWidget(
                    renderY, (int) (dividedWidth * 32), (int) (dividedHeight * 4), currentCategory, true);
            sideListWidgets.add(rootCategoryWidget);

            renderY += (int) (dividedHeight * 4);
        }

        for (String category : categoriesList) {
            CategoryWidget categoryWidget =
                    new CategoryWidget(renderY, (int) (dividedWidth * 32), (int) (dividedHeight * 4), category, false);
            sideListWidgets.add(categoryWidget);

            renderY += (int) (dividedHeight * 4);
        }

        scroll(scrollOffset);
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
