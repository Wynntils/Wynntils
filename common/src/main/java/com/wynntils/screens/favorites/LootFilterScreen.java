/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.favorites;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.WynntilsGridLayoutScreen;
import com.wynntils.screens.base.widgets.ColorPickerWidget;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
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
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class LootFilterScreen extends WynntilsGridLayoutScreen {

    private static final int MAX_VISIBLE_ROWS = 8;
    private static final int SCROLL_BAR_WIDTH = 3;
    private static final CustomColor SCROLL_BAR_TRACK_COLOR = new CustomColor(0, 0, 0, 150);

    private static final int FILTERS_X = 3;
    private static final int CATEGORIES_X = 19;
    private static final int LIST_WIDTH = 14;
    private static final int EDITOR_X = 36;
    private static final int EDITOR_SECOND_COLUMN_X = 50;
    private static final int EDITOR_COLUMN_WIDTH = 10;
    private static final int ICON_X_INPUT_X = 45;
    private static final int ICON_Y_INPUT_X = 50;
    private static final int ICON_SIZE_INPUT_X = 55;
    private static final int ICON_INPUT_WIDTH = 4;

    private static final int HEADER_ROW_Y = 6;
    private static final int LIST_FIRST_ROW_Y = 8;
    private static final int LIST_ROW_HEIGHT = 5;
    private static final int NAME_ROW_Y = 11;
    private static final int ICON_ROW_Y = 19;
    private static final int ICON_COLOR_ROW_Y = 27;
    private static final int OVERLAY_COLOR_ROW_Y = 35;
    private static final int CLOSE_OVERRIDE_ROW_Y = 43;
    private static final int PREVIEW_ROW_Y = 45;
    private static final int DONE_ROW_Y = 56;

    private static final int PREVIEW_SCALE = 3;
    private static final int PREVIEW_MARGIN = 4;
    private static final int PREVIEW_SIZE = (FavoriteRenderer.SLOT_SIZE + 2 * PREVIEW_MARGIN) * PREVIEW_SCALE;
    private static final int RESIZE_HANDLE_SIZE = 2;
    private static final int MIN_ICON_SIZE = 4;
    private static final int MAX_ICON_SIZE = FavoriteRenderer.SLOT_SIZE;
    private static final int MAX_ICON_EDGE = FavoriteRenderer.SLOT_SIZE + PREVIEW_MARGIN;

    private static final ItemStack PREVIEW_ITEM = new ItemStack(Items.DIAMOND_SWORD);
    private static final CustomColor PREVIEW_BACKGROUND_COLOR = new CustomColor(0, 0, 0, 150);
    private static final CustomColor PREVIEW_SLOT_COLOR = new CustomColor(139, 139, 139);

    private final Screen previousScreen;
    private final ListView lootFilterList = new ListView(FILTERS_X);
    private final ListView categoryList = new ListView(CATEGORIES_X);

    private FavoriteCategory selectedCategory;
    private DragMode dragMode = DragMode.NONE;

    private double iconGrabX;
    private double iconGrabY;

    private double listGrabY;

    private Button activeLootFilterButton;
    private Button selectedCategoryButton;
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

        LootFilter lootFilter = Services.Favorites.getActiveLootFilter();
        if (!lootFilter.getCategories().contains(selectedCategory)) {
            selectedCategory = lootFilter.getCategories().getFirst();
        }

        lootFilterList.scroll = clampScroll(lootFilterList.scroll, Services.Favorites.getLootFilters());
        categoryList.scroll = clampScroll(categoryList.scroll, lootFilter.getCategories());

        initLootFilterList(lootFilter);
        initCategoryList(lootFilter);
        initCategoryEditor();

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.lootFilters.done"), (button) -> this.onClose())
                .pos((int) (dividedWidth * EDITOR_X), (int) (dividedHeight * DONE_ROW_Y))
                .size((int) (dividedWidth * 8), BUTTON_SIZE)
                .build());
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        renderLabel(guiGraphics, "lootFilters", FILTERS_X, HEADER_ROW_Y);
        renderLabel(guiGraphics, "categories", CATEGORIES_X, HEADER_ROW_Y);
        renderLabel(guiGraphics, "lootFilterName", FILTERS_X, getLootFilterNameRowY());

        renderLabel(guiGraphics, "categoryName", EDITOR_X, NAME_ROW_Y);
        renderLabel(guiGraphics, "icon", EDITOR_X, ICON_ROW_Y);
        renderLabel(guiGraphics, "iconX", ICON_X_INPUT_X, ICON_ROW_Y);
        renderLabel(guiGraphics, "iconY", ICON_Y_INPUT_X, ICON_ROW_Y);
        renderLabel(guiGraphics, "iconSize", ICON_SIZE_INPUT_X, ICON_ROW_Y);
        renderLabel(guiGraphics, "iconColor", EDITOR_X, ICON_COLOR_ROW_Y);
        renderLabel(guiGraphics, "backgroundColor", EDITOR_SECOND_COLUMN_X, ICON_COLOR_ROW_Y);
        renderLabel(guiGraphics, "overlayColor", EDITOR_X, OVERLAY_COLOR_ROW_Y);
        renderLabel(guiGraphics, "borderColor", EDITOR_SECOND_COLUMN_X, OVERLAY_COLOR_ROW_Y);
        renderLabel(guiGraphics, "closeOverride", EDITOR_X, CLOSE_OVERRIDE_ROW_Y);

        renderPreview(guiGraphics);
        renderScrollBar(guiGraphics, lootFilterList, Services.Favorites.getLootFilters());
        renderScrollBar(guiGraphics, categoryList, Services.Favorites.getCategories());

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

        boolean handled = super.doMouseClicked(event, isDoubleClick);

        // Clicking a list entry selects it, so from here on the selected entry is the dragged one
        if (isInList(event.x(), event.y(), lootFilterList, Services.Favorites.getLootFilters())) {
            dragMode = DragMode.LOOT_FILTER;
        } else if (isInList(event.x(), event.y(), categoryList, Services.Favorites.getCategories())) {
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

        if (isInList(mouseX, mouseY, lootFilterList, Services.Favorites.getLootFilters())) {
            scrollTo(lootFilterList, Services.Favorites.getLootFilters(), lootFilterList.scroll + rows);
            return true;
        }

        if (isInList(mouseX, mouseY, categoryList, Services.Favorites.getCategories())) {
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
            Button button = addListButton(
                    lootFilterList,
                    i,
                    lootFilter.getName(),
                    lootFilter == activeLootFilter,
                    () -> Services.Favorites.setActiveLootFilter(lootFilter));
            if (lootFilter == activeLootFilter) {
                activeLootFilterButton = button;
            }
        }

        int actionsY = LIST_FIRST_ROW_Y + getVisibleRows(lootFilters) * LIST_ROW_HEIGHT;
        addActionButton(FILTERS_X, actionsY, 0, "new", ChatFormatting.GREEN, true, () -> {
            Services.Favorites.addLootFilter(new LootFilter(I18n.get("screens.wynntils.lootFilters.newName")));
            lootFilterList.scroll = lootFilters.size();
        });
        addActionButton(FILTERS_X, actionsY, 1, "copy", ChatFormatting.BLUE, true, () -> {
            Services.Favorites.addLootFilter(activeLootFilter.copy());
            lootFilterList.scroll = lootFilters.size();
        });
        addActionButton(
                FILTERS_X,
                actionsY,
                2,
                "delete",
                ChatFormatting.RED,
                lootFilters.size() > 1,
                () -> confirmDelete(activeLootFilter.getName(), () -> {
                    lootFilters.remove(activeLootFilter);
                    Services.Favorites.setActiveLootFilter(lootFilters.getFirst());
                }));

        addInput(FILTERS_X, getLootFilterNameRowY(), LIST_WIDTH, activeLootFilter.getName(), name -> {
            // The active loot filter is identified by its name, so it has to stay unique
            boolean valid = !name.isBlank()
                    && lootFilters.stream()
                            .noneMatch(other ->
                                    other != activeLootFilter && other.getName().equals(name));
            if (valid) {
                activeLootFilter.setName(name);
                Services.Favorites.setActiveLootFilter(activeLootFilter);
                if (activeLootFilterButton != null) {
                    activeLootFilterButton.setMessage(getListButtonMessage(name, true));
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
            Button button = addListButton(
                    categoryList,
                    i,
                    category.getName(),
                    category == selectedCategory,
                    () -> selectedCategory = category);
            if (category == selectedCategory) {
                selectedCategoryButton = button;
            }
        }

        int selectedIndex = categories.indexOf(selectedCategory);
        int actionsY = LIST_FIRST_ROW_Y + getVisibleRows(categories) * LIST_ROW_HEIGHT;
        addActionButton(CATEGORIES_X, actionsY, 0, "new", ChatFormatting.GREEN, true, () -> {
            selectedCategory = new FavoriteCategory(I18n.get("screens.wynntils.lootFilters.newName"));
            categories.add(selectedCategory);
            categoryList.scroll = categories.size();
        });
        // The order of the categories decides which one wins if an item matches several
        addActionButton(
                CATEGORIES_X,
                actionsY,
                1,
                "moveUp",
                ChatFormatting.WHITE,
                selectedIndex > 0,
                () -> categories.set(selectedIndex, categories.set(selectedIndex - 1, selectedCategory)));
        addActionButton(
                CATEGORIES_X,
                actionsY,
                2,
                "delete",
                ChatFormatting.RED,
                categories.size() > 1,
                () -> confirmDelete(selectedCategory.getName(), () -> categories.remove(selectedCategory)));
    }

    private void initCategoryEditor() {
        FavoriteCategory category = selectedCategory;

        addInput(EDITOR_X, NAME_ROW_Y, EDITOR_COLUMN_WIDTH, category.getName(), name -> {
            if (name.isBlank()) return false;

            category.setName(name);
            if (selectedCategoryButton != null) {
                selectedCategoryButton.setMessage(getListButtonMessage(name, true));
            }
            return true;
        });

        this.addRenderableWidget(new Button.Builder(
                        Component.literal(EnumUtils.toNiceString(category.getIcon())), (button) -> {
                            FavoriteIcon[] icons = FavoriteIcon.values();
                            category.setIcon(icons[(category.getIcon().ordinal() + 1) % icons.length]);
                            button.setMessage(Component.literal(EnumUtils.toNiceString(category.getIcon())));
                        })
                .pos((int) (dividedWidth * EDITOR_X), (int) (dividedHeight * ICON_ROW_Y))
                .size((int) (dividedWidth * 8), BUTTON_SIZE)
                .build());

        iconXInput = addIntInput(
                ICON_X_INPUT_X,
                category.getIconX(),
                x -> isValidIconPosition(x, category.getIconSize()),
                category::setIconX);
        iconYInput = addIntInput(
                ICON_Y_INPUT_X,
                category.getIconY(),
                y -> isValidIconPosition(y, category.getIconSize()),
                category::setIconY);
        iconSizeInput = addIntInput(
                ICON_SIZE_INPUT_X,
                category.getIconSize(),
                size -> size >= MIN_ICON_SIZE && size <= getMaxIconSize(category),
                category::setIconSize);

        addColorInput(EDITOR_X, ICON_COLOR_ROW_Y, category.getIconColor(), category::setIconColor);
        addColorInput(
                EDITOR_SECOND_COLUMN_X, ICON_COLOR_ROW_Y, category.getBackgroundColor(), category::setBackgroundColor);
        addColorInput(EDITOR_X, OVERLAY_COLOR_ROW_Y, category.getOverlayColor(), category::setOverlayColor);
        addColorInput(EDITOR_SECOND_COLUMN_X, OVERLAY_COLOR_ROW_Y, category.getBorderColor(), category::setBorderColor);

        addInput(
                EDITOR_X,
                CLOSE_OVERRIDE_ROW_Y,
                EDITOR_COLUMN_WIDTH,
                Integer.toString(category.getLootChestCloseOverride()),
                text -> {
                    if (!text.matches("-1|\\d{1,3}")) return false;

                    category.setLootChestCloseOverride(Integer.parseInt(text));
                    return true;
                });
    }

    private void renderPreview(GuiGraphics guiGraphics) {
        RenderUtils.drawRect(
                guiGraphics, PREVIEW_BACKGROUND_COLOR, getPreviewX(), getPreviewY(), PREVIEW_SIZE, PREVIEW_SIZE);

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

    private int getPreviewX() {
        return (int) (dividedWidth * EDITOR_SECOND_COLUMN_X);
    }

    private int getPreviewY() {
        return (int) (dividedHeight * PREVIEW_ROW_Y);
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

    private Button getDraggedListButton() {
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
        return (int) (dividedHeight * (LIST_FIRST_ROW_Y + row * LIST_ROW_HEIGHT));
    }

    private int getListRow(double mouseY) {
        return (int) Math.floor((mouseY - dividedHeight * LIST_FIRST_ROW_Y) / (dividedHeight * LIST_ROW_HEIGHT));
    }

    private boolean isInList(double mouseX, double mouseY, ListView list, List<?> entries) {
        int row = getListRow(mouseY);
        return mouseX >= dividedWidth * list.gridX
                && mouseX < dividedWidth * (list.gridX + LIST_WIDTH)
                && row >= 0
                && row < getVisibleRows(entries);
    }

    private int getScrollBarX(ListView list) {
        return (int) (dividedWidth * (list.gridX + LIST_WIDTH)) + 1;
    }

    private int getListHeight(List<?> entries) {
        return getListRowY(getVisibleRows(entries) - 1) + BUTTON_SIZE - getListRowY(0);
    }

    private float getScrollThumbHeight(List<?> entries) {
        return getListHeight(entries) * (float) getVisibleRows(entries) / entries.size();
    }

    private boolean isOverScrollBar(double mouseX, double mouseY, ListView list, List<?> entries) {
        return entries.size() > MAX_VISIBLE_ROWS
                && mouseX >= getScrollBarX(list)
                && mouseX < getScrollBarX(list) + SCROLL_BAR_WIDTH
                && mouseY >= getListRowY(0)
                && mouseY < getListRowY(0) + getListHeight(entries);
    }

    private void dragScrollBar(ListView list, List<?> entries, double mouseY) {
        float thumbHeight = getScrollThumbHeight(entries);
        double progress = (mouseY - getListRowY(0) - thumbHeight / 2) / (getListHeight(entries) - thumbHeight);
        scrollTo(list, entries, (int) Math.round(progress * (entries.size() - MAX_VISIBLE_ROWS)));
    }

    private void renderScrollBar(GuiGraphics guiGraphics, ListView list, List<?> entries) {
        if (entries.size() <= MAX_VISIBLE_ROWS) return;

        int height = getListHeight(entries);
        float thumbHeight = getScrollThumbHeight(entries);
        float thumbY = getListRowY(0) + (height - thumbHeight) * list.scroll / (entries.size() - MAX_VISIBLE_ROWS);

        RenderUtils.drawRect(
                guiGraphics, SCROLL_BAR_TRACK_COLOR, getScrollBarX(list), getListRowY(0), SCROLL_BAR_WIDTH, height);
        RenderUtils.drawRect(
                guiGraphics, CommonColors.WHITE, getScrollBarX(list), thumbY, SCROLL_BAR_WIDTH, thumbHeight);
    }

    private int getLootFilterNameRowY() {
        return LIST_FIRST_ROW_Y + (getVisibleRows(Services.Favorites.getLootFilters()) + 1) * LIST_ROW_HEIGHT + 3;
    }

    private static Component getListButtonMessage(String name, boolean selected) {
        return Component.literal(name).withStyle(selected ? ChatFormatting.YELLOW : ChatFormatting.WHITE);
    }

    private Button addListButton(ListView list, int index, String name, boolean selected, Runnable onPress) {
        return this.addRenderableWidget(new Button.Builder(getListButtonMessage(name, selected), (button) -> {
                    onPress.run();
                    this.rebuildWidgets();
                })
                .pos((int) (dividedWidth * list.gridX), getListRowY(index - list.scroll))
                .size((int) (dividedWidth * LIST_WIDTH), BUTTON_SIZE)
                .build());
    }

    private void addActionButton(
            int gridX, int gridY, int index, String key, ChatFormatting color, boolean active, Runnable onPress) {
        float buttonWidth = dividedWidth * LIST_WIDTH / 3;
        Button button = new Button.Builder(
                        Component.translatable("screens.wynntils.lootFilters." + key)
                                .withStyle(color),
                        (b) -> {
                            onPress.run();
                            this.rebuildWidgets();
                        })
                .pos((int) (dividedWidth * gridX + index * buttonWidth), (int) (dividedHeight * gridY))
                .size((int) buttonWidth, BUTTON_SIZE)
                .build();
        button.active = active;
        this.addRenderableWidget(button);
    }

    private TextInputBoxWidget addInput(int gridX, int gridY, int gridWidth, String value, Predicate<String> onUpdate) {
        TextInputBoxWidget[] input = new TextInputBoxWidget[1];
        input[0] = new TextInputBoxWidget(
                (int) (dividedWidth * gridX),
                (int) (dividedHeight * gridY),
                (int) (dividedWidth * gridWidth),
                BUTTON_SIZE,
                text -> input[0].setRenderColor(onUpdate.test(text) ? CommonColors.WHITE : CommonColors.RED),
                this);
        input[0].setTextBoxInput(value);
        this.addRenderableWidget(input[0]);
        return input[0];
    }

    private TextInputBoxWidget addIntInput(int gridX, int value, IntPredicate validator, Consumer<Integer> setter) {
        return addInput(gridX, ICON_ROW_Y, ICON_INPUT_WIDTH, Integer.toString(value), text -> {
            if (!text.matches("-?\\d{1,2}") || !validator.test(Integer.parseInt(text))) return false;

            setter.accept(Integer.parseInt(text));
            return true;
        });
    }

    private void addColorInput(int gridX, int gridY, CustomColor color, Consumer<CustomColor> setter) {
        TextInputBoxWidget input = addInput(gridX, gridY, EDITOR_COLUMN_WIDTH, color.toHexString(), text -> {
            CustomColor newColor = CustomColor.fromHexString(text);
            if (newColor == CustomColor.NONE) return false;

            setter.accept(newColor);
            return true;
        });

        this.addRenderableWidget(new ColorPickerWidget(
                (int) (dividedWidth * (gridX + EDITOR_COLUMN_WIDTH)) + 2,
                (int) (dividedHeight * gridY),
                BUTTON_SIZE,
                BUTTON_SIZE,
                input));
    }

    private void renderLabel(GuiGraphics guiGraphics, String key, int gridX, int gridY) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(I18n.get("screens.wynntils.lootFilters." + key)),
                        (int) (dividedWidth * gridX),
                        (int) (dividedHeight * gridY),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
    }

    private static final class ListView {
        private final int gridX;

        private int scroll = 0;

        private ListView(int gridX) {
            this.gridX = gridX;
        }
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
