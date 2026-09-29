/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.mod.TickSchedulerManager;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.models.activities.event.ContentBookOpenEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class LockContentBookFeature extends Feature {
    private long nextLockNotificationTime;
    private boolean activityLocked;
    private TickSchedulerManager.ScheduledTask lockStateTask;

    @Persisted
    private final Config<ForceUnlockAction> forceUnlockAction = new Config<>(ForceUnlockAction.NONE);

    @Persisted
    private final Config<Boolean> lockInRaid = new Config<>(true);

    @Persisted
    private final Config<Boolean> lockInDungeon = new Config<>(true);

    @Persisted
    private final Config<Boolean> lockInLootrun = new Config<>(true);

    @Persisted
    private final Config<Boolean> lockInWorldEvent = new Config<>(true);

    @Persisted
    private final Config<Boolean> lockInWar = new Config<>(true);

    public LockContentBookFeature() {
        super(ProfileDefault.DISABLED);
    }

    @Override
    public void onEnable() {
        activityLocked = false;
        nextLockNotificationTime = 0;
        lockStateTask = Managers.TickScheduler.scheduleNextTick(this::checkLockState);
    }

    @Override
    public void onDisable() {
        if (lockStateTask != null) {
            Managers.TickScheduler.cancel(lockStateTask);
            lockStateTask = null;
        }
        if (activityLocked && Models.WorldState.onWorld()) {
            notifyLockState(false);
        }
        activityLocked = false;
        nextLockNotificationTime = 0;
    }

    private void checkLockState() {
        lockStateTask = null;
        if (!isEnabled()) return;
        updateLockState();
        lockStateTask = Managers.TickScheduler.scheduleLater(this::checkLockState, 4);
    }

    private boolean updateLockState() {
        boolean locked = isLocked();
        if (locked != activityLocked) {
            activityLocked = locked;
            notifyLockState(locked);
        }
        return locked;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContentBookOpen(ContentBookOpenEvent event) {
        if (!updateLockState()) return;
        if ((event.getAction() == ContentBookOpenEvent.OpenAction.INVENTORY_CLICK
                        || event.getAction() == ContentBookOpenEvent.OpenAction.SHIFT_INVENTORY_CLICK)
                && forceUnlockAction.get().allowsInventoryClick()) return;
        if (event.getAction() == ContentBookOpenEvent.OpenAction.SHIFT_RIGHT_CLICK
                && forceUnlockAction.get().allowsShiftRightClick()) return;

        event.setCanceled(true);
        notifyLocked();
    }

    private void notifyLocked() {
        long now = Util.getMillis();
        if (now < nextLockNotificationTime) return;
        notifyLockState(true);
    }

    private void notifyLockState(boolean locked) {
        nextLockNotificationTime = locked ? Util.getMillis() + 1000 : 0;
        Managers.Notification.queueMessage(Component.translatable(
                        locked
                                ? "feature.wynntils.lockContentBook.locked"
                                : "feature.wynntils.lockContentBook.unlocked")
                .withStyle(locked ? ChatFormatting.RED : ChatFormatting.GREEN));
        McUtils.playSoundUI(SoundEvents.NOTE_BLOCK_PLING.value());
    }

    private boolean isLocked() {
        if (!Models.WorldState.onWorld() || Models.WorldState.inCharacterWardrobe()) return false;

        return (lockInRaid.get() && Models.Raid.getCurrentRaid() != null)
                || (lockInDungeon.get() && Models.Dungeon.isInDungeon())
                || (lockInLootrun.get() && Models.Lootrun.getState().isRunning())
                || (lockInWorldEvent.get() && Models.WorldEvent.getCurrentWorldEvent() != null)
                || (lockInWar.get() && Models.War.isWarActive());
    }

    private enum ForceUnlockAction {
        NONE,
        INVENTORY_CLICK,
        SHIFT_RIGHT_CLICK,
        INVENTORY_CLICK_AND_SHIFT_RIGHT_CLICK;

        private boolean allowsInventoryClick() {
            return this == INVENTORY_CLICK || this == INVENTORY_CLICK_AND_SHIFT_RIGHT_CLICK;
        }

        private boolean allowsShiftRightClick() {
            return this == SHIFT_RIGHT_CLICK || this == INVENTORY_CLICK_AND_SHIFT_RIGHT_CLICK;
        }

        @Override
        public String toString() {
            return switch (this) {
                case NONE -> "None";
                case INVENTORY_CLICK -> "Inventory Click";
                case SHIFT_RIGHT_CLICK -> "Shift Right Click";
                case INVENTORY_CLICK_AND_SHIFT_RIGHT_CLICK -> "Inventory Click & Shift Right Click";
            };
        }
    }
}
