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
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.extension.ScreenExtension;
import com.wynntils.models.containers.type.SearchableContainerProperty;
import com.wynntils.models.containers.type.wynncontainers.AccountBankContainer;
import com.wynntils.models.containers.type.wynncontainers.BlockBankContainer;
import com.wynntils.models.containers.type.wynncontainers.BookshelfContainer;
import com.wynntils.models.containers.type.wynncontainers.CharacterBankContainer;
import com.wynntils.models.containers.type.wynncontainers.ContentBookContainer;
import com.wynntils.models.containers.type.wynncontainers.GuildBankContainer;
import com.wynntils.models.containers.type.wynncontainers.GuildMemberListContainer;
import com.wynntils.models.containers.type.wynncontainers.GuildTerritoriesContainer;
import com.wynntils.models.containers.type.wynncontainers.HousingJukeboxContainer;
import com.wynntils.models.containers.type.wynncontainers.HousingListContainer;
import com.wynntils.models.containers.type.wynncontainers.JukeboxContainer;
import com.wynntils.models.containers.type.wynncontainers.MiscBucketContainer;
import com.wynntils.models.containers.type.wynncontainers.PetMenuContainer;
import com.wynntils.models.containers.type.wynncontainers.ScrapMenuContainer;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.screens.base.widgets.ItemSearchWidget;
import com.wynntils.screens.base.widgets.SearchWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    private final Map<Class<? extends SearchableContainerProperty>, Supplier<Boolean>> searchableContainerMap =
            Map.ofEntries(
                    Map.entry(AccountBankContainer.class, filterInBank::get),
                    Map.entry(BlockBankContainer.class, filterInBlockBank::get),
                    Map.entry(BookshelfContainer.class, filterInBookshelf::get),
                    Map.entry(CharacterBankContainer.class, filterInBank::get),
                    Map.entry(ContentBookContainer.class, filterInContentBook::get),
                    Map.entry(GuildBankContainer.class, filterInGuildBank::get),
                    Map.entry(GuildMemberListContainer.class, filterInGuildMemberList::get),
                    Map.entry(GuildTerritoriesContainer.class, filterInGuildTerritories::get),
                    Map.entry(HousingJukeboxContainer.class, filterInHousingJukebox::get),
                    Map.entry(HousingListContainer.class, filterInHousingList::get),
                    Map.entry(JukeboxContainer.class, filterInJukebox::get),
                    Map.entry(MiscBucketContainer.class, filterInMiscBucket::get),
                    Map.entry(PetMenuContainer.class, filterInPetMenu::get),
                    Map.entry(ScrapMenuContainer.class, filterInScrapMenu::get));

    // If the guild bank has lots of custom (crafted) items, it can take multiple packets and a decent amount of time
    // for Wynn to send us the entire updated inventory. During this, the inventory will be in a weird state where
    // some items are updated and some are not. We will assume that after SEARCH_DELAY_MS milliseconds, the inventory
    // is fully updated.
    private static final int GUILD_BANK_SEARCH_DELAY = 500;
    private long guildBankLastSearch = 0;

    private SearchWidget lastSearchWidget;
    private SearchableContainerProperty currentContainer;
    private boolean autoSearching = false;
    private ItemSearchQuery lastSearchQuery;

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;
        if (!(screen.getMenu() instanceof ChestMenu)) return;

        // This is screen.topPos and screen.leftPos, but they are not calculated yet when this is called
        int renderX = (screen.width - screen.imageWidth) / 2;
        int renderY = (screen.height - screen.imageHeight) / 2;

        currentContainer = getCurrentSearchableContainer();
        if (currentContainer == null) return;

        addWidgets(((AbstractContainerScreen<ChestMenu>) screen), renderX, renderY);
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
        if (currentContainer == null) return;
        forceUpdateSearch();

        if (autoSearching && McUtils.mc().screen instanceof AbstractContainerScreen<?> abstractContainerScreen) {
            tryAutoSearch(abstractContainerScreen);
        }
    }

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent.Pre event) {
        if (currentContainer == null) return;
        forceUpdateSearch();
    }

    @SubscribeEvent
    public void onContainerClose(ContainerCloseEvent.Post event) {
        lastSearchWidget = null;
        lastSearchQuery = null;
        currentContainer = null;
        autoSearching = false;
        guildBankLastSearch = 0;
    }

    @SubscribeEvent
    public void onInventoryKeyPress(InventoryKeyPressEvent event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_ENTER) return;
        if (lastSearchWidget == null
                || currentContainer == null
                || currentContainer.getNextItemSlot() == -1
                || !(McUtils.mc().screen instanceof AbstractContainerScreen<?> abstractContainerScreen)
                || !(abstractContainerScreen.getMenu() instanceof ChestMenu chestMenu)) return;

        ScreenExtension screen = (ScreenExtension) abstractContainerScreen;
        if (screen.getFocusedTextInput() != lastSearchWidget) return;

        autoSearching = true;
        if (currentContainer.supportsAdvancedSearch()) {
            matchItemsAdvanced(lastSearchQuery, chestMenu);
        } else {
            matchItemsBasic(lastSearchWidget.getTextBoxInput(), chestMenu);
        }

        tryAutoSearch(abstractContainerScreen);
    }

    private void tryAutoSearch(AbstractContainerScreen<?> abstractContainerScreen) {
        if (!autoSearching) return;
        if (currentContainer instanceof GuildBankContainer) {
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
                .get(currentContainer.getNextItemSlot())
                .getHoverName());

        if (!name.matches(currentContainer.getNextItemPattern())) {
            autoSearching = false;
            return;
        }

        ContainerUtils.clickOnSlot(
                currentContainer.getNextItemSlot(),
                abstractContainerScreen.getMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                abstractContainerScreen.getMenu().getItems());
    }

    private SearchableContainerProperty getCurrentSearchableContainer() {
        if (Models.Container.getCurrentContainer() instanceof SearchableContainerProperty searchableContainer) {
            for (Map.Entry<Class<? extends SearchableContainerProperty>, Supplier<Boolean>> entry :
                    searchableContainerMap.entrySet()) {
                if (entry.getKey().isInstance(searchableContainer)
                        && entry.getValue().get()) {
                    return searchableContainer;
                }
            }
        }

        return null;
    }

    private void addWidgets(AbstractContainerScreen<ChestMenu> screen, int renderX, int renderY) {
        if (currentContainer.supportsAdvancedSearch()) {
            ItemSearchWidget searchWidget = new ItemSearchWidget(
                    renderX + screen.imageWidth - 175,
                    renderY - 20,
                    175,
                    20,
                    currentContainer.supportedProviderTypes(),
                    false,
                    query -> {
                        lastSearchQuery = query;
                        matchItemsAdvanced(lastSearchQuery, screen.getMenu());
                    },
                    (ScreenExtension) screen);

            if (lastSearchWidget != null) {
                searchWidget.setTextBoxInput(lastSearchWidget.getTextBoxInput());
            }

            lastSearchWidget = searchWidget;

            screen.addRenderableWidget(lastSearchWidget);
        } else {
            SearchWidget searchWidget = new SearchWidget(
                    renderX + screen.imageWidth - 175,
                    renderY - 20,
                    175,
                    20,
                    s -> matchItemsBasic(s, screen.getMenu()),
                    (ScreenExtension) screen);

            if (lastSearchWidget != null) {
                searchWidget.setTextBoxInput(lastSearchWidget.getTextBoxInput());
            }

            lastSearchWidget = searchWidget;

            screen.addRenderableWidget(lastSearchWidget);
        }
    }

    private void matchItemsAdvanced(ItemSearchQuery searchQuery, ChestMenu chestMenu) {
        if (searchQuery == null) return;

        Container container = chestMenu.getContainer();
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!currentContainer.getBounds().getSlots().contains(i)) continue;

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

    private void matchItemsBasic(String searchStr, ChestMenu chestMenu) {
        String search = searchStr.toLowerCase(Locale.ROOT);

        Container container = chestMenu.getContainer();
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!currentContainer.getBounds().getSlots().contains(i)) continue;

            ItemStack itemStack = container.getItem(i);

            Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
            if (wynnItemOpt.isEmpty()) continue;

            String name = StyledText.fromComponent(itemStack.getHoverName())
                    .getStringWithoutFormatting()
                    .toLowerCase(Locale.ROOT);
            boolean filtered = !search.isEmpty() && name.contains(search) && itemStack.getItem() != Items.AIR;

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
            if (currentContainer.supportsAdvancedSearch()) {
                matchItemsAdvanced(lastSearchQuery, chestMenu);
            } else {
                matchItemsBasic(lastSearchWidget.getTextBoxInput(), chestMenu);
            }
        }
    }
}
