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
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.containers.containers.trademarket.TradeMarketSellContainer;
import com.wynntils.models.trademarket.type.TradeMarketState;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.TRADEMARKET)
public class TradeMarketPriceMatchFeature extends Feature {
    private static final StyledText CLICK_TO_SET_PRICE = StyledText.fromString("§a§lSet Price");

    // Test in TradeMarketPriceMatchFeature_HIGHEST_BUY_PATTERN
    private static final Pattern HIGHEST_BUY_PATTERN = Pattern.compile("§7Highest Buy Offer: §f([\\d,]+) §8\\(.+\\)");
    // Test in TradeMarketPriceMatchFeature_LOWEST_SELL_PATTERN
    private static final Pattern LOWEST_SELL_PATTERN = Pattern.compile("§7Cheapest Sell Offer: §f([\\d,]+) §8\\(.+\\)");

    private static final int PRICE_SET_ITEM_SLOT = 28;
    private static final int PRICE_INFO_ITEM_SLOT = 51;
    private static final Component SILVERBULL_STAR = Component.literal(" ✮").withStyle(ChatFormatting.AQUA);

    @Persisted
    public final Config<Integer> undercutBy = new Config<>(0);

    private boolean sendPriceMessage = false;
    private long priceToSend = 0;

    @SubscribeEvent
    public void onSellDialogueUpdated(ContainerSetSlotEvent.Post e) {
        if (!(McUtils.mc().screen instanceof ContainerScreen containerScreen)) return;

        if (!(Models.Container.getCurrentContainer() instanceof TradeMarketSellContainer)) return;

        StyledText amountItemName = StyledText.fromComponent(
                containerScreen.getMenu().getSlot(PRICE_SET_ITEM_SLOT).getItem().getHoverName());

        if (!amountItemName.equals(CLICK_TO_SET_PRICE)) return;

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

    private Pair<Integer, Integer> getBuySellOffers(MenuAccess<ChestMenu> containerScreen) {
        ItemStack priceInfoItem =
                containerScreen.getMenu().getSlot(PRICE_INFO_ITEM_SLOT).getItem();

        Matcher highestBuyMatcher = LoreUtils.matchLoreLine(priceInfoItem, 6, HIGHEST_BUY_PATTERN);
        Integer highestBuy = highestBuyMatcher.matches()
                ? Integer.parseInt(highestBuyMatcher.group(1).replace(",", "")) - undercutBy.get()
                : null;
        Matcher lowestSellMatcher = LoreUtils.matchLoreLine(priceInfoItem, 6, LOWEST_SELL_PATTERN);
        Integer lowestSell = lowestSellMatcher.matches()
                ? Integer.parseInt(lowestSellMatcher.group(1).replace(",", "")) - undercutBy.get()
                : null;

        return Pair.of(highestBuy, lowestSell);
    }

    private void addPriceButtons(ContainerScreen containerScreen) {
        Pair<Integer, Integer> buySellOffers = getBuySellOffers(containerScreen);

        int rightPos = containerScreen.leftPos + containerScreen.imageWidth + 1;

        if (buySellOffers.a() != null) {
            int untaxedA = (int) Math.round(buySellOffers.a() / Models.Emerald.getTaxAmount());
            MutableComponent buttonTooltip = undercutBy.get() == 0
                    ? Component.translatable("feature.wynntils.tradeMarketPriceMatch.highestBuyOfferMatchesTooltip")
                    : Component.translatable(
                            "feature.wynntils.tradeMarketPriceMatch.highestBuyOfferUndercutTooltip", undercutBy.get());
            containerScreen.addRenderableWidget(new PriceButton(
                    rightPos,
                    containerScreen.topPos + 30,
                    untaxedA,
                    Component.translatable("feature.wynntils.tradeMarketPriceMatch.highestBuyOffer"),
                    buttonTooltip
                            .append(Component.literal("\n\n"))
                            .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.youReceive")
                                    .withStyle(ChatFormatting.GREEN))
                            .append(Component.literal(Models.Emerald.getFormattedString(untaxedA, false))
                                    .withStyle(ChatFormatting.GRAY)
                                    .append(
                                            Models.Character.isSilverbullSubscriber()
                                                    ? SILVERBULL_STAR
                                                    : Component.empty()))
                            .append(Component.literal("\n"))
                            .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.totalPrice")
                                    .withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(Models.Emerald.getFormattedString(buySellOffers.a(), false))
                                    .withStyle(ChatFormatting.GRAY))));
        }

        if (buySellOffers.b() != null) {
            int untaxedB = (int) Math.round(buySellOffers.b() / Models.Emerald.getTaxAmount());
            MutableComponent buttonTooltip = undercutBy.get() == 0
                    ? Component.translatable("feature.wynntils.tradeMarketPriceMatch.lowestSellOfferMatchesTooltip")
                    : Component.translatable(
                            "feature.wynntils.tradeMarketPriceMatch.lowestSellOfferUndercutTooltip", undercutBy.get());
            containerScreen.addRenderableWidget(new PriceButton(
                    rightPos,
                    containerScreen.topPos + 51,
                    untaxedB,
                    Component.translatable("feature.wynntils.tradeMarketPriceMatch.lowestSellOffer"),
                    buttonTooltip
                            .append(Component.literal("\n\n"))
                            .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.youReceive")
                                    .withStyle(ChatFormatting.GREEN))
                            .append(Component.literal(Models.Emerald.getFormattedString(untaxedB, false))
                                    .withStyle(ChatFormatting.GRAY)
                                    .append(
                                            Models.Character.isSilverbullSubscriber()
                                                    ? SILVERBULL_STAR
                                                    : Component.empty()))
                            .append(Component.literal("\n"))
                            .append(Component.translatable("feature.wynntils.tradeMarketPriceMatch.totalPrice")
                                    .withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(Models.Emerald.getFormattedString(buySellOffers.b(), false))
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
                    PRICE_SET_ITEM_SLOT,
                    McUtils.containerMenu().containerId,
                    0,
                    McUtils.containerMenu().getItems());
        }
    }
}
