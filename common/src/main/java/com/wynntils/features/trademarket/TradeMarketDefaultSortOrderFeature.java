/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.trademarket;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.trademarket.event.TradeMarketStateEvent;
import com.wynntils.models.trademarket.type.TradeMarketState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.TRADEMARKET)
public class TradeMarketDefaultSortOrderFeature extends Feature {
    @Persisted
    private final Config<SortOrder> defaultSortOrder = new Config<>(SortOrder.MOST_RECENT);

    @Persisted
    private final Config<SortOrderChangeSpeed> sortOrderChangeSpeed = new Config<>(SortOrderChangeSpeed.BALANCED);

    @Persisted
    private final Config<Boolean> applySortOrderOnce = new Config<>(true);

    private static final int SORT_ORDER_SLOT = 52;

    private boolean appliedDefaultSortOrder = false;
    private int clickCountdown = 0;
    private boolean shouldRightClick = false;
    private SortOrder currentSortOrder = SortOrder.MOST_RECENT;

    public TradeMarketDefaultSortOrderFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.LITE, ConfigProfile.MINIMAL)
                .build());
    }

    @SubscribeEvent
    public void onTradeMarketState(TradeMarketStateEvent event) {
        TradeMarketState newState = event.getNewState();
        if (!(appliedDefaultSortOrder && applySortOrderOnce.get()) && newState == TradeMarketState.FILTERED_RESULTS) {
            appliedDefaultSortOrder = true;
            // Find the shortest path from current sort order to the one we want to apply
            // Math.abs(path) is path's lenght, Math.sign(path) is path's direction (-1 = right, 1 == left)
            final int path1 = defaultSortOrder.get().ordinal() - currentSortOrder.ordinal();
            final int path2 = path1 + SortOrder.VALUES.length * (path1 > 0 ? 1 : -1);
            if (Math.abs(path1) < Math.abs(path2)) {
                clickCountdown = Math.abs(path1);
                shouldRightClick = Mth.sign(path1) == -1;
            } else {
                clickCountdown = Math.abs(path2);
                shouldRightClick = Mth.sign(path2) == -1;
            }
            currentSortOrder = defaultSortOrder.get();
        } else if (newState == TradeMarketState.NOT_ACTIVE) {
            appliedDefaultSortOrder = false;
            clickCountdown = 0;
            shouldRightClick = false;
            currentSortOrder = SortOrder.MOST_RECENT;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (clickCountdown <= 0) return;
        if (McUtils.mc().level.getGameTime() % sortOrderChangeSpeed.get().ticksDelay != 0) return;

        ContainerUtils.clickOnSlot(
                SORT_ORDER_SLOT,
                McUtils.containerMenu().containerId,
                shouldRightClick ? GLFW.GLFW_MOUSE_BUTTON_RIGHT : GLFW.GLFW_MOUSE_BUTTON_LEFT,
                McUtils.containerMenu().getItems());

        clickCountdown -= 1;
    }

    @SubscribeEvent
    public void onContainerClick(ContainerClickEvent event) {
        if (Models.TradeMarket.getTradeMarketState() != TradeMarketState.FILTERED_RESULTS) return;
        if (event.getSlotNum() != SORT_ORDER_SLOT) return;

        final int mb = event.getMouseButton();
        if (mb == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            currentSortOrder = currentSortOrder.next();
        } else if (mb == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            currentSortOrder = currentSortOrder.prev();
        }
    }

    public enum SortOrder {
        MOST_RECENT,
        LEAST_RECENT,
        MOST_EXPENSIVE,
        LEAST_EXPENSIVE,
        HIGHEST_LEVEL_RANGE,
        LOWEST_LEVEL_RANGE;

        private static final SortOrder[] VALUES = SortOrder.values();

        public SortOrder prev() {
            return VALUES[Math.floorMod(ordinal() - 1, VALUES.length)];
        }

        public SortOrder next() {
            return VALUES[(ordinal() + 1) % VALUES.length];
        }
    }

    // Values taken from BulkBuyFeature
    public enum SortOrderChangeSpeed {
        FAST(4),
        BALANCED(5),
        SAFE(6),
        VERY_SAFE(8);

        public final int ticksDelay;

        SortOrderChangeSpeed(int ticksDelay) {
            this.ticksDelay = ticksDelay;
        }
    }
}
