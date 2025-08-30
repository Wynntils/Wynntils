/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.trademarket;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.models.account.AccountModel;
import com.wynntils.models.containers.containers.trademarket.TradeMarketSellContainer;
import com.wynntils.models.trademarket.TradeMarketModel;
import com.wynntils.models.trademarket.event.TradeMarketSellDialogueUpdatedEvent;
import com.wynntils.models.trademarket.type.TradeMarketPriceCheckInfo;
import com.wynntils.models.trademarket.type.TradeMarketState;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.TRADEMARKET)
public class TradeMarketPriceMatchFeature extends Feature {
    @Persisted
    private final Config<Integer> undercutBy = new Config<>(0);

    private boolean sendPriceMessage = false;
    private long priceToSend = 0;

    @SubscribeEvent
    public void onSellDialogueUpdated(TradeMarketSellDialogueUpdatedEvent e) {
        if (!(McUtils.mc().screen instanceof ContainerScreen containerScreen)) return;
        if (!(Models.Container.getCurrentContainer() instanceof TradeMarketSellContainer)) return;

        removePriceButtons(containerScreen);
        addPriceButtons(containerScreen);
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent e) {
        if (!sendPriceMessage) return;
        if (Models.TradeMarket.getTradeMarketState() != TradeMarketState.PRICE_CHAT_INPUT) return;

        WynntilsMod.info("Trying to set trade market price to " + priceToSend);

        McUtils.sendChat(String.valueOf(priceToSend));

        sendPriceMessage = false;
    }

    private void addPriceButtons(ContainerScreen containerScreen) {
        TradeMarketPriceCheckInfo priceCheckInfo = Models.TradeMarket.getPriceCheckInfo();

        int rightPos = containerScreen.leftPos + containerScreen.imageWidth + 1;

        if (priceCheckInfo.bid() != -1) {
            int untaxedBid = (int) Math.round(priceCheckInfo.bid() / Models.Emerald.getTaxAmount());
            MutableComponent buttonTooltip = undercutBy.get() == 0
                    ? Component.translatable("feature.wynntils.tradeMarketPriceMatch.highestBuyOfferMatchesTooltip")
                    : Component.translatable(
                            "feature.wynntils.tradeMarketPriceMatch.highestBuyOfferUndercutTooltip", undercutBy.get());
            containerScreen.addRenderableWidget(new PriceButton(
                    rightPos,
                    containerScreen.topPos + 30,
                    untaxedBid,
                    Component.translatable("feature.wynntils.tradeMarketPriceMatch.highestBuyOffer"),
                    buttonTooltip
                            .append(Component.literal("\n\n"))
                            .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.youReceive")
                                    .withStyle(ChatFormatting.GREEN))
                            .append(Component.literal(Models.Emerald.getFormattedString(untaxedBid, false))
                                    .withStyle(ChatFormatting.GRAY)
                                    .append(
                                            Models.Account.isSilverbullSubscriber()
                                                    ? AccountModel.SILVERBULL_STAR
                                                    : Component.empty()))
                            .append(Component.literal("\n"))
                            .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.totalPrice")
                                    .withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(Models.Emerald.getFormattedString(priceCheckInfo.bid(), false))
                                    .withStyle(ChatFormatting.GRAY))));
        }

        if (priceCheckInfo.ask() != -1) {
            int untaxedAsk = (int) Math.round(priceCheckInfo.ask() / Models.Emerald.getTaxAmount());
            MutableComponent buttonTooltip = undercutBy.get() == 0
                    ? Component.translatable("feature.wynntils.tradeMarketPriceMatch.lowestSellOfferMatchesTooltip")
                    : Component.translatable(
                            "feature.wynntils.tradeMarketPriceMatch.lowestSellOfferUndercutTooltip", undercutBy.get());
            containerScreen.addRenderableWidget(new PriceButton(
                    rightPos,
                    containerScreen.topPos + 51,
                    untaxedAsk,
                    Component.translatable("feature.wynntils.tradeMarketPriceMatch.lowestSellOffer"),
                    buttonTooltip
                            .append(Component.literal("\n\n"))
                            .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.youReceive")
                                    .withStyle(ChatFormatting.GREEN))
                            .append(Component.literal(Models.Emerald.getFormattedString(untaxedAsk, false))
                                    .withStyle(ChatFormatting.GRAY)
                                    .append(
                                            Models.Account.isSilverbullSubscriber()
                                                    ? AccountModel.SILVERBULL_STAR
                                                    : Component.empty()))
                            .append(Component.literal("\n"))
                            .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.totalPrice")
                                    .withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(Models.Emerald.getFormattedString(priceCheckInfo.ask(), false))
                                    .withStyle(ChatFormatting.GRAY))));
        }
    }

    private void removePriceButtons(ContainerScreen containerScreen) {
        containerScreen.children.stream()
                .filter(child -> child instanceof PriceButton)
                .toList()
                .forEach(containerScreen::removeWidget);
    }

    private final class PriceButton extends WynntilsButton {
        private static final int BUTTON_WIDTH = 100;
        private static final int BUTTON_HEIGHT = 20;

        private final int price;

        private PriceButton(int x, int y, int priceNoTax, Component name, Component hoverText) {
            super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, name);

            this.price = priceNoTax;

            this.setTooltip(Tooltip.create(hoverText));
        }

        @Override
        public void onPress() {
            priceToSend = this.price;
            sendPriceMessage = true;

            ContainerUtils.clickOnSlot(
                    TradeMarketModel.TM_SELL_PRICE_SLOT,
                    McUtils.containerMenu().containerId,
                    0,
                    McUtils.containerMenu().getItems());
        }
    }
}
