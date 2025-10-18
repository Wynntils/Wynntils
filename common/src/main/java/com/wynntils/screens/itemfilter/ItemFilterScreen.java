/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.ItemSearchWidget;
import com.wynntils.screens.base.widgets.SearchWidget;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.itemfilter.widgets.FilterOptionsButton;
import com.wynntils.screens.itemfilter.widgets.PresetButton;
import com.wynntils.screens.itemfilter.widgets.ProviderButton;
import com.wynntils.screens.itemfilter.widgets.ProviderFilterListWidget;
import com.wynntils.screens.itemfilter.widgets.SortWidget;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.SortInfo;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class ItemFilterScreen extends WynntilsScreen {
    // Constants
    private static final float SCROLL_FACTOR = 10f;
    private static final int MAX_PRESETS = 4;
    private static final int MAX_PROVIDERS_PER_PAGE = 8;
    private static final int MAX_SORTS_PER_PAGE = 7;
    private static final int PROVIDER_MASK_TOP_Y = 25;
    private static final int PROVIDER_MASK_BOTTOM_Y = 197;

    // Collections
    private final List<ItemProviderType> supportedProviderTypes;
    private List<ItemStatProvider<?>> itemStatProviders = new ArrayList<>();
    private List<SortInfo> sorts = new ArrayList<>();
    private List<Pair<String, String>> presets;
    private List<SortWidget> sortButtons = new ArrayList<>();
    private List<WynntilsButton> presetButtons = new ArrayList<>();
    private List<WynntilsButton> providerButtons = new ArrayList<>();
    private Map<ItemStatProvider<?>, List<StatProviderAndFilterPair>> filterMap = new HashMap<>();

    // Renderables
    private final SearchWidget itemSearchWidget;
    private final SearchWidget previousSearchWidget;
    private final SearchWidget providerSearchWidget;
    private final TextInputBoxWidget itemNameInput;
    private Button applyButton;
    private Button savePresetButton;
    private Button toggleSortButton;
    private FilterOptionsButton allButton;
    private FilterOptionsButton usedButton;
    private FilterOptionsButton unusedButton;
    private FilterOptionsButton selectedFilterButton;
    private ProviderFilterListWidget filterWidget;
    private TextInputBoxWidget focusedTextInput;
    private TextInputBoxWidget presetNameInput;
    private WynntilsButton nextPresetButton;
    private WynntilsButton previousPresetButton;

    // UI size, positions, etc
    private boolean draggingProviderScroll = false;
    private boolean draggingSortScroll = false;
    private float providerScrollY;
    private float sortScrollY;
    private int presetsScrollOffset = 0;
    private int providersScrollOffset = 0;
    private int sortScrollOffset = 0;
    private int offsetX;
    private int offsetY;
    private Renderable hovered = null;

    // Screen information
    private final boolean supportsSorting;
    private final Screen previousScreen;
    private boolean sortMode = false;
    private FilterType filterType = FilterType.ALL;
    private ItemStatProvider<?> selectedProvider;

    private ItemFilterScreen(
            SearchWidget searchWidget,
            Screen previousScreen,
            boolean supportsSorting,
            List<ItemProviderType> supportedProviderTypes) {
        super(Component.literal("Item Filter Screen"));

        this.previousSearchWidget = searchWidget;
        this.previousScreen = previousScreen;
        this.supportsSorting = supportsSorting;
        this.supportedProviderTypes = supportedProviderTypes;

        // region Input widgets
        itemNameInput = new TextInputBoxWidget(220, 5, 100, 18, (s -> updateQueryString()), this);

        this.providerSearchWidget = new SearchWidget(
                7,
                5,
                supportsSorting ? 100 : 120,
                20,
                (s) -> {
                    providersScrollOffset = 0;
                    updateProviderWidgets();
                },
                this);

        this.itemSearchWidget = new ItemSearchWidget(
                0,
                -22,
                Texture.ITEM_FILTER_BACKGROUND.width() - 40,
                20,
                supportedProviderTypes,
                supportsSorting,
                (query) -> {
                    if (applyButton == null) return;

                    applyButton.active = true;
                    // Changes are only made when the user presses the apply button
                },
                this);

        this.itemSearchWidget.setTextBoxInput(previousSearchWidget.getTextBoxInput());
        // endregion

        setFocusedTextInput(providerSearchWidget);

        presets = Services.ItemFilter.presets.get();
    }

    public static Screen create(
            SearchWidget searchWidget,
            Screen previousScreen,
            boolean supportsSorting,
            List<ItemProviderType> supportedProviderTypes) {
        return new ItemFilterScreen(searchWidget, previousScreen, supportsSorting, supportedProviderTypes);
    }

    @Override
    protected void doInit() {
        super.doInit();
        offsetX = (int) ((this.width - Texture.ITEM_FILTER_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.ITEM_FILTER_BACKGROUND.height()) / 2f);
        itemNameInput.setX(220 + offsetX);
        itemNameInput.setY(5 + offsetY);
        providerSearchWidget.setX(7 + offsetX);
        providerSearchWidget.setY(5 + offsetY);
        itemSearchWidget.setX(offsetX);
        itemSearchWidget.setY(-22 + offsetY);

        this.addRenderableWidget(itemSearchWidget);
        this.addRenderableWidget(providerSearchWidget);
        this.addRenderableWidget(itemNameInput);

        presetNameInput = new TextInputBoxWidget(
                220 + offsetX, 180 + offsetY, 100, 20, (s -> checkSaveStatus()), this, presetNameInput);

        this.addRenderableWidget(presetNameInput);

        // region State buttons
        if (this.supportsSorting) {
            toggleSortButton = new Button.Builder(
                            Component.literal(sortMode ? "🔍" : "⇅"), (button -> toggleSortMode()))
                    .pos(108 + offsetX, 5 + offsetY)
                    .size(20, 20)
                    .tooltip(Tooltip.create(Component.translatable(
                            sortMode
                                    ? "screens.wynntils.itemFilter.filterToggle"
                                    : "screens.wynntils.itemFilter.sortToggle")))
                    .build();

            this.addRenderableWidget(toggleSortButton);
        }

        Button returnButton = new Button.Builder(Component.literal("⏎"), (button -> onClose()))
                .pos(Texture.ITEM_FILTER_BACKGROUND.width() - 18 + offsetX, -22 + offsetY)
                .size(20, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.itemFilter.return")))
                .build();

        this.addRenderableWidget(returnButton);

        boolean activeApply = applyButton != null && applyButton.active;

        applyButton = new Button.Builder(Component.literal("✔").withStyle(ChatFormatting.GREEN), (button -> {
                    providersScrollOffset = 0;
                    sortScrollOffset = 0;
                    updateStateFromItemSearchWidget();
                    this.applyButton.active = false;
                }))
                .pos(Texture.ITEM_FILTER_BACKGROUND.width() - 39 + offsetX, -22 + offsetY)
                .size(20, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.itemFilter.apply")))
                .build();
        applyButton.active = activeApply;

        this.addRenderableWidget(applyButton);
        // endregion

        // region Preset buttons
        savePresetButton = new Button.Builder(Component.literal("💾"), (button -> savePreset()))
                .pos(330 + offsetX, 180 + offsetY)
                .size(20, 20)
                .build();

        savePresetButton.active = !presetNameInput.getTextBoxInput().isEmpty()
                && !itemSearchWidget.getTextBoxInput().isEmpty();

        this.addRenderableWidget(savePresetButton);

        previousPresetButton = this.addRenderableWidget(new PresetButton(
                Texture.ITEM_FILTER_BACKGROUND.width() - 4 + offsetX,
                4 + offsetY,
                StyledText.fromString("🠝"),
                (b) -> scrollPresets(-1),
                List.of(Component.translatable("screens.wynntils.itemFilter.scrollUp")),
                offsetX,
                offsetY));

        nextPresetButton = this.addRenderableWidget(new PresetButton(
                Texture.ITEM_FILTER_BACKGROUND.width() - 4 + offsetX,
                174 + offsetY,
                StyledText.fromString("🠟"),
                (b) -> scrollPresets(1),
                List.of(Component.translatable("screens.wynntils.itemFilter.scrollDown")),
                offsetX,
                offsetY));

        if (presets.size() <= MAX_PRESETS) {
            previousPresetButton.visible = false;
            nextPresetButton.visible = false;
        }
        // endregion

        // region Filter type buttons
        allButton = new FilterOptionsButton(
                -(Texture.BUTTON_LEFT.width()) + 4 + offsetX,
                8 + offsetY,
                Texture.BUTTON_LEFT.width(),
                Texture.BUTTON_LEFT.height() / 2,
                StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.allFilter")),
                (button) -> setSelectedFilter(FilterType.ALL),
                List.of(Component.translatable("screens.wynntils.itemFilter.allFilterTooltip")),
                Texture.BUTTON_LEFT,
                filterType == FilterType.ALL,
                offsetX,
                offsetY);

        usedButton = new FilterOptionsButton(
                -(Texture.BUTTON_LEFT.width()) + 4 + offsetX,
                12 + Texture.BUTTON_LEFT.height() / 2 + offsetY,
                Texture.BUTTON_LEFT.width(),
                Texture.BUTTON_LEFT.height() / 2,
                StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.usedFilter")),
                (button) -> setSelectedFilter(FilterType.USED),
                List.of(Component.translatable("screens.wynntils.itemFilter.usedFilterTooltip")),
                Texture.BUTTON_LEFT,
                filterType == FilterType.USED,
                offsetX,
                offsetY);

        unusedButton = new FilterOptionsButton(
                -(Texture.BUTTON_LEFT.width()) + 4 + offsetX,
                16 + (Texture.BUTTON_LEFT.height() / 2) * 2 + offsetY,
                Texture.BUTTON_LEFT.width(),
                Texture.BUTTON_LEFT.height() / 2,
                StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.unusedFilter")),
                (button) -> setSelectedFilter(FilterType.UNUSED),
                List.of(Component.translatable("screens.wynntils.itemFilter.unusedFilterTooltip")),
                Texture.BUTTON_LEFT,
                filterType == FilterType.UNUSED,
                offsetX,
                offsetY);

        switch (filterType) {
            case USED -> selectedFilterButton = usedButton;
            case UNUSED -> selectedFilterButton = unusedButton;
            default -> selectedFilterButton = allButton;
        }

        this.addRenderableWidget(allButton);
        this.addRenderableWidget(usedButton);
        this.addRenderableWidget(unusedButton);
        // endregion

        filterWidget = null;

        updateProviderWidgets();
        updatePresetWidgets();

        updateStateFromItemSearchWidget();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        hovered = null;

        RenderUtils.drawTexturedRect(poseStack, Texture.ITEM_FILTER_BACKGROUND, offsetX, offsetY);

        if (selectedProvider == null && !sortMode) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.itemFilter.unselectedFilter")),
                            147 + offsetX,
                            345 + offsetX,
                            63 + offsetY,
                            123 + offsetY,
                            200,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        } else if (sortMode && sorts.isEmpty()) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.noSorts")),
                            147 + offsetX,
                            345 + offsetX,
                            63 + offsetY,
                            123 + offsetY,
                            200,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }

        if (!sortMode) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.itemName")),
                            150 + offsetX,
                            10 + offsetY,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NORMAL);
        }

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.presetName")),
                        150 + offsetX,
                        185 + offsetY,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        for (Renderable renderable : renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);

            if (renderable instanceof WynntilsButton wynntilsButton) {
                if (wynntilsButton.isMouseOver(mouseX, mouseY)) {
                    hovered = renderable;
                }
            }
        }

        if (providerButtons.isEmpty()) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.noProviders")),
                            8 + offsetX,
                            127 + offsetX,
                            28 + offsetY,
                            169 + offsetY,
                            118,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        } else {
            RenderUtils.enableScissor(guiGraphics, 6 + offsetX, 28 + offsetY, 122, MAX_PROVIDERS_PER_PAGE * 21 + 2);

            for (Renderable renderable : providerButtons) {
                renderable.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            RenderUtils.disableScissor(guiGraphics);
        }

        RenderUtils.enableScissor(guiGraphics, 149 + offsetX, 28 + offsetY, 172, MAX_SORTS_PER_PAGE * 21 + 2);

        for (Renderable renderable : sortButtons) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        RenderUtils.disableScissor(guiGraphics);

        for (Renderable renderable : presetButtons) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);

            if (renderable instanceof WynntilsButton wynntilsButton) {
                if (wynntilsButton.isMouseOver(mouseX, mouseY)) {
                    hovered = renderable;
                }
            }
        }

        if (itemStatProviders.size() > MAX_PROVIDERS_PER_PAGE) {
            renderProvidersScroll(poseStack);
        }

        if (sortMode && sorts.size() > MAX_SORTS_PER_PAGE) {
            renderSortScroll(poseStack);
        }

        renderTooltips(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void added() {
        providerSearchWidget.opened();
        super.added();
    }

    @Override
    public void onClose() {
        // Set the query for the ItemSearchWidget on the previous screen and return to it
        previousSearchWidget.setTextBoxInput(itemSearchWidget.getTextBoxInput());
        McUtils.setScreen(previousScreen);
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        if (!draggingProviderScroll && itemStatProviders.size() > MAX_PROVIDERS_PER_PAGE) {
            if (MathUtils.isInside(
                    (int) mouseX,
                    (int) mouseY,
                    133 + offsetX,
                    133 + Texture.SCROLL_BUTTON.width() + offsetX,
                    (int) providerScrollY,
                    (int) (providerScrollY + Texture.SCROLL_BUTTON.height()))) {
                draggingProviderScroll = true;

                return true;
            }
        }

        if (sortMode && !draggingSortScroll && sorts.size() > MAX_SORTS_PER_PAGE) {
            if (MathUtils.isInside((int) mouseX, (int) mouseY, 330, 336, (int) sortScrollY, (int) (sortScrollY + 20))) {
                draggingSortScroll = true;

                return true;
            }
        }

        for (GuiEventListener listener : getWidgetsForIteration().toList()) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                // Preset and filter buttons have a slight bit rendered underneath the background but we don't want that
                // part to be clickable
                if (listener instanceof PresetButton || listener instanceof FilterOptionsButton) {
                    if (MathUtils.isInside(
                            (int) mouseX,
                            (int) mouseY,
                            offsetX,
                            Texture.ITEM_FILTER_BACKGROUND.width() + offsetX,
                            offsetY,
                            Texture.ITEM_FILTER_BACKGROUND.height() + offsetY)) {
                        return false;
                    }
                }

                listener.mouseClicked(mouseX, mouseY, button);
            }
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // Prevent scrolling issues in the filter widget by triggering mouse released when no longer hovering it
        if (filterWidget != null && !filterWidget.isMouseOver(mouseX, mouseY)) {
            filterWidget.mouseReleased(mouseX, mouseY, button);
        }

        if (draggingProviderScroll) {
            int scrollAreaStartY = 24 + 10 + offsetY;
            int scrollAreaHeight = MAX_PROVIDERS_PER_PAGE * 21 - Texture.SCROLL_BUTTON.height();

            int newOffset = Math.round(MathUtils.map(
                    (float) mouseY,
                    scrollAreaStartY,
                    scrollAreaStartY + scrollAreaHeight,
                    0,
                    getMaxProviderScrollOffset()));

            newOffset = Math.max(0, Math.min(newOffset, getMaxProviderScrollOffset()));

            scrollProviders(newOffset);

            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        } else if (draggingSortScroll) {
            int scrollAreaStartY = 30 + 10;
            int scrollAreaHeight = MAX_SORTS_PER_PAGE * 21 - 20;

            int newOffset = Math.round(MathUtils.map(
                    (float) mouseY,
                    scrollAreaStartY,
                    scrollAreaStartY + scrollAreaHeight,
                    0,
                    getMaxSortsScrollOffset()));

            newOffset = Math.max(0, Math.min(newOffset, getMaxSortsScrollOffset()));

            scrollSorts(newOffset);

            return true;
        }

        for (GuiEventListener listener : this.children) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                return listener.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingProviderScroll = false;
        draggingSortScroll = false;

        for (GuiEventListener listener : this.children) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                return listener.mouseReleased(mouseX, mouseY, button);
            }
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        double scrollValue = -Math.signum(deltaY);

        for (GuiEventListener listener : this.children) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                if (listener.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
                    return true;
                }
            }
        }

        if (presets.size() > MAX_PRESETS && mouseX >= Texture.ITEM_FILTER_BACKGROUND.width() + offsetX) {
            scrollPresets((int) scrollValue);
            return true;
        }

        int scrollAmount = (int) (-deltaY * SCROLL_FACTOR);

        if (sortMode && mouseX >= 150 + offsetX) {
            int newOffset = Math.max(0, Math.min(sortScrollOffset + scrollAmount, getMaxSortsScrollOffset()));
            scrollSorts(newOffset);
        } else {
            int newOffset = Math.max(0, Math.min(providersScrollOffset + scrollAmount, getMaxProviderScrollOffset()));
            scrollProviders(newOffset);
        }

        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return focusedTextInput != null && focusedTextInput.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            onClose();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER && applyButton.active) {
            updateStateFromItemSearchWidget();
            applyButton.active = false;
        }

        return focusedTextInput != null && focusedTextInput.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public TextInputBoxWidget getFocusedTextInput() {
        return focusedTextInput;
    }

    @Override
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {
        this.focusedTextInput = focusedTextInput;
    }

    public void setFiltersForProvider(ItemStatProvider<?> provider, List<StatProviderAndFilterPair> filterPairs) {
        // Remove all old filters for the provider
        filterMap.remove(provider);

        // Add the new filters
        if (filterPairs != null && !filterPairs.isEmpty()) {
            filterMap.put(provider, filterPairs);
        }

        updateQueryString();
    }

    public void updateFilterWidget() {
        if (filterWidget != null && selectedProvider != null) {
            filterWidget.onFiltersChanged(filterMap.getOrDefault(selectedProvider, List.of()));
        }
    }

    public void addSort(SortInfo newSort) {
        sorts = sorts.stream()
                .filter(sort -> sort.provider() != newSort.provider())
                .collect(Collectors.toList());

        sorts.add(newSort);

        updateQueryString();
        updateSortWidgets();
    }

    public void removeSort(ItemStatProvider<?> provider) {
        // Remove all instances of the provider in the sort list
        sorts.removeIf(sort -> sort.provider() == provider);

        updateQueryString();
        updateSortWidgets();
    }

    public void changeSort(SortInfo oldSortInfo, SortInfo newSortInfo) {
        sorts = sorts.stream()
                .map(sort -> {
                    if (sort.equals(oldSortInfo)) return newSortInfo;
                    return sort;
                })
                .collect(Collectors.toList());

        updateQueryString();
        updateSortWidgets();
    }

    public void reorderSort(SortInfo sortInfo, int direction) {
        // Find the index of the provider
        int indexOf = sorts.indexOf(sortInfo);

        // Should never happen but just in case
        if (indexOf == -1) return;

        SortInfo sort = sorts.get(indexOf);

        // Remove the sort and add it in the new index
        sorts.remove(sort);
        sorts.add(indexOf + direction, sort);

        updateQueryString();
        updateSortWidgets();
    }

    public boolean inSortMode() {
        return sortMode;
    }

    public Pair<Boolean, Boolean> canSortMove(SortInfo sortInfo) {
        // If this sort is not the first or last in the list, it can be moved
        int index = sorts.indexOf(sortInfo);
        return Pair.of(index != 0, index != sorts.size() - 1);
    }

    public void setSelectedProvider(ItemStatProvider<?> selectedProvider) {
        this.selectedProvider = selectedProvider;

        createValueWidget();
    }

    public ItemStatProvider<?> getSelectedProvider() {
        return selectedProvider;
    }

    public boolean isProviderInUse(ItemStatProvider<?> provider) {
        return filterMap.containsKey(provider) || sorts.stream().anyMatch(sort -> sort.provider() == provider);
    }

    public int getProviderMaskTopY() {
        return PROVIDER_MASK_TOP_Y + offsetY;
    }

    public int getProviderMaskBottomY() {
        return PROVIDER_MASK_BOTTOM_Y + offsetY;
    }

    private void updateProviderWidgets() {
        for (AbstractWidget widget : providerButtons) {
            this.removeWidget(widget);
        }

        providerButtons = new ArrayList<>();

        itemStatProviders = Services.ItemFilter.getItemStatProviders().stream()
                .filter(itemStatProvider ->
                        itemStatProvider.getFilterTypes().stream().anyMatch(supportedProviderTypes::contains))
                .filter(provider -> searchMatches(provider.getDisplayName()))
                .toList();

        // Filter the providers if not using the ALL type
        if (filterType == FilterType.USED) {
            itemStatProviders =
                    itemStatProviders.stream().filter(this::isProviderInUse).toList();
        } else if (filterType == FilterType.UNUSED) {
            itemStatProviders = itemStatProviders.stream()
                    .filter(provider -> !isProviderInUse(provider))
                    .toList();
        }

        int yPos = 31 + offsetY;

        for (ItemStatProvider<?> provider : itemStatProviders) {
            providerButtons.add(new ProviderButton(7 + offsetX, yPos, 120, 18, this, provider));

            yPos += 21;
        }

        if (selectedProvider != null) {
            Stream<ItemStatProvider<?>> providerList = Services.ItemFilter.getItemStatProviders().stream();

            ItemStatProvider<?> newSelected = providerList
                    .filter(provider -> provider.getName().equals(selectedProvider.getName()))
                    .findFirst()
                    .orElse(null);

            setSelectedProvider(newSelected);
        }

        scrollProviders(providersScrollOffset);
    }

    private void updateStateFromItemSearchWidget() {
        parseFilters();
        updateProviderWidgets();

        if (sortMode) {
            updateSortWidgets();
        }

        updateFilterWidget();
    }

    private void updatePresetWidgets() {
        for (AbstractWidget widget : presetButtons) {
            this.removeWidget(widget);
        }

        presetButtons = new ArrayList<>();

        if (presets.isEmpty()) return;

        int yPos = 4 + offsetY;

        for (int i = 0; i < Math.min(MAX_PRESETS, presets.size()); i++) {
            yPos += Texture.BUTTON_RIGHT.height() / 2 + 6;
            int presetIndex;

            if (i + presetsScrollOffset < 0) {
                presetIndex = (i + presetsScrollOffset) + presets.size();
            } else if (i + presetsScrollOffset > presets.size() - 1) {
                presetIndex = (i + presetsScrollOffset) - presets.size();
            } else {
                presetIndex = (i + presetsScrollOffset);
            }

            if (presetIndex > presets.size() - 1) break;

            String presetName = presets.get(presetIndex).a();

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal(presetName));
            tooltip.add(Component.translatable("screens.wynntils.itemFilter.presetHelp1"));
            tooltip.add(Component.translatable("screens.wynntils.itemFilter.presetHelp2"));
            tooltip.add(Component.translatable("screens.wynntils.itemFilter.presetHelp3"));
            tooltip.add(Component.translatable("screens.wynntils.itemFilter.presetHelp4"));

            tooltip = ComponentUtils.wrapTooltips(tooltip, 200);

            presetButtons.add(new PresetButton(
                    Texture.ITEM_FILTER_BACKGROUND.width() - 4 + offsetX,
                    yPos,
                    StyledText.fromString(presetName),
                    (b) -> clickPreset(b, presetIndex),
                    tooltip,
                    offsetX,
                    offsetY));
        }
    }

    private void updateSortWidgets() {
        for (AbstractWidget widget : sortButtons) {
            this.removeWidget(widget);
        }

        sortButtons = new ArrayList<>();

        int yPos = 29 + offsetY;

        for (SortInfo sort : sorts) {
            sortButtons.add(new SortWidget(150 + offsetX, yPos, this, sort));

            yPos += 21;
        }

        scrollSorts(sortScrollOffset);
    }

    private void createValueWidget() {
        if (filterWidget != null) {
            this.removeWidget(filterWidget);
        }

        filterWidget = new ProviderFilterListWidget(
                150 + offsetX,
                30 + offsetY,
                this,
                selectedProvider,
                filterMap.getOrDefault(selectedProvider, List.of()));
        this.addRenderableWidget(filterWidget);
    }

    private void scrollProviders(int newOffset) {
        providersScrollOffset = newOffset;

        for (WynntilsButton provider : providerButtons) {
            int newY = 31 + offsetY + (providerButtons.indexOf(provider) * 21) - providersScrollOffset;

            provider.setY(newY);
            provider.visible =
                    newY >= (31 + offsetY - 21) && newY <= (31 + offsetY + (MAX_PROVIDERS_PER_PAGE + 1) * 21);
        }
    }

    private void scrollSorts(int newOffset) {
        sortScrollOffset = newOffset;

        for (SortWidget sort : sortButtons) {
            int newY = 31 + offsetY + (sortButtons.indexOf(sort) * 21) - sortScrollOffset;

            sort.setY(newY);
            sort.visible = newY >= (31 + offsetY - 21) && newY <= (31 + offsetY + (MAX_SORTS_PER_PAGE + 1) * 21);
        }
    }

    private void scrollPresets(int direction) {
        if (Math.abs(presetsScrollOffset + direction) == presets.size()) {
            presetsScrollOffset = 0;
        } else {
            presetsScrollOffset =
                    MathUtils.clamp(presetsScrollOffset + direction, -(presets.size() - 1), (presets.size() - 1));
        }

        updatePresetWidgets();
    }

    private int getMaxProviderScrollOffset() {
        return (itemStatProviders.size() - MAX_PROVIDERS_PER_PAGE) * 21;
    }

    private int getMaxSortsScrollOffset() {
        return (sorts.size() - MAX_SORTS_PER_PAGE) * 21;
    }

    private void clickPreset(int button, int presetIndex) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (KeyboardUtils.isShiftDown()) { // Shift the preset up
                int indexToSwap = presetIndex == 0 ? presets.size() - 1 : presetIndex - 1;

                Collections.swap(presets, presetIndex, indexToSwap);

                Services.ItemFilter.presets.store(presets);
                Services.ItemFilter.presets.touched();

                presets = Services.ItemFilter.presets.get();

                updatePresetWidgets();
            } else { // Select the preset
                itemSearchWidget.setTextBoxInput(presets.get(presetIndex).b());
                updateStateFromItemSearchWidget();
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (KeyboardUtils.isShiftDown()) { // Shift the preset down
                int indexToSwap = presetIndex == presets.size() - 1 ? 0 : presetIndex + 1;

                Collections.swap(presets, presetIndex, indexToSwap);

                Services.ItemFilter.presets.store(presets);
                Services.ItemFilter.presets.touched();

                presets = Services.ItemFilter.presets.get();

                updatePresetWidgets();
            } else if (KeyboardUtils.isControlDown()) { // Delete the preset
                presets.remove(presetIndex);

                Services.ItemFilter.presets.store(presets);
                Services.ItemFilter.presets.touched();

                presets = Services.ItemFilter.presets.get();

                presetsScrollOffset = Math.max(presetsScrollOffset - 1, 0);

                updatePresetWidgets();

                if (presets.size() <= MAX_PRESETS) {
                    nextPresetButton.visible = false;
                    previousPresetButton.visible = false;
                }
            }
        }
    }

    private void savePreset() {
        presets.add(new Pair<>(presetNameInput.getTextBoxInput(), itemSearchWidget.getTextBoxInput()));

        Services.ItemFilter.presets.store(presets);
        Services.ItemFilter.presets.touched();

        presetNameInput.setTextBoxInput("");

        if (presets.size() > MAX_PRESETS) {
            nextPresetButton.visible = true;
            previousPresetButton.visible = true;
        }

        updatePresetWidgets();
    }

    private void checkSaveStatus() {
        if (presetNameInput == null || itemSearchWidget == null) return;

        savePresetButton.active = !presetNameInput.getTextBoxInput().isEmpty()
                && !itemSearchWidget.getTextBoxInput().isEmpty();
    }

    private void toggleSortMode() {
        sortMode = !sortMode;

        itemNameInput.visible = !sortMode;

        if (sortMode) {
            if (filterWidget != null) {
                selectedProvider = null;
                this.removeWidget(filterWidget);
            }

            sortScrollOffset = 0;

            updateSortWidgets();
        } else {
            for (AbstractWidget widget : sortButtons) {
                this.removeWidget(widget);
            }

            sortButtons = new ArrayList<>();
        }

        toggleSortButton.setMessage(Component.literal(sortMode ? "🔍" : "⇅"));
        toggleSortButton.setTooltip(Tooltip.create(Component.translatable(
                sortMode ? "screens.wynntils.itemFilter.filterToggle" : "screens.wynntils.itemFilter.sortToggle")));
    }

    private void parseFilters() {
        ItemSearchQuery searchQuery =
                Services.ItemFilter.createSearchQuery(itemSearchWidget.getTextBoxInput(), true, supportedProviderTypes);

        filterMap = new HashMap<>(searchQuery.filters().entries());
        sorts = searchQuery.sorts();

        String plainTextString = String.join(" ", searchQuery.plainTextTokens());

        // Don't want to update the item name if it's the same (avoid recursion loop)
        if (plainTextString.equals(itemNameInput.getTextBoxInput())) return;

        itemNameInput.setTextBoxInput(plainTextString);
    }

    private void updateQueryString() {
        // Create the whole query based on the filters, sorts and the item name
        String queryString =
                Services.ItemFilter.getItemFilterString(filterMap, sorts, List.of(itemNameInput.getTextBoxInput()));

        // Don't want to update the search widget if the query is the same (avoid recursion loop)
        if (Objects.equals(itemSearchWidget.getTextBoxInput(), queryString)) return;

        itemSearchWidget.setTextBoxInput(queryString);
        // The active button does not need to be active,
        // as the changes are only made when the user interacted
        // with other widgets than the search widget itself
        if (applyButton != null) {
            applyButton.active = false;
        }

        savePresetButton.active = !presetNameInput.getTextBoxInput().isEmpty()
                && !itemSearchWidget.getTextBoxInput().isEmpty();
    }

    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Preset and filter buttons have a small bit rendered underneath the background, that shouldn't show tooltip
        if (hovered instanceof PresetButton || hovered instanceof FilterOptionsButton) {
            if (MathUtils.isInside(
                    mouseX,
                    mouseY,
                    offsetX,
                    Texture.ITEM_FILTER_BACKGROUND.width() + offsetX,
                    offsetY,
                    Texture.ITEM_FILTER_BACKGROUND.height() + offsetY)) {
                return;
            }
        }

        List<Component> tooltipLines = List.of();

        if (hovered instanceof TooltipProvider tooltipWidget) {
            tooltipLines = tooltipWidget.getTooltipLines();
        }

        if (tooltipLines.isEmpty()) return;

        guiGraphics.renderComponentTooltip(FontRenderer.getInstance().getFont(), tooltipLines, mouseX, mouseY);
    }

    private void renderProvidersScroll(PoseStack poseStack) {
        providerScrollY = 24
                + offsetY
                + MathUtils.map(
                        providersScrollOffset,
                        0,
                        getMaxProviderScrollOffset(),
                        0,
                        177 - Texture.CONFIG_BOOK_SCROLL_BUTTON.height());

        RenderUtils.drawTexturedRect(poseStack, Texture.SCROLL_BUTTON, 133 + offsetX, providerScrollY);
    }

    private void renderSortScroll(PoseStack poseStack) {
        RenderUtils.drawRect(
                poseStack, CommonColors.LIGHT_GRAY, 330 + offsetX, 30 + offsetY, 0, 6, MAX_SORTS_PER_PAGE * 21);

        sortScrollY = 30
                + offsetY
                + MathUtils.map(sortScrollOffset, 0, getMaxSortsScrollOffset(), 0, MAX_SORTS_PER_PAGE * 21 - 20);

        RenderUtils.drawRect(
                poseStack,
                draggingSortScroll ? CommonColors.BLACK : CommonColors.GRAY,
                330 + offsetX,
                sortScrollY,
                0,
                6,
                20);
    }

    private void setSelectedFilter(FilterType newFilter) {
        selectedFilterButton.setIsSelected(false);

        // Set which buttons is selected to change its texture
        switch (newFilter) {
            case USED -> {
                usedButton.setIsSelected(true);
                selectedFilterButton = usedButton;
            }
            case UNUSED -> {
                unusedButton.setIsSelected(true);
                selectedFilterButton = unusedButton;
            }
            default -> {
                allButton.setIsSelected(true);
                selectedFilterButton = allButton;
            }
        }

        // Update the filter type and repopulate the providers list
        filterType = newFilter;
        providersScrollOffset = 0;

        updateProviderWidgets();
    }

    private boolean searchMatches(String name) {
        return StringUtils.partialMatch(name, providerSearchWidget.getTextBoxInput());
    }

    private Stream<GuiEventListener> getWidgetsForIteration() {
        return Stream.concat(
                children.stream(),
                Stream.concat(providerButtons.stream(), Stream.concat(sortButtons.stream(), presetButtons.stream())));
    }

    private enum FilterType {
        ALL,
        USED,
        UNUSED
    }
}
