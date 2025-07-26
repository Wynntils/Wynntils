/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.models.worlds.event.CutsceneStartedEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.neoforged.bus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.UTILITIES)
public class AutoSkipCutscenesFeature extends Feature {
    @Persisted
    private final Config<SkipCondition> skipCondition = new Config<>(SkipCondition.ALL);

    @SubscribeEvent
    public void onCutsceneStarted(CutsceneStartedEvent e) {
        boolean shouldSkip = false;
        if (e.isGroupCutscene() && skipCondition.get() != SkipCondition.SOLO) {
            shouldSkip = true;
        } else if (!e.isGroupCutscene() && skipCondition.get() != SkipCondition.GROUP) {
            shouldSkip = true;
        }

        e.setCanceled(shouldSkip);

        if (shouldSkip) {
            McUtils.mc()
                    .getConnection()
                    .send(new ServerboundPlayerActionPacket(
                            ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                            BlockPos.ZERO,
                            Direction.DOWN));
        }
    }

    private enum SkipCondition {
        ALL,
        SOLO,
        GROUP
    }
}
