/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class TradeMarketBulkSellFeature extends Feature {

    @RegisterConfig
    public Config<Integer> bulkSell1Amount = new Config<>(64);

    @RegisterConfig
    public Config<Integer> bulkSell2Amount = new Config<>(0);

    @RegisterConfig
    public Config<Integer> bulkSell3Amount = new Config<>(0);

    private static final Pattern ITEM_NAME_PATTERN =
            Pattern.compile("§6Selling §f(\\d+|\\d+,\\d+) ([^À]*)À*§6 for §f[\\d,]*§7² Each");
    private static final String SELL_DIALOGUE_TITLE = "What would you like to sell?";
    private static final int SELLABLE_ITEM_SLOT = 10;
    private static final int AMOUNT_ITEM_SLOT = 11;
    private static final List<SellButton> sellButtons = new ArrayList<>();
    private static final int BUTTON_WIDTH = 60;

    private boolean shouldSend = false;
    private int amountToSend = 0;

    @SubscribeEvent
    public void onSellDialogueOpened(ScreenOpenedEvent e) {
        if (!(e.getScreen() instanceof ContainerScreen cs)) return;
        if (!ComponentUtils.getUnformatted(cs.getTitle()).equals(SELL_DIALOGUE_TITLE)) return;

        sellButtons.clear();
        sellButtons.add(new SellButton(cs.leftPos - BUTTON_WIDTH, cs.topPos, 0, true));
        if (bulkSell1Amount.get() > 0) {
            sellButtons.add(new SellButton(cs.leftPos - BUTTON_WIDTH, cs.topPos + 21, bulkSell1Amount.get(), false));
        }
        if (bulkSell2Amount.get() > 0) {
            sellButtons.add(new SellButton(cs.leftPos - BUTTON_WIDTH, cs.topPos + 42, bulkSell2Amount.get(), false));
        }
        if (bulkSell3Amount.get() > 0) {
            sellButtons.add(new SellButton(cs.leftPos - BUTTON_WIDTH, cs.topPos + 63, bulkSell3Amount.get(), false));
        }
        sellButtons.forEach(b -> b.active = false);
        sellButtons.forEach(cs::addRenderableWidget);
    }

    @SubscribeEvent
    public void onSellDialogueUpdated(ContainerSetSlotEvent e) {
        if (!(McUtils.mc().screen instanceof ContainerScreen cs)) return;
        if (!ComponentUtils.getUnformatted(cs.getTitle()).equals(SELL_DIALOGUE_TITLE)) return;

        String itemName = getItemName(cs);
        if (itemName == null) {
            sellButtons.forEach(b -> b.active = false);
            return;
        }

        sellButtons.get(0).setAmount(getAmount(itemName));
        sellButtons.forEach(b -> b.active = true);
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent e) {
        if (!shouldSend) return;
        if (!ComponentUtils.getUnformatted(e.getMessage())
                .contains("Type the amount you wish to sell or type 'cancel' to cancel:")) return;

        WynntilsMod.info("Trying to bulk sell " + amountToSend + " items");
        McUtils.mc().getConnection().sendChat(String.valueOf(amountToSend));
        shouldSend = false;
    }

    private String getItemName(MenuAccess<ChestMenu> cs) {
        ItemStack is = cs.getMenu().getSlot(SELLABLE_ITEM_SLOT).getItem();
        if (is == ItemStack.EMPTY) return null;
        if (is.getHoverName().toString().contains("Click an Item to sell")) return null;
        Matcher m = ITEM_NAME_PATTERN.matcher(ComponentUtils.getCoded(is.getHoverName()));

        if (!m.matches()) return null;
        return m.group(2);
    }

    private int getAmount(String name) {
        int amount = 0;
        for (ItemStack is : McUtils.inventory().items) {
            if (is.getHoverName().getString().trim().equals(name)) {
                amount += is.getCount();
            }
        }
        return amount;
    }

    private final class SellButton extends WynntilsButton {

        private int amount;

        private SellButton(int x, int y, int amount, boolean isAll) {
            super(
                    x,
                    y,
                    BUTTON_WIDTH,
                    20,
                    isAll
                            ? Component.translatable("feature.wynntils.tradeMarketBulkSell.sellAll")
                            : Component.translatable("feature.wynntils.tradeMarketBulkSell.sell", amount));
            this.amount = amount;
        }

        @Override
        public void onPress() {
            shouldSend = true;
            amountToSend = amount;
            ContainerUtils.clickOnSlot(
                    AMOUNT_ITEM_SLOT,
                    McUtils.containerMenu().containerId,
                    0,
                    McUtils.containerMenu().getItems());
        }

        private void setAmount(int amount) {
            this.amount = amount;
        }
    }
}
