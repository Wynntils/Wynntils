/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.event.InventoryMouseClickedEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.extension.ScreenExtension;
import com.wynntils.models.containers.type.InteractiveContainerType;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.screens.base.widgets.ItemFilterUIButton;
import com.wynntils.screens.base.widgets.ItemSearchHelperWidget;
import com.wynntils.screens.base.widgets.ItemSearchWidget;
import com.wynntils.screens.base.widgets.SearchWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.Optional;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.INVENTORY)
public class ContainerSearchFeature extends Feature {
    @Persisted
    public final Config<Boolean> filterInBank = new Config<>(true);

    @Persisted
    public final Config<Boolean> filterInBlockBank = new Config<>(true);

    @Persisted
    public final Config<Boolean> filterInBookshelf = new Config<>(true);

    @Persisted
    public final Config<Boolean> filterInMiscBucket = new Config<>(true);

    @Persisted
    public final Config<Boolean> filterInGuildBank = new Config<>(true);

    @Persisted
    public final Config<Boolean> filterInGuildMemberList = new Config<>(true);

    @Persisted
    public final Config<Boolean> filterInScrapMenu = new Config<>(true);

    @Persisted
    public final Config<Boolean> filterInPetMenu = new Config<>(true);

    @Persisted
    public final Config<Boolean> filterInContentBook = new Config<>(true);

    @Persisted
    public final Config<Boolean> filterInGuildTerritories = new Config<>(true);

    @Persisted
    public final Config<Boolean> filterInHousingJukebox = new Config<>(true);

    @Persisted
    public final Config<Boolean> filterInHousingList = new Config<>(true);

    @Persisted
    public final Config<Boolean> filterInJukebox = new Config<>(true);

    @Persisted
    public final Config<CustomColor> highlightColor = new Config<>(CommonColors.MAGENTA);

    // If the guild bank has lots of custom (crafted) items, it can take multiple packets and a decent amount of time
    // for Wynn to send us the entire updated inventory. During this, the inventory will be in a weird state where
    // some items are updated and some are not. We will assume that after SEARCH_DELAY_MS milliseconds, the inventory
    // is fully updated.
    private static final int GUILD_BANK_SEARCH_DELAY = 500;
    private long guildBankLastSearch = 0;

    private SearchWidget lastSearchWidget;
    private ItemSearchHelperWidget lastItemSearchHelperWidget;
    private InteractiveContainerType currentInteractiveContainerType;
    private boolean autoSearching = false;
    private ItemSearchQuery lastSearchQuery;

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;
        if (!(screen.getMenu() instanceof ChestMenu)) return;

        // This is screen.topPos and screen.leftPos, but they are not calculated yet when this is called
        int renderX = (screen.width - screen.imageWidth) / 2;
        int renderY = (screen.height - screen.imageHeight) / 2;

        currentInteractiveContainerType = getCurrentInteractiveContainerType(screen);
        if (currentInteractiveContainerType == null) return;

