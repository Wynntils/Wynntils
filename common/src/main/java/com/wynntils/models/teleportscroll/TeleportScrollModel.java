/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.teleportscroll;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.models.items.items.game.TeleportScrollItem;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public final class TeleportScrollModel extends Model {
    private static final int MAX_CHARGES = 3;
    private static final long RECHARGE_INTERVAL_MS = TimeUnit.MINUTES.toMillis(10);
    // this value can be fairly high since we don't expect any 1->2 then another 1->2
    // immediately after, like pretty much ever
    // we can assume this is desync between scroll items and just ignore it
    private static final long TRANSITION_DEDUP_WINDOW_MS = TimeUnit.SECONDS.toMillis(30);

    @Persisted
    private final Storage<Long> nextRechargeTimestamp = new Storage<>(-1L);

    private int lastProcessedOldCharges = -1;
    private int lastProcessedNewCharges = -1;
    private long lastProcessedTransitionTimestamp = 0L;

    public TeleportScrollModel() {
        super(List.of(Models.Item));
    }

    public int getTeleportScrollRechargeTimerSeconds() {
        long nextRechargeAt = nextRechargeTimestamp.get();
        if (nextRechargeAt == -1L) return -1;

        long now = System.currentTimeMillis();
        long remainingMs = Math.max(0L, nextRechargeAt - now);
        return (int) Math.ceil(remainingMs / 1000d);
    }

    @SubscribeEvent
    public void onSlotSet(SetSlotEvent.Post event) {
        if (McUtils.player() == null) return;
        if (!Objects.equals(event.getContainer(), McUtils.inventory())) return;

        Optional<TeleportScrollItem> oldScrollOpt =
                Models.Item.asWynnItem(event.getOldItemStack(), TeleportScrollItem.class);
        Optional<TeleportScrollItem> newScrollOpt =
                Models.Item.asWynnItem(event.getItemStack(), TeleportScrollItem.class);
        if (oldScrollOpt.isEmpty() && newScrollOpt.isEmpty()) return;

        int oldCharges =
                oldScrollOpt.map(TeleportScrollItem::getRemainingCharges).orElse(-1);
        int newCharges =
                newScrollOpt.map(TeleportScrollItem::getRemainingCharges).orElse(-1);

        long now = System.currentTimeMillis();
        if (oldCharges == -1 || newCharges == -1) {
            int inventoryCharges = getTeleportScrollCharges();
            if (inventoryCharges >= MAX_CHARGES) clearRechargeCycle();
            return;
        }

        if (oldCharges == newCharges) return;
        // Shared charges update every scroll stack, so dedupe equivalent transitions in a short window
        if (isDuplicateTransition(oldCharges, newCharges, now)) return;

        // Charge spent (3->2, 2->1, 1->0):
        // keep existing timer if active, otherwise start the first 10-minute interval
        if (newCharges < oldCharges) {
            if (nextRechargeTimestamp.get() == -1L) {
                nextRechargeTimestamp.store(now + RECHARGE_INTERVAL_MS);
            }
            return;
        }

        // Fully recharged means no active recharge timer
        if (newCharges >= MAX_CHARGES) {
            clearRechargeCycle();
            return;
        }

        // Scroll charge bomb grants +2 charges without affecting normal timed recharges
        // Keep the current timer unchanged when this specific jump is observed
        // technically 1->3 and 2->3 from bombs also don't affect, but timer is reset then anyway
        if (oldCharges == 0 && newCharges == 2) return;

        // Normal recharge increase (e.g. 1->2): next recharge is 10 minutes from now
        nextRechargeTimestamp.store(now + RECHARGE_INTERVAL_MS);
    }

    public int getTeleportScrollCharges() {
        if (McUtils.player() == null) return -1;

        int maxCharges = -1;
        for (ItemStack itemStack : McUtils.inventory().items) {
            Optional<TeleportScrollItem> scrollOpt = Models.Item.asWynnItem(itemStack, TeleportScrollItem.class);
            if (scrollOpt.isPresent()) {
                maxCharges = Math.max(maxCharges, scrollOpt.get().getRemainingCharges());
            }
        }

        return maxCharges;
    }

    private void clearRechargeCycle() {
        if (nextRechargeTimestamp.get() != -1L) {
            nextRechargeTimestamp.store(-1L);
        }
    }

    private boolean isDuplicateTransition(int oldCharges, int newCharges, long now) {
        if (oldCharges == lastProcessedOldCharges
                && newCharges == lastProcessedNewCharges
                && now - lastProcessedTransitionTimestamp <= TRANSITION_DEDUP_WINDOW_MS) {
            return true;
        }

        lastProcessedOldCharges = oldCharges;
        lastProcessedNewCharges = newCharges;
        lastProcessedTransitionTimestamp = now;
        return false;
    }
}
