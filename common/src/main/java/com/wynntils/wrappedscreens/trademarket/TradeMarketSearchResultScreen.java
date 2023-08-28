/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wrappedscreens.trademarket;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.handlers.wrappedscreen.WrappedScreen;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.WynntilsContainerScreen;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.screens.base.widgets.ItemSearchHelperWidget;
import com.wynntils.screens.base.widgets.ItemSearchWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;

public class TradeMarketSearchResultScreen extends WynntilsContainerScreen<ChestMenu> implements WrappedScreen {
    // Constants
    private static final CustomColor LABEL_COLOR = CustomColor.fromInt(0x404040);
    private static final ResourceLocation CONTAINER_BACKGROUND =
            new ResourceLocation("textures/gui/container/generic_54.png");
    private static final int SCROLL_AREA_HEIGHT = 110;
    private static final int ITEMS_PER_PAGE = 54;

    // Info
    private final TradeMarketSearchResultParent parent;
    private final WrappedScreenInfo wrappedScreenInfo;

    // Widgets
    private ItemSearchWidget itemSearchWidget;

    // State
    private Component currentState = Component.empty();

    // Scrolling
    private int scrollOffset = 0;
    private boolean holdingScrollbar = false;

    protected TradeMarketSearchResultScreen(WrappedScreenInfo wrappedScreenInfo, TradeMarketSearchResultParent parent) {
        super(ChestMenu.sixRows(999, McUtils.inventory()), McUtils.inventory(), Component.literal("Wrapped Screen"));

        // Make our screen display according to the number of items, at the correct y position
        this.imageHeight = 114 + this.getMenu().getRowCount() * 18;
        this.inventoryLabelY = this.imageHeight - 94;

        this.parent = parent;
        this.wrappedScreenInfo = wrappedScreenInfo;
    }

    @Override
    public WrappedScreenInfo getWrappedScreenInfo() {
        return wrappedScreenInfo;
    }

