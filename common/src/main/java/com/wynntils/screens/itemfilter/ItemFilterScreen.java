/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.ItemSearchHelperWidget;
import com.wynntils.screens.base.widgets.ItemSearchWidget;
import com.wynntils.screens.base.widgets.SearchWidget;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.itemfilter.widgets.BooleanValueWidget;
import com.wynntils.screens.itemfilter.widgets.FilterOptionsButton;
import com.wynntils.screens.itemfilter.widgets.GeneralValueWidget;
import com.wynntils.screens.itemfilter.widgets.IntegerValueWidget;
import com.wynntils.screens.itemfilter.widgets.ListValueWidget;
import com.wynntils.screens.itemfilter.widgets.PresetButton;
import com.wynntils.screens.itemfilter.widgets.ProviderButton;
import com.wynntils.screens.itemfilter.widgets.SortWidget;
import com.wynntils.screens.itemfilter.widgets.StringValueWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.SortInfo;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.services.itemfilter.type.StatValue;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    private static final int MAX_PRESETS = 4;
    private static final int MAX_PROVIDERS_PER_PAGE = 8;
    private static final int MAX_SORTS_PER_PAGE = 7;

    // Collections
    private List<ItemStatProvider<?>> itemStatProviders = new ArrayList<>();
    private Set<ItemStatProvider<?>> usedProviders = new HashSet<>();
    private List<Pair<ItemStatProvider<?>, String>> filters = new ArrayList<>();
    private List<Pair<String, String>> presets;
    private List<SortInfo> sorts = new ArrayList<>();
    private List<SortWidget> sortButtons = new ArrayList<>();
    private List<WynntilsButton> presetButtons = new ArrayList<>();
    private List<WynntilsButton> providerButtons = new ArrayList<>();

    // Renderables
    private final SearchWidget itemSearchWidget;
    private final SearchWidget previousSearchWidget;
    private final SearchWidget providerSearchWidget;
    private final TextInputBoxWidget itemNameInput;
    private Button savePresetButton;
    private FilterOptionsButton allButton;
    private FilterOptionsButton usedButton;
    private FilterOptionsButton unusedButton;
    private FilterOptionsButton selectedFilterButton;
    private GeneralValueWidget valueWidget;
    private TextInputBoxWidget focusedTextInput;
    private TextInputBoxWidget presetNameInput;
    private WynntilsButton helperButton;
    private WynntilsButton nextPresetButton;
    private WynntilsButton previousPresetButton;

    // UI size, positions, etc
    private boolean draggingProviderScroll = false;
    private boolean draggingSortScroll = false;
    private double currentUnusedProviderScroll = 0;
    private double currentUnusedSortScroll = 0;
    private float providerScrollY;
    private float sortScrollY;
    private float translationX;
    private float translationY;
    private int presetsScrollOffset = 0;
    private int providersScrollOffset = 0;
    private int sortScrollOffset = 0;
    private Renderable hovered = null;

    // Screen information
    private final boolean supportsSorting;
    private final Screen previousScreen;
    private ItemStatProvider<?> selectedProvider;
    private boolean ignoreUpdate = false;
    private boolean sortMode = false;
    private FilterType filterType = FilterType.ALL;
    private String filterQuery = "";
    private String sortQuery = "";

    private ItemFilterScreen(SearchWidget searchWidget, Screen previousScreen, boolean supportsSorting) {
        super(Component.literal("Item Filter Screen"));

        this.previousSearchWidget = searchWidget;
        this.previousScreen = previousScreen;
        this.supportsSorting = supportsSorting;

        // region Input widgets
        itemNameInput = new TextInputBoxWidget(
                220,
                5,
                100,
                18,
                (s -> {
                    if (ignoreUpdate) {
                        ignoreUpdate = false;
                    } else {
                        populateQuery();
                    }
                }),
                this);

        this.providerSearchWidget = new SearchWidget(
                7,
                5,
                100,
                20,
                (s) -> {
                    providersScrollOffset = 0;
                    populateProviders();
                },
                this);

        this.itemSearchWidget = new ItemSearchWidget(
                0,
                -22,
                Texture.ITEM_FILTER_BACKGROUND.width() - 20,
                20,
                supportsSorting,
                (q -> {
                    if (ignoreUpdate) {
                        ignoreUpdate = false;
                    } else {
                        parseFilters(q.queryString());
                        populateProviders();

                        if (sortMode) {
                            populateSorts();
                        }
                    }
                }),
                this);

        this.itemSearchWidget.setTextBoxInput(previousSearchWidget.getTextBoxInput());
        // endregion

        setFocusedTextInput(providerSearchWidget);

        presets = Services.ItemFilter.presets.get();
    }

    public static Screen create(SearchWidget searchWidget, Screen previousScreen, boolean supportsSorting) {
        return new ItemFilterScreen(searchWidget, previousScreen, supportsSorting);
    }

    @Override
    protected void doInit() {
        super.doInit();

        translationX = (this.width - Texture.ITEM_FILTER_BACKGROUND.width()) / 2f;
        translationY = (this.height - Texture.ITEM_FILTER_BACKGROUND.height()) / 2f;

        this.addRenderableWidget(itemSearchWidget);
        this.addRenderableWidget(providerSearchWidget);

        helperButton = new ItemSearchHelperWidget(
                Texture.ITEM_FILTER_BACKGROUND.width() - 37,
                -19,
                (int) (Texture.INFO.width() / 1.7f),
                (int) (Texture.INFO.height() / 1.7f),
                Texture.INFO,
                true);

        this.addRenderableWidget(helperButton);

        this.addRenderableWidget(itemNameInput);

        presetNameInput = new TextInputBoxWidget(220, 180, 100, 20, (s -> checkSaveStatus()), this, presetNameInput);

        this.addRenderableWidget(presetNameInput);

        // region State buttons
        Button toggleSortButton = new Button.Builder(Component.literal("🔄"), (button -> toggleSortMode()))
                .pos(108, 5)
                .size(20, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.itemFilter.sortToggle")))
                .build();

        this.addRenderableWidget(toggleSortButton);

        Button returnButton = new Button.Builder(Component.literal("⏎"), (button -> onClose()))
                .pos(Texture.ITEM_FILTER_BACKGROUND.width() - 18, -22)
                .size(20, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.itemFilter.return")))
                .build();

        this.addRenderableWidget(returnButton);
        // endregion

        // region Preset buttons
        savePresetButton = new Button.Builder(Component.literal("💾"), (button -> savePreset()))
                .pos(330, 180)
                .size(20, 20)
                .build();

        savePresetButton.active = !presetNameInput.getTextBoxInput().isEmpty()
                && !itemSearchWidget.getTextBoxInput().isEmpty();

        this.addRenderableWidget(savePresetButton);

        previousPresetButton = this.addRenderableWidget(new PresetButton(
                Texture.ITEM_FILTER_BACKGROUND.width() - 4,
                4,
                StyledText.fromString("🠝"),
                (b) -> scrollPresets(-1),
                List.of(Component.translatable("screens.wynntils.itemFilter.scrollUp")),
                translationX,
                translationY));

        nextPresetButton = this.addRenderableWidget(new PresetButton(
                Texture.ITEM_FILTER_BACKGROUND.width() - 4,
                174,
                StyledText.fromString("🠟"),
                (b) -> scrollPresets(1),
                List.of(Component.translatable("screens.wynntils.itemFilter.scrollDown")),
                translationX,
                translationY));

        if (presets.size() <= MAX_PRESETS) {
            previousPresetButton.visible = false;
            nextPresetButton.visible = false;
        }
        // endregion

        // region Filter type buttons
        allButton = new FilterOptionsButton(
                -(Texture.PAPER_BUTTON_LEFT.width()) + 4,
                8,
                Texture.PAPER_BUTTON_LEFT.width(),
                Texture.PAPER_BUTTON_LEFT.height() / 2,
                StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.allFilter")),
                (button) -> setSelectedFilter(FilterType.ALL),
                List.of(Component.translatable("screens.wynntils.itemFilter.allFilterTooltip")),
                Texture.PAPER_BUTTON_LEFT,
                filterType == FilterType.ALL);

        usedButton = new FilterOptionsButton(
                -(Texture.PAPER_BUTTON_LEFT.width()) + 4,
                12 + Texture.PAPER_BUTTON_LEFT.height() / 2,
                Texture.PAPER_BUTTON_LEFT.width(),
                Texture.PAPER_BUTTON_LEFT.height() / 2,
                StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.usedFilter")),
                (button) -> setSelectedFilter(FilterType.USED),
                List.of(Component.translatable("screens.wynntils.itemFilter.usedFilterTooltip")),
                Texture.PAPER_BUTTON_LEFT,
                filterType == FilterType.USED);

        unusedButton = new FilterOptionsButton(
                -(Texture.PAPER_BUTTON_LEFT.width()) + 4,
                16 + (Texture.PAPER_BUTTON_LEFT.height() / 2) * 2,
                Texture.PAPER_BUTTON_LEFT.width(),
                Texture.PAPER_BUTTON_LEFT.height() / 2,
                StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.unusedFilter")),
                (button) -> setSelectedFilter(FilterType.UNUSED),
                List.of(Component.translatable("screens.wynntils.itemFilter.unusedFilterTooltip")),
                Texture.PAPER_BUTTON_LEFT,
                filterType == FilterType.UNUSED);

        switch (filterType) {
            case USED -> selectedFilterButton = usedButton;
            case UNUSED -> selectedFilterButton = unusedButton;
            default -> selectedFilterButton = allButton;
        }

        this.addRenderableWidget(allButton);
        this.addRenderableWidget(usedButton);
        this.addRenderableWidget(unusedButton);
        // endregion

        valueWidget = null;

        populateProviders();
        populatePresets();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        hovered = null;

        poseStack.pushPose();
        poseStack.translate(translationX, translationY, 0);

        RenderUtils.drawTexturedRect(poseStack, Texture.ITEM_FILTER_BACKGROUND, 0, 0);

        if (selectedProvider == null && !sortMode) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.itemFilter.unselectedFilter")),
                            147,
                            345,
                            63,
                            123,
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
                            147,
                            345,
                            63,
                            123,
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
                            150,
                            10,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NORMAL);
        }

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.presetName")),
                        150,
                        185,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        int adjustedMouseX = mouseX - (int) translationX;
        int adjustedMouseY = mouseY - (int) translationY;

        for (Renderable renderable : renderables) {
            renderable.render(guiGraphics, adjustedMouseX, adjustedMouseY, partialTick);

            if (renderable instanceof WynntilsButton wynntilsButton) {
                if (wynntilsButton.isMouseOver(adjustedMouseX, adjustedMouseY)) {
                    hovered = renderable;
                }
            }
        }

        if (providerButtons.isEmpty()) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromComponent(Component.translatable("screens.wynntils.itemFilter.noProviders")),
                            8,
                            127,
                            28,
                            169,
                            118,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        } else {
            for (Renderable renderable : providerButtons) {
                renderable.render(guiGraphics, adjustedMouseX, adjustedMouseY, partialTick);
            }
        }

        for (Renderable renderable : sortButtons) {
            renderable.render(guiGraphics, adjustedMouseX, adjustedMouseY, partialTick);
        }

        for (Renderable renderable : presetButtons) {
            renderable.render(guiGraphics, adjustedMouseX, adjustedMouseY, partialTick);

            if (renderable instanceof WynntilsButton wynntilsButton) {
                if (wynntilsButton.isMouseOver(adjustedMouseX, adjustedMouseY)) {
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

        poseStack.popPose();

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
        McUtils.mc().setScreen(previousScreen);
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        double adjustedMouseX = mouseX - translationX;
        double adjustedMouseY = mouseY - translationY;

        // Don't want to clear the text when right clicking
        if (helperButton.mouseClicked(adjustedMouseX, adjustedMouseY, button)) {
            return false;
        }

        if (!draggingProviderScroll && itemStatProviders.size() > MAX_PROVIDERS_PER_PAGE) {
            if (MathUtils.isInside(
                    (int) adjustedMouseX,
                    (int) adjustedMouseY,
                    132,
                    132 + Texture.SCROLL_BUTTON.width(),
                    (int) providerScrollY,
                    (int) (providerScrollY + Texture.SCROLL_BUTTON.height()))) {
                draggingProviderScroll = true;

                return true;
            }
        }

        if (sortMode && !draggingSortScroll && sorts.size() > MAX_SORTS_PER_PAGE) {
            if (MathUtils.isInside((int) adjustedMouseX, (int) adjustedMouseY, 330, 336, (int) sortScrollY, (int)
                    (sortScrollY + 20))) {
                draggingSortScroll = true;

                return true;
            }
        }

        for (GuiEventListener listener : getWidgetsForIteration().toList()) {
            if (listener.isMouseOver(adjustedMouseX, adjustedMouseY)) {
                // Preset and filter buttons have a slight bit rendered underneath the background but we don't want that
                // part to be clickable
                if (listener instanceof PresetButton || listener instanceof FilterOptionsButton) {
                    if (MathUtils.isInside(
                            (int) adjustedMouseX,
                            (int) adjustedMouseY,
                            0,
                            Texture.ITEM_FILTER_BACKGROUND.width(),
                            0,
                            Texture.ITEM_FILTER_BACKGROUND.height())) {
                        return false;
                    }
                }

                listener.mouseClicked(adjustedMouseX, adjustedMouseY, button);
            }
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double adjustedMouseX = mouseX - translationX;
        double adjustedMouseY = mouseY - translationY;

        if (valueWidget != null && valueWidget.mouseDragged(adjustedMouseX, adjustedMouseY, button, dragX, dragY)) {
            return true;
        }

        if (draggingProviderScroll) {
            int renderY = 24;
            int scrollAreaStartY = renderY + 9;

            int newValue = Math.round(MathUtils.map(
                    (float) adjustedMouseY,
                    scrollAreaStartY,
                    scrollAreaStartY + 160,
                    0,
                    Math.max(0, itemStatProviders.size() - MAX_PROVIDERS_PER_PAGE)));

            scrollProviders(newValue - providersScrollOffset);

            return super.mouseDragged(adjustedMouseX, adjustedMouseY, button, dragX, dragY);
        } else if (draggingSortScroll) {
            int renderY = 30;
            int scrollAreaStartY = renderY + 10;

            int newValue = Math.round(MathUtils.map(
                    (float) adjustedMouseY,
                    scrollAreaStartY,
                    scrollAreaStartY + MAX_SORTS_PER_PAGE * 21 - 20,
                    0,
                    Math.max(0, sorts.size() - MAX_SORTS_PER_PAGE)));

            scrollSorts(newValue - sortScrollOffset);

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double adjustedMouseX = mouseX - translationX;
        double adjustedMouseY = mouseY - translationY;

        for (GuiEventListener listener : this.children) {
            if (listener.isMouseOver(adjustedMouseX, adjustedMouseY)) {
                listener.mouseReleased(adjustedMouseX, adjustedMouseY, button);
            }
        }

        draggingProviderScroll = false;
        draggingSortScroll = false;

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        double adjustedMouseX = mouseX - translationX;
        double adjustedMouseY = mouseY - translationY;
        double scrollValue = -Math.signum(deltaY);

        for (GuiEventListener listener : this.children) {
            if (listener.mouseScrolled(adjustedMouseX, adjustedMouseY, deltaX, deltaY)) {
                return true;
            }
        }

        if (presets.size() > MAX_PRESETS && adjustedMouseX >= Texture.ITEM_FILTER_BACKGROUND.width()) {
            scrollPresets((int) scrollValue);
            return true;
        }

        if (sortMode && adjustedMouseX >= 150) {
            if (Math.abs(deltaY) == 1.0) {
                scrollSorts((int) -deltaY);
                return true;
            }

            // Account for scrollpad
            currentUnusedSortScroll -= deltaY / 5d;

            if (Math.abs(currentUnusedSortScroll) < 1) return true;

            int scroll = (int) (currentUnusedSortScroll);
            currentUnusedSortScroll = currentUnusedSortScroll % 1;

            scrollSorts(scroll);
        } else {
            if (Math.abs(deltaY) == 1.0) {
                scrollProviders((int) -deltaY);
                return true;
            }

            // Account for scrollpad
            currentUnusedProviderScroll -= deltaY / 5d;

            if (Math.abs(currentUnusedProviderScroll) < 1) return true;

            int scroll = (int) (currentUnusedProviderScroll);
            currentUnusedProviderScroll = currentUnusedProviderScroll % 1;

            scrollProviders(scroll);
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

    public void addFilter(Pair<ItemStatProvider<?>, String> newFilter) {
        filters.add(newFilter);
        usedProviders.add(newFilter.a());

        createFilterQuery();
        populateQuery();
        // Sorts are not required to be repopulated here as filters can only be added
        // when not in sort view mode
    }

    public void removeFilter(ItemStatProvider<?> provider) {
        // Remove all instances of the provider in the filter list
        filters = filters.stream().filter(filter -> filter.a() != provider).collect(Collectors.toList());

        usedProviders =
                usedProviders.stream().filter(filter -> filter != provider).collect(Collectors.toSet());

        createFilterQuery();
        populateQuery();
        // Same as above method, sorts aren't required to be repopulated here
    }

    public void addSort(SortInfo newSort) {
        sorts = sorts.stream()
                .filter(sort -> sort.provider() != newSort.provider())
                .collect(Collectors.toList());

        sorts.add(newSort);
        usedProviders.add(newSort.provider());

        createSortQuery();
        populateQuery();
        populateSorts();
    }

    public void removeSort(ItemStatProvider<?> provider) {
        // Remove all instances of the provider in the sort list
        sorts.removeIf(sort -> sort.provider() == provider);
        usedProviders.removeIf(p -> p == provider);

        createSortQuery();
        populateQuery();
        populateSorts();
    }

    public void changeSort(SortInfo oldSortInfo, SortInfo newSortInfo) {
        sorts = sorts.stream()
                .map(sort -> {
                    if (sort.equals(oldSortInfo)) return newSortInfo;
                    return sort;
                })
                .collect(Collectors.toList());

        createSortQuery();
        populateQuery();
        populateSorts();
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

        createSortQuery();
        populateQuery();
        populateSorts();
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
        // If there is no value widget or a new provider has been selected then
        // create a new value widget, otherwise we just want to update the values
        // for the existing widget to avoid triggering consumers
        if (valueWidget == null || this.selectedProvider != selectedProvider) {
            this.selectedProvider = selectedProvider;
            createValueWidget();
        } else {
            Optional<String> valuesOpt = filters.stream()
                    .filter(value -> value.a() == selectedProvider)
                    .map(Pair::b)
                    .findFirst();

            List<String> values = valuesOpt.map(s -> List.of(s.split(","))).orElseGet(() -> List.of(""));

            valueWidget.updateValues(values);
        }
    }

    public ItemStatProvider<?> getSelectedProvider() {
        return selectedProvider;
    }

    public boolean isProviderInUse(ItemStatProvider<?> provider) {
        return usedProviders.contains(provider);
    }

    private void populateProviders() {
        for (AbstractWidget widget : providerButtons) {
            this.removeWidget(widget);
        }

        providerButtons = new ArrayList<>();

        itemStatProviders = Services.ItemFilter.getItemStatProviders().stream()
                .filter(provider -> searchMatches(provider.getDisplayName()))
                .toList();

        // Filter the providers if not using the ALL type
        if (filterType == FilterType.USED) {
            itemStatProviders = Services.ItemFilter.getItemStatProviders().stream()
                    .filter(provider -> usedProviders.contains(provider))
                    .toList();
        } else if (filterType == FilterType.UNUSED) {
            itemStatProviders = Services.ItemFilter.getItemStatProviders().stream()
                    .filter(provider -> !usedProviders.contains(provider))
                    .toList();
        }

        int currentProviderIndex;
        int yPos = 31;

        for (int i = 0; i < MAX_PROVIDERS_PER_PAGE; i++) {
            currentProviderIndex = i + providersScrollOffset;

            if (itemStatProviders.size() - 1 < currentProviderIndex) break;

            ItemStatProvider<?> currentProvider = itemStatProviders.get(currentProviderIndex);

            providerButtons.add(new ProviderButton(
                    7, yPos, 120, 18, this, itemStatProviders.get(currentProviderIndex), translationX, translationY));

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
    }

    private void populatePresets() {
        for (AbstractWidget widget : presetButtons) {
            this.removeWidget(widget);
        }

        presetButtons = new ArrayList<>();

        if (presets.isEmpty()) return;

        int yPos = 4;

        for (int i = 0; i < Math.min(MAX_PRESETS, presets.size()); i++) {
            yPos += Texture.PAPER_BUTTON_RIGHT.height() / 2 + 6;
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
                    Texture.ITEM_FILTER_BACKGROUND.width() - 4,
                    yPos,
                    StyledText.fromString(presetName),
                    (b) -> clickPreset(b, presetIndex),
                    tooltip,
                    translationX,
                    translationY));
        }
    }

    private void populateSorts() {
        for (AbstractWidget widget : sortButtons) {
            this.removeWidget(widget);
        }

        sortButtons = new ArrayList<>();

        int currentSortIndex;
        int yPos = 29;

        for (int i = 0; i < MAX_SORTS_PER_PAGE; i++) {
            currentSortIndex = i + sortScrollOffset;

            if (sorts.size() - 1 < currentSortIndex) break;

            sortButtons.add(new SortWidget(150, yPos, this, translationX, translationY, sorts.get(i)));

            yPos += 21;
        }
    }

    private void createValueWidget() {
        if (valueWidget != null) {
            this.removeWidget(valueWidget);
        }

        valueWidget = getWidgetFromProvider();

        this.addRenderableWidget(valueWidget);
    }

    private GeneralValueWidget getWidgetFromProvider() {
        Optional<String> valuesOpt = filters.stream()
                .filter(value -> value.a() == selectedProvider)
                .map(Pair::b)
                .findFirst();

        List<String> values = valuesOpt.map(s -> List.of(s.split(","))).orElseGet(() -> List.of(""));

        if (selectedProvider.getValidInputs().isEmpty()) {
            if (selectedProvider.getType().equals(String.class)) {
                return new StringValueWidget(values, this);
            } else if (selectedProvider.getType().equals(Boolean.class)) {
                return new BooleanValueWidget(values.get(0), this);
            } else { // Integer, CappedValue and StatValue
                return new IntegerValueWidget(values, selectedProvider.getType().equals(StatValue.class), this);
            }
        } else {
            return new ListValueWidget(translationX, translationY, selectedProvider.getValidInputs(), values, this);
        }
    }

    private void scrollProviders(int delta) {
        providersScrollOffset = MathUtils.clamp(
                providersScrollOffset + delta, 0, Math.max(0, itemStatProviders.size() - MAX_PROVIDERS_PER_PAGE));

        populateProviders();
    }

    private void scrollSorts(int delta) {
        sortScrollOffset = MathUtils.clamp(sortScrollOffset + delta, 0, Math.max(0, sorts.size() - MAX_SORTS_PER_PAGE));

        populateSorts();
    }

    private void scrollPresets(int direction) {
        if (Math.abs(presetsScrollOffset + direction) == presets.size()) {
            presetsScrollOffset = 0;
        } else {
            presetsScrollOffset =
                    MathUtils.clamp(presetsScrollOffset + direction, -(presets.size() - 1), (presets.size() - 1));
        }

        populatePresets();
    }

    private void clickPreset(int button, int presetIndex) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (KeyboardUtils.isShiftDown()) { // Shift the preset up
                int indexToSwap = presetIndex == 0 ? presets.size() - 1 : presetIndex - 1;

                Collections.swap(presets, presetIndex, indexToSwap);

                Services.ItemFilter.presets.store(presets);
                Services.ItemFilter.presets.touched();

                presets = Services.ItemFilter.presets.get();

                populatePresets();
            } else { // Select the preset
                itemSearchWidget.setTextBoxInput(presets.get(presetIndex).b());
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (KeyboardUtils.isShiftDown()) { // Shift the preset down
                int indexToSwap = presetIndex == presets.size() - 1 ? 0 : presetIndex + 1;

                Collections.swap(presets, presetIndex, indexToSwap);

                Services.ItemFilter.presets.store(presets);
                Services.ItemFilter.presets.touched();

                presets = Services.ItemFilter.presets.get();

                populatePresets();
            } else if (KeyboardUtils.isControlDown()) { // Delete the preset
                presets.remove(presetIndex);

                Services.ItemFilter.presets.store(presets);
                Services.ItemFilter.presets.touched();

                presets = Services.ItemFilter.presets.get();

                presetsScrollOffset = Math.max(presetsScrollOffset - 1, 0);

                populatePresets();

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

        populatePresets();
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
            if (valueWidget != null) {
                selectedProvider = null;
                this.removeWidget(valueWidget);
            }

            sortScrollOffset = 0;

            populateSorts();
        } else {
            for (AbstractWidget widget : sortButtons) {
                this.removeWidget(widget);
            }

            sortButtons = new ArrayList<>();
        }
    }

    private void parseFilters(String input) {
        List<Pair<ItemStatProvider<?>, String>> newFilters = new ArrayList<>();
        Set<ItemStatProvider<?>> newUsedProviders = new HashSet<>();

        ItemSearchQuery searchQuery = Services.ItemFilter.createSearchQuery(input, true);

        // filters =
        for (StatProviderAndFilterPair value : searchQuery.filters().values()) {
            ItemStatProvider itemStatProvider = value.statProvider();
            newUsedProviders.add(itemStatProvider);
        }

        sorts = searchQuery.sorts();
        newUsedProviders.addAll(
                searchQuery.sorts().stream().map(SortInfo::provider).toList());

        usedProviders = newUsedProviders;

        createFilterQuery();
        createSortQuery();

        ignoreUpdate = true;
        itemNameInput.setTextBoxInput(String.join(" ", searchQuery.plainTextTokens()));
    }

    private void createFilterQuery() {
        filterQuery = "";

        if (filters.isEmpty()) return;

        StringBuilder filterBuilder = new StringBuilder();

        for (Pair<ItemStatProvider<?>, String> filter : filters) {
            filterBuilder.append(filter.a().getName()).append(":").append(filter.b());

            // Append a space to all filters besides the last
            if (!filter.b().isEmpty()) {
                filterBuilder.append(" ");
            }
        }

        filterQuery = filterBuilder.toString().trim();
    }

    private void createSortQuery() {
        sortQuery = "";

        if (sorts.isEmpty()) return;

        StringBuilder sortBuilder = new StringBuilder(Services.ItemFilter.SORT_KEY);
        sortBuilder.append(":");

        for (SortInfo sortInfo : sorts) {
            sortBuilder.append(sortInfo.provider().getName());

            // Append the seperator for all sorts except the last
            if (sorts.indexOf(sortInfo) != sorts.size() - 1) {
                sortBuilder.append(Services.ItemFilter.LIST_SEPARATOR);
            }
        }

        sortQuery = sortBuilder.toString();
    }

    private void populateQuery() {
        // Create the whole query based on the filters, sorts and the item name
        StringBuilder totalQuery = new StringBuilder();

        if (!filterQuery.isEmpty()) {
            totalQuery.append(filterQuery);
        }

        if (!sortQuery.isEmpty()) {
            if (!totalQuery.isEmpty()) {
                totalQuery.append(" ");
            }

            totalQuery.append(sortQuery);
        }

        if (!itemNameInput.getTextBoxInput().isEmpty()) {
            if (!totalQuery.isEmpty()) {
                totalQuery.append(" ");
            }

            totalQuery.append(itemNameInput.getTextBoxInput());
        }

        ignoreUpdate = true;

        itemSearchWidget.setTextBoxInput(totalQuery.toString());
    }

    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Preset and filter buttons have a small bit rendered underneath the background, that shouldn't show tooltip
        if (hovered instanceof PresetButton || hovered instanceof FilterOptionsButton) {
            int adjustedMouseX = mouseX - (int) translationX;
            int adjustedMouseY = mouseY - (int) translationY;

            if (MathUtils.isInside(
                    adjustedMouseX,
                    adjustedMouseY,
                    0,
                    Texture.ITEM_FILTER_BACKGROUND.width(),
                    0,
                    Texture.ITEM_FILTER_BACKGROUND.height())) {
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
        providerScrollY =
                24 + MathUtils.map(providersScrollOffset, 0, itemStatProviders.size() - MAX_PROVIDERS_PER_PAGE, 0, 160);

        RenderUtils.drawTexturedRect(poseStack, Texture.SCROLL_BUTTON, 132, providerScrollY);
    }

    private void renderSortScroll(PoseStack poseStack) {
        RenderUtils.drawRect(poseStack, CommonColors.LIGHT_GRAY, 330, 30, 0, 6, MAX_SORTS_PER_PAGE * 21);

        sortScrollY = 30
                + MathUtils.map(
                        sortScrollOffset, 0, sorts.size() - MAX_SORTS_PER_PAGE, 0, MAX_SORTS_PER_PAGE * 21 - 20);

        RenderUtils.drawRect(
                poseStack, draggingSortScroll ? CommonColors.BLACK : CommonColors.GRAY, 330, sortScrollY, 0, 6, 20);
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

        populateProviders();
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
