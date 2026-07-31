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
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.trademarket.TradeMarketModel;
import com.wynntils.models.trademarket.event.TradeMarketStateEvent;
import com.wynntils.models.trademarket.type.TradeMarketSortOrder;
import com.wynntils.models.trademarket.type.TradeMarketState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.ActionSpeed;
import com.wynntils.utils.wynn.ContainerUtils;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.TRADEMARKET)
public class TradeMarketDefaultSortOrderFeature extends Feature {
    @Persisted
    private final Config<TradeMarketSortOrder> defaultSortOrder = new Config<>(TradeMarketSortOrder.MOST_RECENT);

    @Persisted
    private final Config<ActionSpeed> sortOrderChangeSpeed = new Config<>(ActionSpeed.BALANCED);

    @Persisted
    private final Config<Boolean> applySortOrderOnce = new Config<>(true);

    private int clickCountdown = 0;
    private boolean shouldRightClick = false;
    private boolean appliedDefaultSortOrder = false;

    public TradeMarketDefaultSortOrderFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.LITE, ConfigProfile.MINIMAL)
                .build());
    }

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        if (Models.TradeMarket.getTradeMarketState() != TradeMarketState.FILTERED_RESULTS) return;
        if (appliedDefaultSortOrder && applySortOrderOnce.get()) return;

        TradeMarketSortOrder currentSortOrder =
                Models.TradeMarket.getSortOrder(event.getItems().get(TradeMarketModel.SORT_ORDER_SLOT));

        // Find the shortest path from current sort order to the one we want to apply
        // Math.abs(path) is path's length, path < 0 -> right click else left click
        final int path1 = defaultSortOrder.get().ordinal() - currentSortOrder.ordinal();
        final int path2 = path1 + TradeMarketSortOrder.LENGTH * (path1 > 0 ? 1 : -1);
        if (Math.abs(path1) < Math.abs(path2)) {
            clickCountdown = Math.abs(path1);
            shouldRightClick = path1 < 0;
        } else {
            clickCountdown = Math.abs(path2);
            shouldRightClick = path2 < 0;
        }
        appliedDefaultSortOrder = true;
    }

    @SubscribeEvent
    public void onTradeMarketState(TradeMarketStateEvent event) {
        if (event.getNewState() == TradeMarketState.NOT_ACTIVE) {
            clickCountdown = 0;
            shouldRightClick = false;
            appliedDefaultSortOrder = false;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (clickCountdown <= 0) return;
        if (McUtils.mc().level.getGameTime() % sortOrderChangeSpeed.get().getTicksDelay() != 0) return;

        ContainerUtils.clickOnSlot(
                TradeMarketModel.SORT_ORDER_SLOT,
                McUtils.containerMenu().containerId,
                shouldRightClick ? GLFW.GLFW_MOUSE_BUTTON_RIGHT : GLFW.GLFW_MOUSE_BUTTON_LEFT,
                McUtils.containerMenu().getItems());

        clickCountdown -= 1;
    }
}