    @Override
    protected void doInit() {
        super.doInit();

        ItemSearchWidget oldWidget = itemSearchWidget;

        // This is screen.topPos and screen.leftPos, but they are not calculated yet when this is called
        int renderX = (width - imageWidth) / 2;
        int renderY = (height - imageHeight) / 2 - 22;

        itemSearchWidget = new ItemSearchWidget(renderX, renderY, 175, 20, q -> reloadElements(), (TextboxScreen) this);
        this.addRenderableWidget(itemSearchWidget);

        // Copy over the old widget's text
        if (oldWidget != null) {
            itemSearchWidget.setTextBoxInput(oldWidget.getTextBoxInput());
        }

        WynntilsButton helperButton = new ItemSearchHelperWidget(
                renderX + 160,
                renderY + 4,
                (int) (Texture.INFO.width() / 2f),
                (int) (Texture.INFO.height() / 2f),
                Texture.INFO,
                a -> {},
                true);
        this.addRenderableWidget(helperButton);

        WynntilsButton backButton = new BasicTexturedButton(
                renderX - Texture.CONTAINER_SIDEBAR.width() / 2 - 2,
                renderY + Texture.CONTAINER_SIDEBAR.height(),
                Texture.ARROW_LEFT_ICON.width(),
                Texture.ARROW_LEFT_ICON.height(),
                Texture.ARROW_LEFT_ICON,
                (button) -> parent.goBackToSearch(),
                List.of(Component.translatable("screens.wynntils.tradeMarketSearchResult.backToSearch")));
        this.addRenderableWidget(backButton);

        WynntilsButton loadMoreButton = new BasicTexturedButton(
                renderX - Texture.CONTAINER_SIDEBAR.width() / 2 - 2,
                renderY + Texture.CONTAINER_SIDEBAR.height() - 20,
                Texture.SMALL_ADD_ICON.width(),
                Texture.SMALL_ADD_ICON.height(),
                Texture.SMALL_ADD_ICON,
                (button) -> parent.loadNextPageBatch(),
                List.of(Component.translatable(
                        "screens.wynntils.tradeMarketSearchResult.loadNextBatch", parent.getPageLoadBatchSize())));
        this.addRenderableWidget(loadMoreButton);
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        updateItems();

        renderables.forEach(c -> c.render(poseStack, mouseX, mouseY, partialTick));

        super.doRender(poseStack, mouseX, mouseY, partialTick);
        renderScrollButton(poseStack);

        // Render item tooltip
        super.renderTooltip(poseStack, mouseX, mouseY);

        // Render tooltip for hovered widget
        for (GuiEventListener child : children()) {
            if (child instanceof TooltipProvider tooltipProvider && child.isMouseOver(mouseX, mouseY)) {
                this.renderTooltip(poseStack, tooltipProvider.getTooltipLines(), Optional.empty(), mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(
                poseStack, this.currentState, (float) this.titleLabelX, (float) this.titleLabelY, LABEL_COLOR.asInt());
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Container
        RenderUtils.drawTexturedRect(
                poseStack, CONTAINER_BACKGROUND, x, y, this.imageWidth, this.menu.getRowCount() * 18 + 17, 256, 256);

        // Inventory
        RenderUtils.drawTexturedRect(
                poseStack,
                CONTAINER_BACKGROUND,
                x,
                y + this.menu.getRowCount() * 18 + 17,
                0,
                this.imageWidth,
                96,
                0,
                126,
                this.imageWidth,
                96,
                256,
                256);

        // Scrollbar
        RenderUtils.drawTexturedRect(poseStack, Texture.SCROLLBAR_BACKGROUND, x + this.imageWidth - 7, y);

        // Sidebar
        RenderUtils.drawTexturedRect(
                poseStack, Texture.CONTAINER_SIDEBAR, x - Texture.CONTAINER_SIDEBAR.width() + 7, y);
    }

    private void renderScrollButton(PoseStack poseStack) {
        float renderX =
                (this.width - this.imageWidth) / 2 + this.imageWidth + Texture.SCROLLBAR_BACKGROUND.width() / 2 - 14;
        float renderY = (this.height - this.imageHeight) / 2
                + Texture.SCROLLBAR_BUTTON.height() / 2
                + MathUtils.map(scrollOffset, 0, getMaxScrollOffset(), 0, SCROLL_AREA_HEIGHT);

        RenderUtils.drawTexturedRect(poseStack, Texture.SCROLLBAR_BUTTON, renderX, renderY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float scrollBarRenderX =
                (this.width - this.imageWidth) / 2 + this.imageWidth + Texture.SCROLLBAR_BACKGROUND.width() / 2 - 14;
        float scrollBarRenderY = (this.height - this.imageHeight) / 2
                + Texture.SCROLLBAR_BUTTON.height() / 2
                + MathUtils.map(scrollOffset, 0, getMaxScrollOffset(), 0, SCROLL_AREA_HEIGHT);

        if (mouseX >= scrollBarRenderX
                && mouseX <= scrollBarRenderX + Texture.SCROLLBAR_BUTTON.width()
                && mouseY >= scrollBarRenderY
                && mouseY <= scrollBarRenderY + Texture.SCROLLBAR_BUTTON.height()) {
            holdingScrollbar = true;
            return true;
        }

        if (hoveredSlot != null) {
            parent.clickOnItem(hoveredSlot.getItem());
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!holdingScrollbar) return false;

        int renderY = (this.height - this.imageHeight) / 2;
        int scrollAreaStartY = renderY + 14;

        int newValue = (int) MathUtils.map(
                (float) mouseY, scrollAreaStartY, scrollAreaStartY + SCROLL_AREA_HEIGHT, 0, getMaxScrollOffset());

        scroll(newValue - scrollOffset);

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        holdingScrollbar = false;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Signum so we only scroll 1 item at a time
        double scrollValue = -Math.signum(delta);
        scroll((int) scrollValue);

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void scroll(int delta) {
        int maxValue = getMaxScrollOffset();

        scrollOffset = MathUtils.clamp(scrollOffset + delta, 0, maxValue);
    }

    private int getMaxScrollOffset() {
        int maxItemOffset = Math.max(0, parent.getFilteredItems().size() - 54);
        int maxValue = maxItemOffset / 9 + (maxItemOffset % 9 > 0 ? 1 : 0);
        return maxValue;
    }

    private void updateItems() {
        List<ItemStack> filteredItems = parent.getFilteredItems();

        // Reset all items
        for (int i = 0; i < 54; i++) {
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

    protected ItemSearchQuery getSearchQuery() {
        return itemSearchWidget.getSearchQuery();
    }

    private void reloadElements() {
        parent.updateDisplayItems(itemSearchWidget.getSearchQuery());
        scrollOffset = 0;
    }
}
