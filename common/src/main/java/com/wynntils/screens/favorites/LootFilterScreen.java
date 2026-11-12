/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.favorites;

import com.google.gson.JsonSyntaxException;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.json.JsonManager;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.ColorPickerWidget;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.favorites.widgets.LootFilterButton;
import com.wynntils.screens.favorites.widgets.LootFilterInputWidget;
import com.wynntils.services.favorites.FavoriteRenderer;
import com.wynntils.services.favorites.type.FavoriteCategory;
import com.wynntils.services.favorites.type.FavoriteIcon;
import com.wynntils.services.favorites.type.LootFilter;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class LootFilterScreen extends WynntilsScreen {
    private static final String CATEGORY_CLIPBOARD_PREFIX = "wynntilsFavoriteCategory,";

    private static final int PANEL_WIDTH = 444;
    private static final int PANEL_HEIGHT = 244;
    private static final int PANEL_PADDING = 8;
    private static final int COLUMN_GAP = 6;
    private static final int LIST_WIDTH = 100;
    private static final int EDITOR_WIDTH = 216;
    private static final int LABEL_HEIGHT = 10;
    private static final int INPUT_HEIGHT = 20;
    private static final int BUTTON_HEIGHT = 16;
    private static final int BUTTON_GAP = 2;

    private static final int LIST_Y = 20;
    private static final int LIST_PADDING = 3;
    private static final int MAX_VISIBLE_ROWS = 6;
    private static final int ROW_PITCH = BUTTON_HEIGHT + BUTTON_GAP;
    private static final int LIST_HEIGHT = MAX_VISIBLE_ROWS * ROW_PITCH - BUTTON_GAP + LIST_PADDING * 2;
    private static final int SCROLL_BAR_SPACE = 12;
    private static final int SCROLL_BAR_PADDING = 4;
    private static final int SCROLL_BUTTON_PADDING = 11;
    private static final int ACTIONS_Y = LIST_Y + LIST_HEIGHT + 3;
    private static final int LOOT_FILTER_NAME_Y = ACTIONS_Y + ROW_PITCH * 2 + 2;

    private static final int NAME_Y = 21;
    private static final int ICON_Y = 54;
    private static final int ICON_COLOR_Y = 96;
    private static final int OVERLAY_COLOR_Y = 129;
    private static final int CLOSE_OVERRIDE_Y = 162;
    private static final int FOOTER_Y = 208;
    private static final int ICON_BUTTON_WIDTH = 50;
    private static final int ICON_INPUT_WIDTH = 26;
    private static final int COLOR_INPUT_WIDTH = 80;
    private static final int COLOR_COLUMN_WIDTH = 108;
    private static final int CLOSE_OVERRIDE_INPUT_WIDTH = 40;
    private static final int DONE_BUTTON_WIDTH = 60;
    private static final int PREVIEW_X = 142;

    private static final int PREVIEW_SCALE = 3;
    private static final int PREVIEW_MARGIN = 4;
    private static final int PREVIEW_SIZE = (FavoriteRenderer.SLOT_SIZE + 2 * PREVIEW_MARGIN) * PREVIEW_SCALE;
    private static final int RESIZE_HANDLE_SIZE = 2;
    private static final int MIN_ICON_SIZE = 4;
    private static final int MAX_ICON_SIZE = FavoriteRenderer.SLOT_SIZE;
    private static final int MAX_ICON_EDGE = FavoriteRenderer.SLOT_SIZE + PREVIEW_MARGIN;

    private static final ItemStack PREVIEW_ITEM = new ItemStack(Items.DIAMOND_SWORD);
    private static final CustomColor PREVIEW_SLOT_COLOR = new CustomColor(139, 139, 139);
    private static final CustomColor LABEL_COLOR = new CustomColor(64, 42, 20);

    private final Screen previousScreen;
    private final ListView lootFilterList = new ListView();
    private final ListView categoryList = new ListView();
    private final Set<AbstractWidget> invalidInputs = new HashSet<>();

    private int contentX;
    private int contentY;
    private int editorX;

    private FavoriteCategory selectedCategory;
    private DragMode dragMode = DragMode.NONE;
    private double iconGrabX;
    private double iconGrabY;
    private double listGrabY;

    private LootFilterButton activeLootFilterButton;
    private LootFilterButton selectedCategoryButton;
    private TextInputBoxWidget iconXInput;
    private TextInputBoxWidget iconYInput;
    private TextInputBoxWidget iconSizeInput;

    private LootFilterScreen(Screen previousScreen) {
        super(Component.literal("Loot Filter Screen"));

        this.previousScreen = previousScreen;
    }

    public static Screen create(Screen previousScreen) {
        return new LootFilterScreen(previousScreen);
    }

    @Override
    protected void doInit() {
        super.doInit();

        contentX = (this.width - PANEL_WIDTH) / 2 + PANEL_PADDING;
        contentY = (this.height - PANEL_HEIGHT) / 2 + PANEL_PADDING;
        lootFilterList.x = contentX;
        categoryList.x = lootFilterList.x + LIST_WIDTH + COLUMN_GAP;
        editorX = categoryList.x + LIST_WIDTH + COLUMN_GAP;

        LootFilter lootFilter = Services.Favorites.getActiveLootFilter();
        if (!lootFilter.getCategories().contains(selectedCategory)) {
            selectedCategory = lootFilter.getCategories().getFirst();
        }

        lootFilterList.scroll = clampScroll(lootFilterList.scroll, Services.Favorites.getLootFilters());
        categoryList.scroll = clampScroll(categoryList.scroll, lootFilter.getCategories());
        invalidInputs.clear();

        initLootFilterList(lootFilter);
        initCategoryList(lootFilter);
        initCategoryEditor();

        this.addRenderableWidget(new LootFilterButton(
                editorX + EDITOR_WIDTH - DONE_BUTTON_WIDTH,
                contentY + FOOTER_Y + 4,
                DONE_BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Component.translatable("screens.wynntils.lootFilters.done"),
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_BLUE,
                this::onClose));
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND,
                contentX - PANEL_PADDING,
                contentY - PANEL_PADDING,
                PANEL_WIDTH,
                PANEL_HEIGHT);

        // Separates the button that closes the screen from the columns above it
        RenderUtils.drawRect(
                guiGraphics, LABEL_COLOR, contentX, contentY + FOOTER_Y, PANEL_WIDTH - PANEL_PADDING * 2, 1);

        renderTitle(guiGraphics, "lootFilters", lootFilterList.x, LIST_WIDTH);
        renderTitle(guiGraphics, "categories", categoryList.x, LIST_WIDTH);
        renderTitle(guiGraphics, "editorTitle", editorX, EDITOR_WIDTH);

        renderList(guiGraphics, lootFilterList, Services.Favorites.getLootFilters());
        renderList(guiGraphics, categoryList, Services.Favorites.getCategories());

        renderLabel(guiGraphics, "lootFilterName", lootFilterList.x, LOOT_FILTER_NAME_Y);
        renderLabel(guiGraphics, "categoryName", editorX, NAME_Y);
        renderLabel(guiGraphics, "icon", editorX, ICON_Y);
        renderLabel(guiGraphics, "iconX", getIconInputX(0), ICON_Y);
        renderLabel(guiGraphics, "iconY", getIconInputX(1), ICON_Y);
        renderLabel(guiGraphics, "iconSize", getIconInputX(2), ICON_Y);
        renderLabel(guiGraphics, "iconColor", editorX, ICON_COLOR_Y);
        renderLabel(guiGraphics, "backgroundColor", editorX + COLOR_COLUMN_WIDTH, ICON_COLOR_Y);
        renderLabel(guiGraphics, "overlayColor", editorX, OVERLAY_COLOR_Y);
        renderLabel(guiGraphics, "borderColor", editorX + COLOR_COLUMN_WIDTH, OVERLAY_COLOR_Y);
        renderLabel(guiGraphics, "closeOverride", editorX, CLOSE_OVERRIDE_Y);

        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        for (AbstractWidget input : invalidInputs) {
            RenderUtils.drawRectBorders(
                    guiGraphics,
                    CommonColors.RED,
                    input.getX(),
                    input.getY(),
                    input.getX() + input.getWidth(),
                    input.getY() + input.getHeight(),
                    1);
        }

        renderPreview(guiGraphics);

        if (isListDrag()) {
            getDraggedListButton().render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (isInPreview(event.x(), event.y())) {
            startIconDrag(toSlotX(event.x()), toSlotY(event.y()));
            return true;
        }

        if (isOverScrollBar(event.x(), event.y(), lootFilterList, Services.Favorites.getLootFilters())) {
            dragMode = DragMode.LOOT_FILTER_SCROLL;
            dragScrollBar(lootFilterList, Services.Favorites.getLootFilters(), event.y());
            return true;
        }

        if (isOverScrollBar(event.x(), event.y(), categoryList, Services.Favorites.getCategories())) {
            dragMode = DragMode.CATEGORY_SCROLL;
            dragScrollBar(categoryList, Services.Favorites.getCategories(), event.y());
            return true;
        }

        TextInputBoxWidget focused = getFocusedTextInput();
        boolean handled = super.doMouseClicked(event, isDoubleClick);

        if (focused != null && !focused.isMouseOver(event.x(), event.y())) {
            setFocusedTextInput(null);
        }

        // Clicking a list entry selects it, so from here on the selected entry is the dragged one
        if (isOverListEntry(event.x(), event.y(), lootFilterList, Services.Favorites.getLootFilters())) {
            dragMode = DragMode.LOOT_FILTER;
        } else if (isOverListEntry(event.x(), event.y(), categoryList, Services.Favorites.getCategories())) {
            dragMode = DragMode.CATEGORY;
        }

        if (isListDrag()) {
            listGrabY = event.y() - getDraggedListButton().getY();
        }

        return handled;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        FavoriteCategory category = selectedCategory;

        switch (dragMode) {
            case LOOT_FILTER ->
                dragListEntry(
                        lootFilterList,
                        Services.Favorites.getLootFilters(),
                        Services.Favorites.getActiveLootFilter(),
                        event.y());
            case CATEGORY -> dragListEntry(categoryList, Services.Favorites.getCategories(), category, event.y());
            case LOOT_FILTER_SCROLL -> dragScrollBar(lootFilterList, Services.Favorites.getLootFilters(), event.y());
            case CATEGORY_SCROLL -> dragScrollBar(categoryList, Services.Favorites.getCategories(), event.y());
            case ICON_MOVE -> {
                int maxPosition = MAX_ICON_EDGE - category.getIconSize();
                setIconValue(iconXInput, clampRounded(toSlotX(event.x()) - iconGrabX, -PREVIEW_MARGIN, maxPosition));
                setIconValue(iconYInput, clampRounded(toSlotY(event.y()) - iconGrabY, -PREVIEW_MARGIN, maxPosition));
            }
            case ICON_RESIZE -> {
                double size =
                        Math.max(toSlotX(event.x()) - category.getIconX(), toSlotY(event.y()) - category.getIconY());
                setIconValue(iconSizeInput, clampRounded(size, MIN_ICON_SIZE, getMaxIconSize(category)));
            }
            case NONE -> {
                return super.mouseDragged(event, dragX, dragY);
            }
        }

        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean wasListDrag = isListDrag();
        dragMode = DragMode.NONE;
        if (wasListDrag) {
            this.rebuildWidgets();
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int rows = (int) -Math.signum(scrollY);

        if (isOverList(mouseX, mouseY, lootFilterList)) {
            scrollTo(lootFilterList, Services.Favorites.getLootFilters(), lootFilterList.scroll + rows);
            return true;
        }

        if (isOverList(mouseX, mouseY, categoryList)) {
            scrollTo(categoryList, Services.Favorites.getCategories(), categoryList.scroll + rows);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void removed() {
        Services.Favorites.touched();
        super.removed();
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previousScreen);
    }

    private void initLootFilterList(LootFilter activeLootFilter) {
        List<LootFilter> lootFilters = Services.Favorites.getLootFilters();

        activeLootFilterButton = null;
        for (int i = lootFilterList.scroll; i < lootFilterList.scroll + getVisibleRows(lootFilters); i++) {
            LootFilter lootFilter = lootFilters.get(i);
            LootFilterButton button = addListButton(
                    lootFilterList,
                    i,
                    lootFilter.getName(),
                    lootFilter == activeLootFilter,
                    () -> Services.Favorites.setActiveLootFilter(lootFilter));
            if (lootFilter == activeLootFilter) {
                activeLootFilterButton = button;
            }
        }

        addActionButton(lootFilterList, 0, 0, "new", Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_GREEN, true, () -> {
            Services.Favorites.addLootFilter(new LootFilter(I18n.get("screens.wynntils.lootFilters.newName")));
            lootFilterList.scroll = lootFilters.size();
        });
        addActionButton(lootFilterList, 0, 1, "copy", Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_BLUE, true, () -> {
            Services.Favorites.addLootFilter(activeLootFilter.copy());
            lootFilterList.scroll = lootFilters.size();
        });
        addActionButton(
                lootFilterList,
                1,
                0,
                "delete",
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_RED,
                lootFilters.size() > 1,
                () -> confirmDelete(activeLootFilter.getName(), () -> {
                    lootFilters.remove(activeLootFilter);
                    Services.Favorites.setActiveLootFilter(lootFilters.getFirst());
                }));

        addInput(lootFilterList.x, LOOT_FILTER_NAME_Y + LABEL_HEIGHT, LIST_WIDTH, activeLootFilter.getName(), name -> {
            // The active loot filter is identified by its name, so it has to stay unique
            boolean valid = !name.isBlank()
                    && lootFilters.stream()
                            .noneMatch(other ->
                                    other != activeLootFilter && other.getName().equals(name));
            if (valid) {
                activeLootFilter.setName(name);
                Services.Favorites.setActiveLootFilter(activeLootFilter);
                if (activeLootFilterButton != null) {
                    activeLootFilterButton.setMessage(Component.literal(name));
                }
            }
            return valid;
        });
    }

    private void initCategoryList(LootFilter lootFilter) {
        List<FavoriteCategory> categories = lootFilter.getCategories();

        selectedCategoryButton = null;
        for (int i = categoryList.scroll; i < categoryList.scroll + getVisibleRows(categories); i++) {
            FavoriteCategory category = categories.get(i);
            LootFilterButton button = addListButton(
                    categoryList,
                    i,
                    category.getName(),
                    category == selectedCategory,
                    () -> selectedCategory = category);
            if (category == selectedCategory) {
                selectedCategoryButton = button;
            }
        }

        addActionButton(categoryList, 0, 0, "new", Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_GREEN, true, () -> {
            selectedCategory = new FavoriteCategory(I18n.get("screens.wynntils.lootFilters.newName"));
            categories.add(selectedCategory);
            categoryList.scroll = categories.size();
        });
        addActionButton(
                categoryList,
                0,
                1,
                "delete",
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_RED,
                categories.size() > 1,
                () -> confirmDelete(selectedCategory.getName(), () -> categories.remove(selectedCategory)));
        addActionButton(
                categoryList,
                1,
                0,
                "import",
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_GOLD,
                true,
                () -> importCategory(categories));
        addActionButton(
                categoryList,
                1,
                1,
                "export",
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_BLUE,
                true,
                this::exportCategory);
    }

    private void importCategory(List<FavoriteCategory> categories) {
        String clipboard = McUtils.mc().keyboardHandler.getClipboard();

        FavoriteCategory category = null;
        if (clipboard != null && clipboard.startsWith(CATEGORY_CLIPBOARD_PREFIX)) {
            try {
                category = JsonManager.GSON.fromJson(
                        clipboard.substring(CATEGORY_CLIPBOARD_PREFIX.length()), FavoriteCategory.class);
            } catch (JsonSyntaxException e) {
                category = null;
            }
        }

        if (category == null || !category.isValid()) {
            McUtils.sendErrorToClient(I18n.get("screens.wynntils.lootFilters.invalidCategoryClipboard"));
            return;
        }

        // An item can only be in one category, so items that are already a favorite stay where they are
        category.getItems().removeAll(Services.Favorites.getFavoriteItems());
        categories.add(category);
        selectedCategory = category;
        categoryList.scroll = categories.size();

        McUtils.sendWynntilsPrefixMessage(
                Component.translatable("screens.wynntils.lootFilters.importedCategory", category.getName()));
    }

    private void exportCategory() {
        McUtils.mc()
                .keyboardHandler
                .setClipboard(CATEGORY_CLIPBOARD_PREFIX + JsonManager.GSON.toJson(selectedCategory));
        McUtils.sendWynntilsPrefixMessage(
                Component.translatable("screens.wynntils.lootFilters.exportedCategory", selectedCategory.getName()));
    }

    private void initCategoryEditor() {
        FavoriteCategory category = selectedCategory;

        addInput(editorX, NAME_Y + LABEL_HEIGHT, PREVIEW_X - COLUMN_GAP, category.getName(), name -> {
            if (name.isBlank()) return false;

            category.setName(name);
            if (selectedCategoryButton != null) {
                selectedCategoryButton.setMessage(Component.literal(name));
            }
            return true;
        });

        LootFilterButton[] iconButton = new LootFilterButton[1];
        iconButton[0] = new LootFilterButton(
                editorX,
                contentY + ICON_Y + LABEL_HEIGHT,
                ICON_BUTTON_WIDTH,
                INPUT_HEIGHT,
                Component.literal(EnumUtils.toNiceString(category.getIcon())),
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT,
                () -> {
                    FavoriteIcon[] icons = FavoriteIcon.values();
                    category.setIcon(icons[(category.getIcon().ordinal() + 1) % icons.length]);
                    iconButton[0].setMessage(Component.literal(EnumUtils.toNiceString(category.getIcon())));
                });
        this.addRenderableWidget(iconButton[0]);

        iconXInput = addIntInput(
                getIconInputX(0),
                category.getIconX(),
                x -> isValidIconPosition(x, category.getIconSize()),
                category::setIconX);
        iconYInput = addIntInput(
                getIconInputX(1),
                category.getIconY(),
                y -> isValidIconPosition(y, category.getIconSize()),
                category::setIconY);
        iconSizeInput = addIntInput(
                getIconInputX(2),
                category.getIconSize(),
                size -> size >= MIN_ICON_SIZE && size <= getMaxIconSize(category),
                category::setIconSize);

        addColorInput(editorX, ICON_COLOR_Y, category.getIconColor(), category::setIconColor);
        addColorInput(
                editorX + COLOR_COLUMN_WIDTH,
                ICON_COLOR_Y,
                category.getBackgroundColor(),
                category::setBackgroundColor);
        addColorInput(editorX, OVERLAY_COLOR_Y, category.getOverlayColor(), category::setOverlayColor);
        addColorInput(
                editorX + COLOR_COLUMN_WIDTH, OVERLAY_COLOR_Y, category.getBorderColor(), category::setBorderColor);

        addInput(
                editorX,
                CLOSE_OVERRIDE_Y + LABEL_HEIGHT,
                CLOSE_OVERRIDE_INPUT_WIDTH,
                Integer.toString(category.getLootChestCloseOverride()),
                text -> {
                    if (!text.matches("-1|\\d{1,3}")) return false;

                    category.setLootChestCloseOverride(Integer.parseInt(text));
                    return true;
                });
    }

    private void renderTitle(GuiGraphics guiGraphics, String key, int x, int width) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_MENU_RIBBON,
                x,
                contentY,
                width,
                Texture.BUILD_LOADOUTS_MENU_RIBBON.height());

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(I18n.get("screens.wynntils.lootFilters." + key)),
                        x + width / 2f,
                        contentY + Texture.BUILD_LOADOUTS_MENU_RIBBON.height() / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    private void renderLabel(GuiGraphics guiGraphics, String key, int x, int y) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(I18n.get("screens.wynntils.lootFilters." + key)),
                        x,
                        contentY + y,
                        LABEL_COLOR,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
    }

    private void renderList(GuiGraphics guiGraphics, ListView list, List<?> entries) {
        int listY = contentY + LIST_Y;

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT, list.x, listY, LIST_WIDTH, LIST_HEIGHT);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_SCROLL_SIDE_BAR,
                getScrollBarX(list),
                listY + SCROLL_BAR_PADDING,
                Texture.BUILD_LOADOUTS_SCROLL_SIDE_BAR.width(),
                LIST_HEIGHT - SCROLL_BAR_PADDING * 2);

        int maxScroll = entries.size() - getVisibleRows(entries);
        float buttonY = maxScroll == 0
                ? getScrollTrackTop()
                : MathUtils.map(list.scroll, 0, maxScroll, getScrollTrackTop(), getScrollTrackBottom());
        RenderUtils.drawTexturedRect(
                guiGraphics, Texture.BUILD_LOADOUTS_SCOLL_BAR_BUTTON, getScrollBarX(list) + 1, buttonY);
    }

    private void renderPreview(GuiGraphics guiGraphics) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT,
                getPreviewX(),
                getPreviewY(),
                PREVIEW_SIZE,
                PREVIEW_SIZE);

        // Everything inside is drawn in slot pixels, with the slot at the origin
        guiGraphics.pose().pushMatrix();
        guiGraphics
                .pose()
                .translate(
                        getPreviewX() + PREVIEW_MARGIN * PREVIEW_SCALE, getPreviewY() + PREVIEW_MARGIN * PREVIEW_SCALE);
        guiGraphics.pose().scale(PREVIEW_SCALE, PREVIEW_SCALE);

        RenderUtils.drawRect(
                guiGraphics, PREVIEW_SLOT_COLOR, 0, 0, FavoriteRenderer.SLOT_SIZE, FavoriteRenderer.SLOT_SIZE);
        FavoriteRenderer.renderBackground(guiGraphics, selectedCategory, 0, 0);
        RenderUtils.renderItem(guiGraphics, PREVIEW_ITEM, 0, 0);
        FavoriteRenderer.renderForeground(guiGraphics, selectedCategory, 0, 0);

        RenderUtils.drawRect(
                guiGraphics,
                CommonColors.WHITE,
                selectedCategory.getIconX() + selectedCategory.getIconSize() - RESIZE_HANDLE_SIZE,
                selectedCategory.getIconY() + selectedCategory.getIconSize() - RESIZE_HANDLE_SIZE,
                RESIZE_HANDLE_SIZE,
                RESIZE_HANDLE_SIZE);

        guiGraphics.pose().popMatrix();
    }

    private void confirmDelete(String name, Runnable delete) {
        McUtils.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        delete.run();
                    }
                    McUtils.setScreen(this);
                },
                Component.translatable("screens.wynntils.lootFilters.deleteTitle", name),
                Component.translatable("screens.wynntils.lootFilters.deleteMessage")));
    }

    private void startIconDrag(double slotX, double slotY) {
        FavoriteCategory category = selectedCategory;
        double iconRight = category.getIconX() + category.getIconSize();
        double iconBottom = category.getIconY() + category.getIconSize();

        if (slotX >= iconRight - RESIZE_HANDLE_SIZE
                && slotX <= iconRight
                && slotY >= iconBottom - RESIZE_HANDLE_SIZE
                && slotY <= iconBottom) {
            dragMode = DragMode.ICON_RESIZE;
            return;
        }

        dragMode = DragMode.ICON_MOVE;
        if (slotX >= category.getIconX() && slotX <= iconRight && slotY >= category.getIconY() && slotY <= iconBottom) {
            iconGrabX = slotX - category.getIconX();
            iconGrabY = slotY - category.getIconY();
        } else {
            iconGrabX = category.getIconSize() / 2d;
            iconGrabY = category.getIconSize() / 2d;
        }
    }

    private static void setIconValue(TextInputBoxWidget input, int value) {
        input.setTextBoxInput(Integer.toString(value));
    }

    private static int clampRounded(double value, int min, int max) {
        return MathUtils.clamp((int) Math.round(value), min, max);
    }

    private static boolean isValidIconPosition(int position, int iconSize) {
        return position >= -PREVIEW_MARGIN && position <= MAX_ICON_EDGE - iconSize;
    }

    private static int getMaxIconSize(FavoriteCategory category) {
        return Math.min(MAX_ICON_SIZE, MAX_ICON_EDGE - Math.max(category.getIconX(), category.getIconY()));
    }

    private int getIconInputX(int index) {
        return editorX + ICON_BUTTON_WIDTH + BUTTON_GAP + index * (ICON_INPUT_WIDTH + BUTTON_GAP);
    }

    private int getPreviewX() {
        return editorX + PREVIEW_X;
    }

    private int getPreviewY() {
        return contentY + NAME_Y;
    }

    private boolean isInPreview(double mouseX, double mouseY) {
        return mouseX >= getPreviewX()
                && mouseX < getPreviewX() + PREVIEW_SIZE
                && mouseY >= getPreviewY()
                && mouseY < getPreviewY() + PREVIEW_SIZE;
    }

    private double toSlotX(double mouseX) {
        return (mouseX - getPreviewX()) / PREVIEW_SCALE - PREVIEW_MARGIN;
    }

    private double toSlotY(double mouseY) {
        return (mouseY - getPreviewY()) / PREVIEW_SCALE - PREVIEW_MARGIN;
    }

    private boolean isListDrag() {
        return dragMode == DragMode.LOOT_FILTER || dragMode == DragMode.CATEGORY;
    }

    private LootFilterButton getDraggedListButton() {
        return dragMode == DragMode.LOOT_FILTER ? activeLootFilterButton : selectedCategoryButton;
    }

    private <T> void dragListEntry(ListView list, List<T> entries, T dragged, double mouseY) {
        int lastRow = getVisibleRows(entries) - 1;
        int row = getListRow(mouseY);

        int scroll = clampScroll(list.scroll + (row < 0 ? -1 : row > lastRow ? 1 : 0), entries);
        int targetIndex = scroll + MathUtils.clamp(row, 0, lastRow);

        if (scroll != list.scroll || targetIndex != entries.indexOf(dragged)) {
            list.scroll = scroll;
            entries.remove(dragged);
            entries.add(targetIndex, dragged);
            this.rebuildWidgets();
        }

        getDraggedListButton().setY(MathUtils.clamp((int) (mouseY - listGrabY), getListRowY(0), getListRowY(lastRow)));
    }

    private void scrollTo(ListView list, List<?> entries, int scroll) {
        int clampedScroll = clampScroll(scroll, entries);
        if (clampedScroll == list.scroll) return;

        list.scroll = clampedScroll;
        this.rebuildWidgets();
    }

    private static int clampScroll(int scroll, List<?> entries) {
        return MathUtils.clamp(scroll, 0, entries.size() - getVisibleRows(entries));
    }

    private static int getVisibleRows(List<?> entries) {
        return Math.min(entries.size(), MAX_VISIBLE_ROWS);
    }

    private int getListRowY(int row) {
        return contentY + LIST_Y + LIST_PADDING + row * ROW_PITCH;
    }

    private int getListRow(double mouseY) {
        return (int) Math.floor((mouseY - getListRowY(0)) / ROW_PITCH);
    }

    private boolean isOverList(double mouseX, double mouseY, ListView list) {
        return mouseX >= list.x
                && mouseX < list.x + LIST_WIDTH
                && mouseY >= contentY + LIST_Y
                && mouseY < contentY + LIST_Y + LIST_HEIGHT;
    }

    private boolean isOverListEntry(double mouseX, double mouseY, ListView list, List<?> entries) {
        int row = getListRow(mouseY);
        return isOverList(mouseX, mouseY, list)
                && mouseX < getScrollBarX(list)
                && row >= 0
                && row < getVisibleRows(entries);
    }

    private boolean isOverScrollBar(double mouseX, double mouseY, ListView list, List<?> entries) {
        return entries.size() > MAX_VISIBLE_ROWS && isOverList(mouseX, mouseY, list) && mouseX >= getScrollBarX(list);
    }

    private int getScrollBarX(ListView list) {
        return list.x + LIST_WIDTH - SCROLL_BAR_SPACE;
    }

    private int getScrollTrackTop() {
        return contentY + LIST_Y + SCROLL_BUTTON_PADDING;
    }

    private int getScrollTrackBottom() {
        return contentY
                + LIST_Y
                + LIST_HEIGHT
                - Texture.BUILD_LOADOUTS_SCOLL_BAR_BUTTON.height()
                - SCROLL_BUTTON_PADDING;
    }

    private void dragScrollBar(ListView list, List<?> entries, double mouseY) {
        double buttonY = mouseY - Texture.BUILD_LOADOUTS_SCOLL_BAR_BUTTON.height() / 2d;
        double progress = (buttonY - getScrollTrackTop()) / (getScrollTrackBottom() - getScrollTrackTop());
        scrollTo(list, entries, (int) Math.round(progress * (entries.size() - MAX_VISIBLE_ROWS)));
    }

    private LootFilterButton addListButton(ListView list, int index, String name, boolean selected, Runnable onPress) {
        return this.addRenderableWidget(new LootFilterButton(
                list.x + LIST_PADDING,
                getListRowY(index - list.scroll),
                LIST_WIDTH - LIST_PADDING - SCROLL_BAR_SPACE,
                BUTTON_HEIGHT,
                Component.literal(name),
                selected ? Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_BLUE : Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND,
                () -> {
                    onPress.run();
                    this.rebuildWidgets();
                }));
    }

    private void addActionButton(
            ListView list, int row, int column, String key, Texture texture, boolean active, Runnable onPress) {
        int buttonWidth = (LIST_WIDTH - BUTTON_GAP) / 2;
        LootFilterButton button = new LootFilterButton(
                list.x + column * (buttonWidth + BUTTON_GAP),
                contentY + ACTIONS_Y + row * ROW_PITCH,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.translatable("screens.wynntils.lootFilters." + key),
                texture,
                () -> {
                    onPress.run();
                    this.rebuildWidgets();
                });
        button.active = active;
        this.addRenderableWidget(button);
    }

    private TextInputBoxWidget addInput(int x, int y, int width, String value, Predicate<String> onUpdate) {
        TextInputBoxWidget[] input = new TextInputBoxWidget[1];
        input[0] = new LootFilterInputWidget(
                x,
                contentY + y,
                width,
                text -> {
                    if (onUpdate.test(text)) {
                        invalidInputs.remove(input[0]);
                    } else {
                        invalidInputs.add(input[0]);
                    }
                },
                this);
        input[0].setTextBoxInput(value);
        this.addRenderableWidget(input[0]);
        return input[0];
    }

    private TextInputBoxWidget addIntInput(int x, int value, IntPredicate validator, Consumer<Integer> setter) {
        return addInput(x, ICON_Y + LABEL_HEIGHT, ICON_INPUT_WIDTH, Integer.toString(value), text -> {
            if (!text.matches("-?\\d{1,2}") || !validator.test(Integer.parseInt(text))) return false;

            setter.accept(Integer.parseInt(text));
            return true;
        });
    }

    private void addColorInput(int x, int y, CustomColor color, Consumer<CustomColor> setter) {
        TextInputBoxWidget input = addInput(x, y + LABEL_HEIGHT, COLOR_INPUT_WIDTH, color.toHexString(), text -> {
            CustomColor newColor = CustomColor.fromHexString(text);
            if (newColor == CustomColor.NONE) return false;

            setter.accept(newColor);
            return true;
        });

        this.addRenderableWidget(new ColorPickerWidget(
                x + COLOR_INPUT_WIDTH + BUTTON_GAP, contentY + y + LABEL_HEIGHT, INPUT_HEIGHT, INPUT_HEIGHT, input));
    }

    private static final class ListView {
        private int x;
        private int scroll = 0;
    }

    private enum DragMode {
        NONE,
        LOOT_FILTER,
        CATEGORY,
        LOOT_FILTER_SCROLL,
        CATEGORY_SCROLL,
        ICON_MOVE,
        ICON_RESIZE
    }
}
