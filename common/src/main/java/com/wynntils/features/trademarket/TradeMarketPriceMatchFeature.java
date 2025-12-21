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
import com.wynntils.models.account.AccountModel;
import com.wynntils.models.containers.containers.trademarket.TradeMarketSellContainer;
import com.wynntils.models.trademarket.TradeMarketModel;
import com.wynntils.models.trademarket.event.TradeMarketChatInputEvent;
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

    public TradeMarketPriceMatchFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(
                        ConfigProfile.NEW_PLAYER, ConfigProfile.LITE, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onSellDialogueUpdated(TradeMarketSellDialogueUpdatedEvent e) {
        if (!(McUtils.screen() instanceof ContainerScreen containerScreen)) return;
        if (!(Models.Container.getCurrentContainer() instanceof TradeMarketSellContainer)) return;

        removePriceButtons(containerScreen);
        addPriceButtons(containerScreen);
    }

    @SubscribeEvent
    public void onTradeMarketChatInput(TradeMarketChatInputEvent e) {
        if (e.getState() != TradeMarketState.PRICE_CHAT_INPUT) return;
        if (!sendPriceMessage) return;

        WynntilsMod.info("Trying to set trade market price to " + priceToSend);
        e.setResponse(String.valueOf(priceToSend));
        e.cancelChat();
        sendPriceMessage = false;
    }

    private void addPriceButtons(ContainerScreen containerScreen) {
        TradeMarketPriceCheckInfo priceCheckInfo = Models.TradeMarket.getPriceCheckInfo();

        int rightPos = containerScreen.leftPos + containerScreen.imageWidth + 1;

        if (priceCheckInfo.bid() != -1) {
            // Do not undercut highest buy offer, just match it
            int taxedBid = priceCheckInfo.bid();
            int untaxedBid = Models.Emerald.getWithoutTax(taxedBid);

            MutableComponent buttonTooltip = Component.translatable(
                            "feature.wynntils.tradeMarketPriceMatch.highestBuyOfferMatchesTooltip")
                    .append(Component.literal("\n\n"))
                    .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.recommendedPrice")
                            .withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(getPriceComponent(priceCheckInfo.recommendedPrice()))
                    .append(Component.literal("\n"))
                    .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.youReceive")
                            .withStyle(ChatFormatting.GREEN))
                    .append(getPriceComponent(untaxedBid))
                    .append(Models.Account.isSilverbullSubscriber() ? AccountModel.SILVERBULL_STAR : Component.empty())
                    .append(Component.literal("\n"))
                    .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.totalPrice")
                            .withStyle(ChatFormatting.GOLD))
                    .append(getPriceComponent(taxedBid));

            PriceButton priceButton = new PriceButton(
                    rightPos,
                    containerScreen.topPos + 30,
                    untaxedBid,
                    Component.translatable("feature.wynntils.tradeMarketPriceMatch.highestBuyOffer"),
                    buttonTooltip);
            containerScreen.addRenderableWidget(priceButton);
        }

        if (priceCheckInfo.ask() != -1) {
            int lowestAsk = priceCheckInfo.ask();
            int taxedBid = (lowestAsk <= undercutBy.get()) ? 1 : lowestAsk - undercutBy.get();
            int untaxedBid = Models.Emerald.getWithoutTax(taxedBid);

            MutableComponent buttonTooltip = (undercutBy.get() == 0)
                    ? Component.translatable("feature.wynntils.tradeMarketPriceMatch.lowestSellOfferMatchesTooltip")
                    : Component.translatable(
                            "feature.wynntils.tradeMarketPriceMatch.lowestSellOfferUndercutTooltip", undercutBy.get());
            buttonTooltip
                    .append(Component.literal("\n\n"))
                    .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.recommendedPrice")
                            .withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(getPriceComponent(priceCheckInfo.recommendedPrice()))
                    .append(Component.literal("\n"))
                    .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.youReceive")
                            .withStyle(ChatFormatting.GREEN))
                    .append(getPriceComponent(untaxedBid))
                    .append(Models.Account.isSilverbullSubscriber() ? AccountModel.SILVERBULL_STAR : Component.empty())
                    .append(Component.literal("\n"))
                    .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.totalPrice")
                            .withStyle(ChatFormatting.GOLD))
                    .append(getPriceComponent(taxedBid));

            PriceButton priceButton = new PriceButton(
                    rightPos,
                    containerScreen.topPos + 51,
                    untaxedBid,
                    Component.translatable("feature.wynntils.tradeMarketPriceMatch.lowestSellOffer"),
                    buttonTooltip);
            containerScreen.addRenderableWidget(priceButton);
        }
    }

    private MutableComponent getPriceComponent(int price) {
        return Component.literal(Models.Emerald.getEmeraldCountString(price, false))
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(" (" + Models.Emerald.getFormattedString(price, false) + ")")
                        .withStyle(ChatFormatting.GRAY));
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
