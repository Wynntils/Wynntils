/*
 * Copyright © Wynntils 2023-2024.
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
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.containers.containers.TradeMarketSellContainer;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.TRADEMARKET)
public class TradeMarketBulkSellFeature extends Feature {
    private static final Pattern ITEM_NAME_PATTERN = Pattern.compile("(.+)À");
    private static final String SOLD_ITEM_SLOT = "Empty Item Slot";
    private static final String CLICK_TO_SET_AMOUNT = "Set Amount";
    private static final String TYPE_SELL_AMOUNT =
            "\uDAFF\uDFFC\uE001\uDB00\uDC06 Type the amount you wish to sell or ";

    private static final int SELLABLE_ITEM_SLOT = 22;
    private static final int AMOUNT_ITEM_SLOT = 31;

    @Persisted
    public final Config<Integer> bulkSell1Amount = new Config<>(64);

    @Persisted
    public final Config<Integer> bulkSell2Amount = new Config<>(0);

    @Persisted
    public final Config<Integer> bulkSell3Amount = new Config<>(0);

    private boolean sendAmountMessage = false;
    private int amountToSend = 0;

    @SubscribeEvent
    public void onSellDialogueUpdated(ContainerSetSlotEvent.Pre e) {
        if (!(McUtils.mc().screen instanceof ContainerScreen containerScreen)) return;

        if (!(Models.Container.getCurrentContainer() instanceof TradeMarketSellContainer)) return;

        StyledText amountItemName = StyledText.fromComponent(
                containerScreen.getMenu().getSlot(AMOUNT_ITEM_SLOT).getItem().getHoverName());
        if (!amountItemName.equalsString(CLICK_TO_SET_AMOUNT, PartStyle.StyleType.NONE)) return;

        String soldItemName = getSoldItemName(containerScreen);

        removeSellButtons(containerScreen);

        if (soldItemName == null) return;

        addSellButtons(containerScreen, soldItemName);
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent e) {
        if (!sendAmountMessage) return;
        if (!e.getOriginalStyledText().contains(TYPE_SELL_AMOUNT)) return;

        WynntilsMod.info("Trying to bulk sell " + amountToSend + " items");

        McUtils.sendChat(String.valueOf(amountToSend));

        sendAmountMessage = false;
    }

    private String getSoldItemName(MenuAccess<ChestMenu> cs) {
        ItemStack itemStack = cs.getMenu().getSlot(SELLABLE_ITEM_SLOT).getItem();
        if (itemStack == ItemStack.EMPTY) return null;

        StyledText itemStackName = StyledText.fromComponent(itemStack.getHoverName());
        if (itemStackName.getString(PartStyle.StyleType.NONE).equals(SOLD_ITEM_SLOT)) return null;

        Matcher m = itemStackName.getMatcher(ITEM_NAME_PATTERN);
        if (!m.matches()) return null;

        return m.group(1);
    }

    private int getAmountInInventory(String name) {
        int amount = 0;

        for (ItemStack itemStack : McUtils.inventory().items) {
            StyledText itemName = StyledText.fromComponent(itemStack.getHoverName())
                    .getNormalized()
                    .trim();
            if (itemName.getString().endsWith(name)) {
                amount += itemStack.getCount();
            }
        }

        return amount;
    }

    private void addSellButtons(ContainerScreen containerScreen, String soldItemName) {
        containerScreen.addRenderableWidget(new SellButton(
                containerScreen.leftPos - SellButton.BUTTON_WIDTH - 1,
                containerScreen.topPos + 30,
                () -> getAmountInInventory(soldItemName),
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
