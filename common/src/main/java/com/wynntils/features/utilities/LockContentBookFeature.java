/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.models.activities.event.ContentBookOpenEvent;
import com.wynntils.models.activities.event.ContentBookOpenEvent.OpenAction;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class LockContentBookFeature extends Feature {
    private static final long COMBAT_LOCK_DURATION_MS = 5_000L;

    public LockContentBookFeature() {
        super(ProfileDefault.DISABLED, List.of(ConfigDependency.functionality(Models.Combat.trackDamage)));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContentBookOpen(ContentBookOpenEvent event) {
        if (!Models.WorldState.onWorld() || Models.WorldState.inCharacterWardrobe()) return;
        if (!shouldBlockOpen(
                event.getAction(), System.currentTimeMillis() - Models.Combat.getLastDamageDealtTimestamp())) return;

        event.setCanceled(true);
        Managers.Notification.queueMessage(Component.translatable("feature.wynntils.lockContentBook.locked")
                .withStyle(ChatFormatting.RED));
        McUtils.playSoundUI(SoundEvents.ANVIL_LAND);
    }

    static boolean shouldBlockOpen(OpenAction action, long timeSinceLastDamage) {
        return timeSinceLastDamage < COMBAT_LOCK_DURATION_MS
                && !action.isShift()
                && action != OpenAction.INVENTORY_CLICK;
    }
}
