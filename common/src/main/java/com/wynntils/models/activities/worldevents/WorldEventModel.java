/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.worldevents;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.models.activities.bossbars.AnnihilationSunBar;
import com.wynntils.models.activities.type.ActivityDifficulty;
import com.wynntils.models.activities.type.ActivityDistance;
import com.wynntils.models.activities.type.ActivityInfo;
import com.wynntils.models.activities.type.ActivityLength;
import com.wynntils.models.containers.containers.reward.EventContainer;
import com.wynntils.models.containers.event.ValuableFoundEvent;
import com.wynntils.models.items.items.game.CorruptedCacheItem;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public final class WorldEventModel extends Model {
    public static final AnnihilationSunBar annihilationSunBar = new AnnihilationSunBar();

    private int nextExpectedRewardContainerId = -2;

    public WorldEventModel() {
        super(List.of());

        Handlers.BossBar.registerBar(annihilationSunBar);
    }

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent.Pre e) {
        if (Models.Container.getCurrentContainer() instanceof EventContainer eventContainer) {
            nextExpectedRewardContainerId = eventContainer.getContainerId();
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenClosedEvent e) {
        nextExpectedRewardContainerId = -2;
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent.Post event) {
        if (event.getContainerId() != nextExpectedRewardContainerId) return;
        if (event.getSlot() >= Models.LootChest.LOOT_CHEST_ITEM_COUNT) return;

        ItemStack itemStack = event.getItemStack();

        Optional<CorruptedCacheItem> cacheItem = Models.Item.asWynnItem(itemStack, CorruptedCacheItem.class);
        if (cacheItem.isPresent()) {
            WynntilsMod.postEvent(new ValuableFoundEvent(itemStack, ValuableFoundEvent.ItemSource.WORLD_EVENT));
            return;
        }
    }

    private WorldEventInfo getWorldEventInfoFromActivity(ActivityInfo activity) {
        return new WorldEventInfo(
                activity.name(),
                activity.specialInfo().orElse(""),
                activity.description().orElse(StyledText.EMPTY).getString(),
                activity.status(),
                activity.requirements().level().key(),
                activity.distance().orElse(ActivityDistance.NEAR),
                activity.length().orElse(ActivityLength.SHORT),
                activity.difficulty().orElse(ActivityDifficulty.EASY),
                activity.rewards());
    }
}
