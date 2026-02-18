/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.trademarket;

import com.google.common.collect.Lists;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.wrappedscreen.WrappedScreen;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.WynntilsContainerScreen;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.screens.base.widgets.ItemFilterUIButton;
import com.wynntils.screens.base.widgets.ItemSearchHelperWidget;
import com.wynntils.screens.base.widgets.ItemSearchWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class TradeMarketSearchResultScreen extends WynntilsContainerScreen<TradeMarketMenu> implements WrappedScreen {
    // Constants
    private static final int FAKE_CONTAINER_ID = 454545;
    private static final StyledText TRADE_MARKET_BACKGROUND =
            StyledText.fromComponent(Component.literal("\uDAFF\uDFE8\uE011")
                    .withStyle(Style.EMPTY.withFont(
                            new FontDescription.Resource(Identifier.withDefaultNamespace("interface")))));
    private static final Identifier INVENTORY_BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int SCROLL_AREA_HEIGHT = 100;
    private static final int ITEMS_PER_PAGE = 45;

    // Info
    private final TradeMarketSearchResultHolder holder;
    private final WrappedScreenInfo wrappedScreenInfo;

    // Widgets
    private ItemSearchWidget itemSearchWidget;
    private BasicTexturedButton sortOptionsButton;

    // State
    private Component currentState = Component.empty();

    // Scrolling
    private int scrollOffset = 0;
    private boolean holdingScrollbar = false;

    protected TradeMarketSearchResultScreen(WrappedScreenInfo wrappedScreenInfo, TradeMarketSearchResultHolder holder) {
        super(
                TradeMarketMenu.create(FAKE_CONTAINER_ID, McUtils.inventory()),
                McUtils.inventory(),
                Component.literal("Trade Market Search Result Wrapped Screen"));

        // Make our screen display according to the number of items, at the correct y position
        this.imageHeight = 114 + this.getMenu().getRowCount() * 18;
        this.inventoryLabelY = this.imageHeight - 94;

        this.holder = holder;
        this.wrappedScreenInfo = wrappedScreenInfo;
    }

    @Override
    public WrappedScreenInfo getWrappedScreenInfo() {
        return wrappedScreenInfo;
    }

    @Override
    protected void doInit() {
        super.doInit();

        // This is screen.topPos and screen.leftPos, but they are not calculated yet when this is called
        int renderX = (width - imageWidth) / 2;
        int renderY = (height - imageHeight) / 2 - 22;

        itemSearchWidget = new ItemSearchWidget(
                renderX + 8,
                this.topPos + this.menu.getRowCount() * 18,
                140,
                16,
                ItemProviderType.normalTypes(),
                true,
                (query) -> {
                    saveSearchFilter(query);
                    reloadElements();
                },
                (TextboxScreen) this);
        this.addRenderableWidget(itemSearchWidget);

        // Set the last search filter, if we opened a new screen
        // On reloads, this should not change anything
        itemSearchWidget.setTextBoxInput(Models.TradeMarket.getLastSearchFilter());

        this.addRenderableWidget(new ItemFilterUIButton(
                itemSearchWidget.getX() + itemSearchWidget.getWidth() + 2,
                itemSearchWidget.getY(),
                16,
                16,
                itemSearchWidget,
                this,
                true,
                Arrays.stream(ItemProviderType.values()).toList()));

        WynntilsButton backButton = new BasicTexturedButton(
                renderX - Texture.TRADE_MARKET_SIDEBAR.width() / 2 + titleLabelX - 2,
                renderY + Texture.TRADE_MARKET_SIDEBAR.height() - 26 - titleLabelY,
                Texture.ARROW_LEFT_ICON.width(),
                Texture.ARROW_LEFT_ICON.height(),
                Texture.ARROW_LEFT_ICON,
                (button) -> holder.goBackToSearch(),
                List.of(Component.translatable("screens.wynntils.tradeMarketSearchResult.backToSearch")
                        .withStyle(ChatFormatting.BOLD)));
        this.addRenderableWidget(backButton);

        WynntilsButton loadMoreButton = new BasicTexturedButton(
                renderX - Texture.TRADE_MARKET_SIDEBAR.width() / 2 + titleLabelX - 2,
                renderY + Texture.TRADE_MARKET_SIDEBAR.height() - 43 - titleLabelY,
                Texture.SMALL_ADD_ICON.width(),
                Texture.SMALL_ADD_ICON.height(),
                Texture.SMALL_ADD_ICON,
                (button) -> holder.loadNextPageBatch(),
                List.of(Component.translatable(
                                "screens.wynntils.tradeMarketSearchResult.loadNextBatch", holder.getPageLoadBatchSize())
                        .withStyle(ChatFormatting.BOLD)));
        this.addRenderableWidget(loadMoreButton);

        sortOptionsButton = new BasicTexturedButton(
                renderX - Texture.TRADE_MARKET_SIDEBAR.width() / 2 + titleLabelX - 2,
                renderY + Texture.TRADE_MARKET_SIDEBAR.height() - 60 - titleLabelY,
                Texture.EDIT_NAME_ICON.width(),
                Texture.EDIT_NAME_ICON.height(),
                Texture.EDIT_NAME_ICON,
                holder::changeSortingMode,
                holder.getSortingItemTooltip());
        this.addRenderableWidget(sortOptionsButton);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateItems();

        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        renderScrollButton(guiGraphics);

        renderables.forEach(c -> c.render(guiGraphics, mouseX, mouseY, partialTick));

        // Render item tooltip
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        // Render tooltip for hovered widget
        for (GuiEventListener child : children()) {
            if (child instanceof TooltipProvider tooltipProvider && child.isMouseOver(mouseX, mouseY)) {
                guiGraphics.setTooltipForNextFrame(
                        Lists.transform(tooltipProvider.getTooltipLines(), Component::getVisualOrderText),
                        mouseX,
                        mouseY);
                break;
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(
                FontRenderer.getInstance().getFont(),
                this.currentState,
                this.titleLabelX,
                this.titleLabelY,
                CommonColors.WHITE.asInt(),
                false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Inventory
        RenderUtils.drawTexturedRect(
                guiGraphics,
                INVENTORY_BACKGROUND,
                x,
                y + this.menu.getRowCount() * 18 + 17,
                this.imageWidth,
                96,
                0,
                126,
                this.imageWidth,
                96,
                256,
                256);

        // Container
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(this.leftPos, this.topPos);
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        TRADE_MARKET_BACKGROUND,
                        this.titleLabelX,
                        this.titleLabelY,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);

        guiGraphics.pose().popMatrix();

        // Scrollbar
        RenderUtils.drawTexturedRect(guiGraphics, Texture.TRADE_MARKET_SCROLL, x + this.imageWidth - 9, y - 31);

        // Sidebar
        RenderUtils.drawTexturedRect(
                guiGraphics, Texture.TRADE_MARKET_SIDEBAR, x - Texture.TRADE_MARKET_SIDEBAR.width() + 9, y - 31);
    }

    private void renderScrollButton(GuiGraphics guiGraphics) {
        float renderX =
                (this.width - this.imageWidth) / 2 + this.imageWidth + Texture.TRADE_MARKET_SCROLL.width() / 2 - 23;
        float renderY = (this.height - this.imageHeight) / 2
                + Texture.SCROLLBAR_BUTTON.height() / 2
                + 6
                + MathUtils.map(scrollOffset, 0, getMaxScrollOffset(), 0, SCROLL_AREA_HEIGHT);

        RenderUtils.drawTexturedRect(guiGraphics, Texture.SCROLLBAR_BUTTON, renderX, renderY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        float scrollBarRenderX =
                (this.width - this.imageWidth) / 2 + this.imageWidth + Texture.TRADE_MARKET_SCROLL.width() / 2 - 23;
        float scrollBarRenderY = (this.height - this.imageHeight) / 2
                + Texture.SCROLLBAR_BUTTON.height() / 2
                + 6
                + MathUtils.map(scrollOffset, 0, getMaxScrollOffset(), 0, SCROLL_AREA_HEIGHT);

        if (event.x() >= scrollBarRenderX
                && event.x() <= scrollBarRenderX + Texture.SCROLLBAR_BUTTON.width()
                && event.y() >= scrollBarRenderY
                && event.y() <= scrollBarRenderY + Texture.SCROLLBAR_BUTTON.height()) {
            holdingScrollbar = true;
            return true;
        }

        if (hoveredSlot != null && hoveredSlot.index < ITEMS_PER_PAGE) {
            holder.clickOnItem(hoveredSlot.getItem());
            return true;
        }

        // Item helper widget needs special handling because it is overlaid on the search box
        for (GuiEventListener child : children()) {
            if (child instanceof ItemSearchHelperWidget) {
                if (child.mouseClicked(event, isDoubleClick)) {
                    return true;
                }
            }
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!holdingScrollbar) return false;

        int renderY = (this.height - this.imageHeight) / 2;
        int scrollAreaStartY = renderY + 20;

        int newValue = Math.round(MathUtils.map(
                (float) event.y(), scrollAreaStartY, scrollAreaStartY + SCROLL_AREA_HEIGHT, 0, getMaxScrollOffset()));

        scroll(newValue - scrollOffset);

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        holdingScrollbar = false;

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        // Signum so we only scroll 1 item at a time
        double scrollValue = -Math.signum(deltaY);
        scroll((int) scrollValue);

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    public void setSearchQuery(String query) {
        itemSearchWidget.setTextBoxInput(query);
    }

    public ItemSearchQuery getSearchQuery() {
        return itemSearchWidget.getSearchQuery();
    }

    public void onSortingModeChanged() {
        sortOptionsButton.setTooltip(holder.getSortingItemTooltip());
    }

    private void scroll(int delta) {
        int maxValue = getMaxScrollOffset();

        scrollOffset = MathUtils.clamp(scrollOffset + delta, 0, maxValue);
    }

    private int getMaxScrollOffset() {
        int maxItemOffset = Math.max(0, holder.getFilteredItems().size() - 54);
        int maxValue = maxItemOffset / 9 + (maxItemOffset % 9 > 0 ? 1 : 0);
        return maxValue;
    }

    private void updateItems() {
        List<ItemStack> filteredItems = holder.getFilteredItems();

        // Reset all items
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            this.menu.setItem(i, 0, ItemStack.EMPTY);
        }

        // Set items with filters and sorting, with scroll offset
        int itemIndex = scrollOffset * 9;
        int currentSlot = 0;
        while (itemIndex < filteredItems.size() && currentSlot < ITEMS_PER_PAGE) {
            ItemStack itemStack = filteredItems.get(itemIndex);
            this.menu.setItem(currentSlot, 0, itemStack);

            itemIndex++;
            currentSlot++;
        }
    }

    protected void setCurrentState(Component currentState) {
        this.currentState = currentState;
    }

    private void reloadElements() {
        holder.updateDisplayItems(itemSearchWidget.getSearchQuery());
        scrollOffset = 0;
    }

    private void saveSearchFilter(ItemSearchQuery query) {
        Models.TradeMarket.setLastSearchFilter(query.queryString());
    }
}
