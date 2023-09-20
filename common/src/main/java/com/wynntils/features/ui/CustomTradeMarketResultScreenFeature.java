/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.wrappedscreen.event.WrappedScreenOpenEvent;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.wrappedscreens.trademarket.TradeMarketSearchResultScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CustomTradeMarketResultScreenFeature extends Feature {
    private static final Pattern TRADE_MARKET_FILTER_SCREEN_TITLE_PATTERN =
            Pattern.compile("\\[Pg. \\d+\\] Filter Items");
    private static final String SEARCH_ITEM_TITLE = "§a§lSearch";

    @Persisted
    private final Config<ShiftBehavior> shiftBehaviorConfig = new Config<>(ShiftBehavior.DISABLED_IF_SHIFT_HELD);

    private boolean shiftClickedSearchItem = false;

    @SubscribeEvent
    public void onWrappedScreenOpen(WrappedScreenOpenEvent event) {
        if (event.getWrappedScreenClass() != TradeMarketSearchResultScreen.class) return;

        switch (shiftBehaviorConfig.get()) {
            case NONE -> {
                event.setOpenScreen(true);
            }
            case ENABLED_IF_SHIFT_HELD -> {
                if (shiftClickedSearchItem) {
                    event.setOpenScreen(true);
                }
            }
            case DISABLED_IF_SHIFT_HELD -> {
                if (!shiftClickedSearchItem) {
                    event.setOpenScreen(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onContainerClick(ContainerClickEvent event) {
        if (!StyledText.fromComponent(event.getItemStack().getHoverName()).equalsString(SEARCH_ITEM_TITLE)) return;

        shiftClickedSearchItem = KeyboardUtils.isShiftDown();
    }

    @SubscribeEvent
    public void onRenderTooltip(ItemTooltipRenderEvent.Pre event) {
        if (shiftBehaviorConfig.get() == ShiftBehavior.NONE) return;
        if (McUtils.mc().screen == null) return;

        if (!StyledText.fromComponent(McUtils.mc().screen.getTitle())
                .matches(TRADE_MARKET_FILTER_SCREEN_TITLE_PATTERN)) {
            return;
        }

        if (!StyledText.fromComponent(event.getItemStack().getHoverName()).equalsString(SEARCH_ITEM_TITLE)) return;

        ItemStack itemStack = event.getItemStack();

        List<Component> tooltips = new ArrayList<>(event.getTooltips());

        MutableComponent component =
                switch (shiftBehaviorConfig.get()) {
                    case NONE -> Component.empty();
                    case ENABLED_IF_SHIFT_HELD -> (Component.translatable(
                            "feature.wynntils.customTradeMarketResultScreen.shiftToEnable"));
                    case DISABLED_IF_SHIFT_HELD -> (Component.translatable(
                            "feature.wynntils.customTradeMarketResultScreen.shiftToDisable"));
                };

        tooltips.add(Component.empty());
        tooltips.add(component.withStyle(ChatFormatting.GREEN));

        event.setTooltips(tooltips);
    }

    private enum ShiftBehavior {
        NONE,
        ENABLED_IF_SHIFT_HELD,
        DISABLED_IF_SHIFT_HELD
    }
}
