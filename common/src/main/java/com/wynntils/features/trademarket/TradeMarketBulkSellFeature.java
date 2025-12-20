/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.trademarket;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.models.containers.containers.trademarket.TradeMarketSellContainer;
import com.wynntils.models.trademarket.event.TradeMarketChatInputEvent;
import com.wynntils.models.trademarket.event.TradeMarketSellDialogueUpdatedEvent;
import com.wynntils.models.trademarket.type.TradeMarketState;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.TRADEMARKET)
public class TradeMarketBulkSellFeature extends Feature {
    private static final int AMOUNT_ITEM_SLOT = 31;

    @Persisted
    private final Config<Integer> bulkSell1Amount = new Config<>(64);

    @Persisted
    private final Config<Integer> bulkSell2Amount = new Config<>(0);

    @Persisted
    private final Config<Integer> bulkSell3Amount = new Config<>(0);

    private boolean sendAmountMessage = false;
    private int amountToSend = 0;

    public TradeMarketBulkSellFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(
                        ConfigProfile.NEW_PLAYER, ConfigProfile.LITE, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onSellDialogueUpdated(TradeMarketSellDialogueUpdatedEvent e) {
        if (!(McUtils.screen() instanceof ContainerScreen containerScreen)) return;
        if (!(Models.Container.getCurrentContainer() instanceof TradeMarketSellContainer)) return;

        String soldItemName = Models.TradeMarket.getSoldItemName();
        removeSellButtons(containerScreen);
        if (soldItemName == null) return;
        addSellButtons(containerScreen, soldItemName);
    }

    @SubscribeEvent
    public void onTradeMarketChatInput(TradeMarketChatInputEvent e) {
        if (e.getState() != TradeMarketState.AMOUNT_CHAT_INPUT) return;
        if (!sendAmountMessage) return;

        WynntilsMod.info("Trying to bulk sell " + amountToSend + " items");
        e.setResponse(String.valueOf(amountToSend));
        sendAmountMessage = false;
    }

    private void addSellButtons(ContainerScreen containerScreen, String soldItemName) {
        containerScreen.addRenderableWidget(new SellButton(
                containerScreen.leftPos - SellButton.BUTTON_WIDTH - 1,
                containerScreen.topPos + 30,
                () -> Models.Inventory.getAmountInInventory(soldItemName),
                true));

        if (bulkSell1Amount.get() > 0) {
            containerScreen.addRenderableWidget(new SellButton(
                    containerScreen.leftPos - SellButton.BUTTON_WIDTH - 1,
                    containerScreen.topPos + 51,
                    bulkSell1Amount::get,
                    false));
        }
        if (bulkSell2Amount.get() > 0) {
            containerScreen.addRenderableWidget(new SellButton(
                    containerScreen.leftPos - SellButton.BUTTON_WIDTH - 1,
                    containerScreen.topPos + 72,
                    bulkSell2Amount::get,
                    false));
        }
        if (bulkSell3Amount.get() > 0) {
            containerScreen.addRenderableWidget(new SellButton(
                    containerScreen.leftPos - SellButton.BUTTON_WIDTH - 1,
                    containerScreen.topPos + 103,
                    bulkSell3Amount::get,
                    false));
        }
    }

    private void removeSellButtons(ContainerScreen containerScreen) {
        containerScreen.children.stream()
                .filter(child -> child instanceof SellButton)
                .toList()
                .forEach(containerScreen::removeWidget);
    }

    private final class SellButton extends WynntilsButton {
        private static final int BUTTON_WIDTH = 60;
        private static final int BUTTON_HEIGHT = 20;

        private final Supplier<Integer> amountSupplier;

        private SellButton(int x, int y, Supplier<Integer> amountSupplier, boolean isAll) {
            super(
                    x,
                    y,
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT,
                    isAll
                            ? Component.translatable("feature.wynntils.tradeMarketBulkSell.sellAll")
                            : Component.translatable(
                                    "feature.wynntils.tradeMarketBulkSell.sell", amountSupplier.get()));

            this.amountSupplier = amountSupplier;
        }

        @Override
        public void onPress() {
            amountToSend = amountSupplier.get();
            sendAmountMessage = true;

            ContainerUtils.clickOnSlot(
                    AMOUNT_ITEM_SLOT,
                    McUtils.containerMenu().containerId,
                    0,
                    McUtils.containerMenu().getItems());
        }
    }
}