        addWidgets(((AbstractContainerScreen<ChestMenu>) screen), renderX, renderY);
    }

    // This might not be needed in 1.20
    // Render the tooltip last
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onContainerRender(ContainerRenderEvent event) {
        if (lastItemSearchHelperWidget == null) return;

        if (lastItemSearchHelperWidget.isHovered()) {
            event.getGuiGraphics()
                    .renderComponentTooltip(
                            FontRenderer.getInstance().getFont(),
                            lastItemSearchHelperWidget.getTooltipLines(),
                            event.getMouseX(),
                            event.getMouseY());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        ItemStack itemStack = e.getSlot().getItem();
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return;

        Boolean result = wynnItemOpt.get().getData().get(WynnItemData.SEARCHED_KEY);
        if (result == null || !result) return;

        RenderUtils.drawArc(e.getPoseStack(), highlightColor.get(), e.getSlot().x, e.getSlot().y, 200, 1f, 6, 8);
    }

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent.Post event) {
        if (currentInteractiveContainerType == null) return;
        forceUpdateSearch();

        if (autoSearching && McUtils.mc().screen instanceof AbstractContainerScreen<?> abstractContainerScreen) {
            tryAutoSearch(abstractContainerScreen);
        }
    }

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent.Pre event) {
        if (currentInteractiveContainerType == null) return;
        forceUpdateSearch();
    }

    @SubscribeEvent
    public void onContainerClose(ContainerCloseEvent.Post event) {
        lastSearchWidget = null;
        lastSearchQuery = null;
        lastItemSearchHelperWidget = null;
        currentInteractiveContainerType = null;
        autoSearching = false;
        guildBankLastSearch = 0;
    }

    @SubscribeEvent
    public void onInventoryKeyPress(InventoryKeyPressEvent event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_ENTER) return;
        if (lastSearchWidget == null
                || currentInteractiveContainerType == null
                || currentInteractiveContainerType.getNextItemSlot() == -1
                || !(McUtils.mc().screen instanceof AbstractContainerScreen<?> abstractContainerScreen)
                || !(abstractContainerScreen.getMenu() instanceof ChestMenu chestMenu)) return;

        ScreenExtension screen = (ScreenExtension) abstractContainerScreen;
        if (screen.getFocusedTextInput() != lastSearchWidget) return;

        autoSearching = true;
        matchItems(lastSearchQuery, chestMenu);

        tryAutoSearch(abstractContainerScreen);
    }

    @SubscribeEvent
    public void onInventoryMouseClick(InventoryMouseClickedEvent event) {
        if (lastItemSearchHelperWidget == null) return;

        if (lastItemSearchHelperWidget.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
            return;
        }
    }

    private void tryAutoSearch(AbstractContainerScreen<?> abstractContainerScreen) {
        if (!autoSearching) return;
        if (currentInteractiveContainerType == InteractiveContainerType.GUILD_BANK) {
            long diff = System.currentTimeMillis() - guildBankLastSearch;
            if (diff < GUILD_BANK_SEARCH_DELAY) {
                Managers.TickScheduler.scheduleLater(
                        () -> tryAutoSearch(abstractContainerScreen), (int) (GUILD_BANK_SEARCH_DELAY - diff) / 50);
                return;
            }
            guildBankLastSearch = System.currentTimeMillis();
        }

        StyledText name = StyledText.fromComponent(abstractContainerScreen
                .getMenu()
                .getItems()
                .get(currentInteractiveContainerType.getNextItemSlot())
                .getHoverName());

        if (!name.matches(currentInteractiveContainerType.getNextItemPattern())) {
            autoSearching = false;
            return;
        }

        ContainerUtils.clickOnSlot(
                currentInteractiveContainerType.getNextItemSlot(),
                abstractContainerScreen.getMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                abstractContainerScreen.getMenu().getItems());
    }

    private InteractiveContainerType getCurrentInteractiveContainerType(Screen screen) {
        InteractiveContainerType containerType = null;

        for (InteractiveContainerType type : InteractiveContainerType.values()) {
            if (type.isScreen(screen)) {
                containerType = type;
            }
        }

        if (containerType == null || !containerType.isSearchable()) return null;

        return switch (containerType) {
            case ACCOUNT_BANK -> filterInBank.get() ? InteractiveContainerType.ACCOUNT_BANK : null;
            case BLOCK_BANK -> filterInBlockBank.get() ? InteractiveContainerType.BLOCK_BANK : null;
            case BOOKSHELF -> filterInBookshelf.get() ? InteractiveContainerType.BOOKSHELF : null;
            case CHARACTER_BANK -> filterInBank.get() ? InteractiveContainerType.CHARACTER_BANK : null;
            case CONTENT_BOOK -> filterInContentBook.get() ? InteractiveContainerType.CONTENT_BOOK : null;
            case GUILD_BANK -> filterInGuildBank.get() ? InteractiveContainerType.GUILD_BANK : null;
            case GUILD_MEMBER_LIST -> filterInGuildMemberList.get() ? InteractiveContainerType.GUILD_MEMBER_LIST : null;
            case GUILD_TERRITORIES -> filterInGuildTerritories.get()
                    ? InteractiveContainerType.GUILD_TERRITORIES
                    : null;
            case HOUSING_JUKEBOX -> filterInHousingJukebox.get() ? InteractiveContainerType.HOUSING_JUKEBOX : null;
            case HOUSING_LIST -> filterInHousingList.get() ? InteractiveContainerType.HOUSING_LIST : null;
            case JUKEBOX -> filterInJukebox.get() ? InteractiveContainerType.JUKEBOX : null;
            case MISC_BUCKET -> filterInMiscBucket.get() ? InteractiveContainerType.MISC_BUCKET : null;
            case PET_MENU -> filterInPetMenu.get() ? InteractiveContainerType.PET_MENU : null;
            case SCRAP_MENU -> filterInScrapMenu.get() ? InteractiveContainerType.SCRAP_MENU : null;
            case ABILITY_TREE, LOBBY, TRADE_MARKET_FILTERS, TRADE_MARKET_PRIMARY, TRADE_MARKET_SECONDARY -> null;
        };
    }

    private void addWidgets(AbstractContainerScreen<ChestMenu> screen, int renderX, int renderY) {
        ItemSearchWidget searchWidget = new ItemSearchWidget(
                renderX + screen.imageWidth - 175,
                renderY - 20,
                155,
                20,
                false,
                query -> {
                    lastSearchQuery = query;
                    matchItems(lastSearchQuery, screen.getMenu());
                },
                (ScreenExtension) screen);

        if (lastSearchWidget != null) {
            searchWidget.setTextBoxInput(lastSearchWidget.getTextBoxInput());
        }

        lastSearchWidget = searchWidget;

        screen.addRenderableWidget(lastSearchWidget);

        lastItemSearchHelperWidget = new ItemSearchHelperWidget(
                renderX + screen.imageWidth - 31,
                renderY - 14,
                Texture.INFO.width() / 3,
                Texture.INFO.height() / 3,
                Texture.INFO,
                true);

        screen.addRenderableWidget(lastItemSearchHelperWidget);

        screen.addRenderableWidget(
                new ItemFilterUIButton(renderX + 157, renderY - 20, lastSearchWidget, screen, false));
    }

    private void matchItems(ItemSearchQuery searchQuery, ChestMenu chestMenu) {
        if (searchQuery == null) return;

        Container container = chestMenu.getContainer();
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!currentInteractiveContainerType.getBounds().getSlots().contains(i)) continue;

            ItemStack itemStack = container.getItem(i);

            Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
            if (wynnItemOpt.isEmpty()) continue;

            boolean filtered = !searchQuery.isEmpty() && Services.ItemFilter.matches(searchQuery, itemStack);

            wynnItemOpt.get().getData().store(WynnItemData.SEARCHED_KEY, filtered);
            if (filtered) {
                autoSearching = false;
            }
        }
    }

    private void forceUpdateSearch() {
        Screen screen = McUtils.mc().screen;
        if (lastSearchWidget != null
                && screen instanceof AbstractContainerScreen<?> abstractContainerScreen
                && abstractContainerScreen.getMenu() instanceof ChestMenu chestMenu) {
            matchItems(lastSearchQuery, chestMenu);
        }
    }
}
