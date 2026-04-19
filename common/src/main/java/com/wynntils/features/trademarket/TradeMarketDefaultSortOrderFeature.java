package com.wynntils.features.trademarket;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.trademarket.event.TradeMarketStateEvent;
import com.wynntils.models.trademarket.type.TradeMarketState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.TRADEMARKET)
public class TradeMarketDefaultSortOrderFeature extends Feature {
    @Persisted
    private final Config<SortOrder> defaultSortOrder = new Config<>(SortOrder.MOST_RECENT);

    @Persisted
    private final Config<SortOrderChangeSpeed> sortOrderChangeSpeed = new Config<>(SortOrderChangeSpeed.BALANCED);

    private static final int SORT_ORDER_SLOT = 52;

    private boolean appliedDefaultSortOrder = false;
    private int clicksLeftCount = 0;

    public TradeMarketDefaultSortOrderFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.LITE, ConfigProfile.MINIMAL)
                .build());
    }

    @SubscribeEvent
    public void onTradeMarketState(TradeMarketStateEvent event) {
        TradeMarketState newState = event.getNewState();
        // Default sort order should only be applied once per opened TM since otherwise we risk overwriting user selected sort order
        if (!appliedDefaultSortOrder && newState == TradeMarketState.FILTERED_RESULTS) {
            appliedDefaultSortOrder = true;
            clicksLeftCount = defaultSortOrder.get().numberOfClicks;
        } else if (newState == TradeMarketState.NOT_ACTIVE) {
            // When TM is closed reset the flag and the counter just in case
            appliedDefaultSortOrder = false;
            clicksLeftCount = 0;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (clicksLeftCount <= 0) return;
        if (McUtils.mc().level.getGameTime() % sortOrderChangeSpeed.get().ticksDelay != 0) return;

        ContainerUtils.clickOnSlot(
                SORT_ORDER_SLOT,
                McUtils.containerMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                McUtils.containerMenu().getItems());

        clicksLeftCount -= 1;
    }

    public enum SortOrder {
        MOST_RECENT(0),
        LEAST_RECENT(1),
        MOST_EXPENSIVE(2),
        LEAST_EXPENSIVE(3),
        HIGHEST_LEVEL_RANGE(4),
        LOWEST_LEVEL_RANGE(5);

        public final int numberOfClicks;

        SortOrder(int numberOfClicks) {
            this.numberOfClicks = numberOfClicks;
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
