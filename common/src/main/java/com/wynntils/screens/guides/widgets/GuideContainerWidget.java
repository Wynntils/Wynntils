/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.SortInfo;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.OptionalBoolean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.lwjgl.glfw.GLFW;

public abstract class GuideContainerWidget<T> extends AbstractWidget implements TextboxScreen {
    private static final float SCROLL_FACTOR = 10f;
    private static final int FILTER_PANEL_WIDTH = 150;

    private List<GuideButton> guideButtons;
    private List<GuideActionButton> favoriteActionButtons = new ArrayList<>();
    private boolean displayingFilters;
    private boolean displayingSorts;
    private GuideActionButton toggleFiltersButton;
    private GuideActionButton favoriteFilterButton;
    private GuideActionButton clearFavoritesButton;
    private OptionalBoolean favoriteFilterState = OptionalBoolean.NULL;
    private GuideFilterPanel filterPanel;
    private GuideSearchWidget searchWidget;
    private TextInputBoxWidget focusedTextInput;
    private String itemNameSearch = "";
    private List<GuideFilterWidget> filterWidgets = List.of();
    private List<GuideSortWidget> sortWidgets = List.of();

    private ItemSearchQuery searchQuery;
    private ItemSearchQuery cachedSearchQuery;
    private List<T> cachedGuideItems = List.of();

    private boolean draggingScroll = false;
    private int scrollOffset;
    private float scrollY;

    private int clearCount = 3;

    protected GuideContainerWidget(int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, Component.empty());

        this.scrollOffset = scrollOffset;
        this.displayingFilters = displayingFilters;

        searchQuery = Services.ItemFilter.createSearchQuery("", true, ItemProviderType.normalTypes());

        filterWidgets = createFilterWidgets(searchQuery);
        sortWidgets = filterWidgets.stream()
                .flatMap(filterWidget -> filterWidget.getProviders().stream())
                .distinct()
                .map(provider -> new GuideSortWidget(this, provider, searchQuery))
                .toList();

        this.displayingFilters = !filterWidgets.isEmpty();

        if (hasFavoriteFilter()) {
            MutableComponent tooltip = Component.empty()
                    .append(WynnFont.asFont("key_shift", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(WynnFont.asFont("key_plus", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.favoriteToggle")
                            .withStyle(ChatFormatting.GREEN));

            favoriteFilterButton = new GuideActionButton(
                    getX() + getWidth() - 80 - (filterWidgets.isEmpty() ? 0 : 44),
                    getY() + 2,
                    80,
                    20,
                    getFavoriteFilterMessage(),
                    (button) -> {
                        favoriteFilterState = switch (favoriteFilterState) {
                            case NULL -> OptionalBoolean.TRUE;
                            case TRUE -> OptionalBoolean.FALSE;
                            case FALSE -> OptionalBoolean.NULL;
                        };
                        favoriteFilterButton.setMessage(getFavoriteFilterMessage());
                        updateSearchFromQuickFilters();
                    },
                    tooltip);
        }

        if (!filterWidgets.isEmpty()) {
            MutableComponent tooltip = Component.empty()
                    .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.filterSortToggle")
                            .withStyle(ChatFormatting.GREEN));

            toggleFiltersButton = new GuideActionButton(
                    getX() + getWidth() - 40,
                    getY() + 2,
                    40,
                    20,
                    Component.literal("Sort"),
                    (button) -> {
                        this.displayingSorts = !this.displayingSorts;
                        filterPanel.setFilterWidgets(this.displayingSorts ? sortWidgets : filterWidgets);
                        toggleFiltersButton.setMessage(Component.literal(this.displayingSorts ? "Filter" : "Sort"));
                        updateSearchFromQuickFilters();
                    },
                    tooltip);

            filterPanel =
                    new GuideFilterPanel(getX() + getWidth() - 146, getY() + 28, 146, getHeight() - 28, filterWidgets);
            filterPanel.visible = true;
        }

        int searchRight =
                getX() + getWidth() - (filterWidgets.isEmpty() ? 0 : 44) - (favoriteFilterButton == null ? 0 : 84);
        searchWidget = new GuideSearchWidget(
                searchRight - 100,
                getY() + 2,
                100,
                20,
                text -> {
                    itemNameSearch = text;
                    updateSearchFromQuickFilters();
                },
                this);
        setFocusedTextInput(searchWidget);

        favoriteActionButtons = new ArrayList<>();

        MutableComponent importTooltip = Component.empty()
                .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                .append(" ")
                .append(Component.translatable("screens.wynntils.wynntilsGuides.import.tooltip")
                        .withStyle(ChatFormatting.GREEN));
        favoriteActionButtons.add(new GuideActionButton(
                getX() + 4,
                getY() + 2,
                40,
                20,
                Component.literal("Import").withStyle(ChatFormatting.GREEN),
                (button) -> importFavorites(),
                importTooltip));

        MutableComponent exportTooltip = Component.empty()
                .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                .append(" ")
                .append(Component.translatable("screens.wynntils.wynntilsGuides.export.tooltip")
                        .withStyle(ChatFormatting.GREEN));
        favoriteActionButtons.add(new GuideActionButton(
                getX() + 46,
                getY() + 2,
                40,
                20,
                Component.literal("Export").withStyle(ChatFormatting.BLUE),
                (button) -> exportFavorites(),
                exportTooltip));

        clearFavoritesButton = new GuideActionButton(
                getX() + 90,
                getY() + 2,
                40,
                20,
                Component.literal("Clear").withStyle(ChatFormatting.RED),
                (button) -> tryClearFavorites(),
                getClearFavoritesTooltip());
        favoriteActionButtons.add(clearFavoritesButton);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.GUIDE_BACKGROUND, getX(), getY() + 28, getBackgroundWidth(), getHeight() - 28);

        RenderUtils.drawTexturedRect(
                guiGraphics, Texture.GUIDE_TITLE, this.width / 2f - Texture.GUIDE_TITLE.width() / 2f, 8);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal(EnumUtils.toNiceString(getGuideType()) + " Guide")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                        this.width / 2f - 56,
                        this.width / 2f + 56,
                        9,
                        8 + Texture.GUIDE_TITLE.height() - 16,
                        112,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal(String.valueOf(getItemCount()))
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                        this.width / 2f - 56,
                        this.width / 2f + 56,
                        8 + 19,
                        8 + Texture.GUIDE_TITLE.height() - 2,
                        112,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);

