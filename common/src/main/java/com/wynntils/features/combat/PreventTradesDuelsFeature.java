/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.PlayerAttackEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.utils.wynn.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class PreventTradesDuelsFeature extends Feature {
    @Persisted
    private final Config<Boolean> onlyWhileFighting = new Config<>(true);

    @Persisted
    private final Config<Integer> fightingTimeCutoff = new Config<>(10); // seconds

    @Persisted
    private final Config<Boolean> whenHoldingGatheringTool = new Config<>(false);

    public PreventTradesDuelsFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.NEW_PLAYER, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onPlayerRightClick(PlayerInteractEvent.Interact event) {
        handlePlayerClick(event, event.getPlayer(), event.getItemStack(), event.getTarget());
    }

    @SubscribeEvent
    public void onPlayerLeftClick(PlayerAttackEvent event) {
        handlePlayerClick(event, event.getPlayer(), event.getPlayer().getMainHandItem(), event.getTarget());
    }

    private void handlePlayerClick(ICancellableEvent event, Player player, ItemStack itemStack, Entity target) {
        if (!player.isShiftKeyDown() || !(target instanceof Player p) || !Models.Player.isLocalPlayer(p)) return;
        if (Models.WorldState.inCharacterWardrobe()) return;

        int timeSinceLastFight =
                (int) ((System.currentTimeMillis() - Models.Combat.getLastDamageDealtTimestamp()) / 1000);

        if (ItemUtils.isWeapon(itemStack) && onlyWhileFighting.get() && timeSinceLastFight < fightingTimeCutoff.get()) {
            // stops interact packet from going out
            event.setCanceled(true);

            Managers.Notification.queueMessage(StyledText.fromString(ChatFormatting.BLUE + "Trade/Duel blocked for "
                    + (fightingTimeCutoff.get() - timeSinceLastFight) + " s"));
        } else if (ItemUtils.isGatheringTool(itemStack) && whenHoldingGatheringTool.get()) {
            // stops interact packet from going out
            event.setCanceled(true);

            Managers.Notification.queueMessage(
                    StyledText.fromString(ChatFormatting.BLUE + "Trade/Duel blocked whilst holding gathering tool"));
        }
    }
}