        RenderUtils.enableScissor(guiGraphics, getX(), getY() + 42, getWidth(), getHeight());
        guideButtons.forEach(guideButton -> guideButton.render(guiGraphics, mouseX, mouseY, partialTick));
        RenderUtils.disableScissor(guiGraphics);

        renderScroll(guiGraphics, mouseX, mouseY);

        if (searchWidget != null) {
            searchWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (favoriteFilterButton != null) {
            favoriteFilterButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (toggleFiltersButton != null) {
            toggleFiltersButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (filterPanel != null) {
            filterPanel.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        favoriteActionButtons.forEach(button -> button.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    public void updateSearchFromQuickFilters() {
        String filters = filterWidgets.stream()
                .map(GuideFilterWidget::getItemSearchQuery)
                .collect(Collectors.joining(" "))
                .trim();

        List<SortInfo> sorts = sortWidgets.stream()
                .map(GuideSortWidget::getSortInfo)
                .flatMap(java.util.Optional::stream)
                .toList();

        if (favoriteFilterState != OptionalBoolean.NULL) {
            filters = (filters + " favorite:" + (favoriteFilterState == OptionalBoolean.TRUE)).trim();
        }

        String queryString = Services.ItemFilter.getItemFilterString(
                        java.util.Map.of(), sorts, List.of(itemNameSearch, filters))
                .replaceAll("\\s+", " ");

        onSearchQueryUpdated(Services.ItemFilter.createSearchQuery(queryString, true, ItemProviderType.normalTypes()));
    }

    private void onSearchQueryUpdated(ItemSearchQuery searchQuery) {
        this.searchQuery = searchQuery;

        if (filterPanel != null) {
            filterWidgets.forEach(filterWidget -> filterWidget.updateFromQuery(searchQuery));
            sortWidgets.forEach(sortWidget -> sortWidget.updateFromQuery(searchQuery));
            filterPanel.updateLayout();
        }

        cachedSearchQuery = null;
        rebuildWidgets();
    }

    private void renderScroll(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (getMaxScrollOffset() <= 0) return;

        scrollY = getScrollAreaStartY()
                + MathUtils.map(
                        scrollOffset,
                        0,
                        getMaxScrollOffset(),
                        0,
                        getScrollAreaHeight() - Texture.SCROLL_BUTTON.height());

        RenderUtils.drawTexturedRect(guiGraphics, Texture.SCROLL_BUTTON, getScrollBarX(), scrollY);

        if (draggingScroll) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        } else if (isOverScrollBar(mouseX, mouseY)) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!draggingScroll && getMaxScrollOffset() > 0) {
            if (isOverScrollBar((int) event.x(), (int) event.y())) {
                draggingScroll = true;

                return true;
            }
        }

        if (searchWidget != null && searchWidget.isMouseOver(event.x(), event.y())) {
            return searchWidget.mouseClicked(event, isDoubleClick);
        }

        if (filterPanel != null && filterPanel.visible && filterPanel.isMouseOver(event.x(), event.y())) {
            return filterPanel.mouseClicked(event, isDoubleClick);
        }

        for (GuideButton guideButton : guideButtons) {
            if (guideButton.isMouseOver(event.x(), event.y())) {
                boolean clicked = guideButton.mouseClicked(event, isDoubleClick);
                // Refresh search in case of a favorite toggle
                updateSearchFromQuickFilters();
                return clicked;
            }
        }

        for (GuideActionButton guideActionButton : favoriteActionButtons) {
            if (guideActionButton.isMouseOver(event.x(), event.y())) {
                return guideActionButton.mouseClicked(event, isDoubleClick);
            }
        }

        if (favoriteFilterButton != null && favoriteFilterButton.isMouseOver(event.x(), event.y())) {
            return favoriteFilterButton.mouseClicked(event, isDoubleClick);
        }

        if (toggleFiltersButton != null && toggleFiltersButton.isMouseOver(event.x(), event.y())) {
            return toggleFiltersButton.mouseClicked(event, isDoubleClick);
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (filterPanel != null && filterPanel.mouseDragged(event, dragX, dragY)) {
            return true;
        }

        if (searchWidget != null && searchWidget.isMouseOver(event.x(), event.y())) {
            return searchWidget.mouseDragged(event, dragX, dragY);
        }

        if (!draggingScroll) return false;

        int scrollAreaStartY = getScrollAreaStartY();
        int scrollAreaHeight = getScrollAreaHeight() - Texture.SCROLL_BUTTON.height();

        float thumbTop = (float) event.y() - Texture.SCROLL_BUTTON.height() / 2f;

        int newOffset = Math.round(MathUtils.map(
                thumbTop, scrollAreaStartY, scrollAreaStartY + scrollAreaHeight, 0, getMaxScrollOffset()));

        newOffset = Math.max(0, Math.min(newOffset, getMaxScrollOffset()));

        scroll(newOffset);

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScroll = false;

        if (searchWidget != null) {
            searchWidget.mouseReleased(event);
        }

        if (filterPanel != null) {
            filterPanel.mouseReleased(event);
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (filterPanel != null && filterPanel.isMouseOver(mouseX, mouseY)) {
            return filterPanel.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        }

        if (searchWidget != null && searchWidget.isMouseOver(mouseX, mouseY)) {
            return searchWidget.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        }

        int scrollAmount = (int) (-deltaY * SCROLL_FACTOR);
        int newOffset = Math.max(0, Math.min(scrollOffset + scrollAmount, getMaxScrollOffset()));
        scroll(newOffset);

        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            return false;
        }

        if (focusedTextInput != null) {
            return focusedTextInput.keyPressed(event);
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        return focusedTextInput != null && focusedTextInput.charTyped(event);
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public boolean isDisplayingFilters() {
        return displayingFilters;
    }

    @Override
    public TextInputBoxWidget getFocusedTextInput() {
        return focusedTextInput;
    }

    @Override
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {
        this.focusedTextInput = focusedTextInput;
    }

    protected void scroll(int newOffset) {
        scrollOffset = newOffset;
        int currentY = getY() + 42;

        int buttonsPerRow = Math.max(1, getGuideItemSpace() / getWidgetWidth());

        for (int i = 0; i < guideButtons.size(); i++) {
            AbstractWidget widget = guideButtons.get(i);

            int newY = currentY - scrollOffset;
            widget.setY(newY);
            widget.visible = newY + 20 >= getY() + 40 && newY < getY() + getHeight();

            if ((i + 1) % buttonsPerRow == 0) {
                currentY += 22;
            }
        }
    }

    private boolean isOverScrollBar(int mouseX, int mouseY) {
        return MathUtils.isInside(
                mouseX, mouseY, getScrollBarX(), getScrollBarX() + Texture.SCROLL_BUTTON.width(), (int) scrollY, (int)
                        (scrollY + Texture.SCROLL_BUTTON.height()));
    }

    private int getScrollBarX() {
        return getX() + getBackgroundWidth() - 9;
    }

    private int getScrollAreaStartY() {
        return getY() + 40;
    }

    private int getScrollAreaHeight() {
        return getHeight() - 42;
    }

    protected int getMaxScrollOffset() {
        int buttonsPerRow = getGuideItemSpace() / getWidgetWidth();
        int rows = (int) Math.ceil((double) getItemCount() / buttonsPerRow);
        int contentHeight = rows * 22;
        int visibleHeight = getHeight() - 42;

        return Math.max(0, contentHeight - visibleHeight);
    }

    private int getGuideItemSpace() {
        return getBackgroundWidth() - Texture.GUIDE_BACKGROUND.left() - Texture.GUIDE_BACKGROUND.right();
    }

    private int getBackgroundWidth() {
        return filterPanel != null ? getWidth() - FILTER_PANEL_WIDTH : getWidth();
    }

    protected int getWidgetWidth() {
        return 22;
    }

    protected void rebuildWidgets() {
        guideButtons = new ArrayList<>();

        int widgetWidth = getWidgetWidth();
        int buttonsPerRow = Math.max(1, getGuideItemSpace() / widgetWidth);

        int usedWidth = buttonsPerRow * widgetWidth;
        int startX = getX() + Texture.GUIDE_BACKGROUND.left() + (getGuideItemSpace() - usedWidth) / 2;

        int renderX = startX;
        int renderY = getY() + 42;

        for (T item : getGuideItems()) {
            GuideButton guideButton = createGuideButton(renderX, renderY, item);
            guideButton.visible = renderY > getY() && renderY < getY() + getHeight();

            guideButtons.add(guideButton);

            renderX += widgetWidth;

            if (guideButtons.size() % buttonsPerRow == 0) {
                renderX = startX;
                renderY += 22;
            }
        }

        scroll(Math.max(0, Math.min(scrollOffset, getMaxScrollOffset())));
    }

    protected List<T> getGuideItems() {
        if (searchQuery != cachedSearchQuery) {
            cachedSearchQuery = searchQuery;
            cachedGuideItems = filterAndSortGuideItems(searchQuery, getAllGuideItems());
        }

        return cachedGuideItems;
    }

    private int getItemCount() {
        return getGuideItems().size();
    }

    protected boolean hasFavoriteFilter() {
        return true;
    }

    private Component getFavoriteFilterMessage() {
        MutableComponent message = Component.literal("Favorite ");
        switch (favoriteFilterState) {
            case TRUE ->
                message.append(Component.literal("★")
                        .withStyle(Style.EMPTY.withFont(FontDescription.DEFAULT).withColor(ChatFormatting.YELLOW)));
            case FALSE ->
                message.append(Component.literal("★")
                        .withStyle(Style.EMPTY.withFont(FontDescription.DEFAULT).withColor(ChatFormatting.RED)));
            case NULL ->
                message.append(Component.literal("☆")
                        .withStyle(Style.EMPTY.withFont(FontDescription.DEFAULT).withColor(ChatFormatting.WHITE)));
        }

        return message;
    }

    private void importFavorites() {
        String clipboard = McUtils.mc().keyboardHandler.getClipboard();

        if (clipboard == null || !clipboard.startsWith("wynntilsFavorites,")) {
            McUtils.sendErrorToClient(I18n.get("screens.wynntils.wynntilsGuides.invalidClipboard"));
            return;
        }

        ArrayList<String> names = new ArrayList<>(Arrays.asList(clipboard.split(",")));
        names.removeFirst(); // Remove the "wynntilsFavorites," part
        names.forEach(name -> {
            if (name.isBlank()) return;
            Services.Favorites.addFavorite(name);
        });

        McUtils.sendWynntilsPrefixMessage(
                Component.translatable("screens.wynntils.wynntilsGuides.importedFavorites", names.size())
                        .withStyle(ChatFormatting.GREEN));

        clearCount = 3;
        clearFavoritesButton.setTooltip(Tooltip.create(getClearFavoritesTooltip()));
        updateSearchFromQuickFilters();
    }

    private void exportFavorites() {
        McUtils.mc()
                .keyboardHandler
                .setClipboard("wynntilsFavorites," + String.join(",", Services.Favorites.getFavoriteItems()));
        McUtils.sendWynntilsPrefixMessage(Component.translatable(
                        "screens.wynntils.wynntilsGuides.exportedFavorites",
                        Services.Favorites.getFavoriteItems().size())
                .withStyle(ChatFormatting.GREEN));
    }

    private void tryClearFavorites() {
        if (clearCount > 1) {
            clearCount--;
            clearFavoritesButton.setTooltip(Tooltip.create(getClearFavoritesTooltip()));
            return;
        }

        Services.Favorites.clearFavorites();
        updateSearchFromQuickFilters();
        clearCount = 3;
        clearFavoritesButton.setTooltip(Tooltip.create(getClearFavoritesTooltip()));
    }

    private Component getClearFavoritesTooltip() {
        return Component.empty()
                .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                .append(" ")
                .append(Component.translatable("screens.wynntils.wynntilsGuides.clear.tooltip", clearCount)
                        .withStyle(ChatFormatting.RED));
    }

    protected abstract GuideButton createGuideButton(int x, int y, T item);

    protected abstract WynntilsGuideScreen.GuideType getGuideType();

    protected abstract List<T> filterAndSortGuideItems(ItemSearchQuery searchQuery, List<T> guideItems);

    protected abstract List<T> getAllGuideItems();

    protected abstract List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery);

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
