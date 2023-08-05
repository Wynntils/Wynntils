/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ItemUtils;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ContinuesAttackingFeature extends Feature {
    @SubscribeEvent
    public void onTick(TickEvent event) {
        Options options = McUtils.options();
        boolean isArcher = Models.Character.getClassType() == ClassType.ARCHER;
        if (isArcher ? !options.keyUse.isDown() : !options.keyAttack.isDown()) return;

        System.out.println("keyDown");
        LocalPlayer player = McUtils.player();
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (player.getCooldowns().isOnCooldown(itemInHand.getItem())) return;
        if (Models.Spell.getSpellPrediction() != SpellDirection.NO_SPELL) return;
        if (!ItemUtils.canBeWielded(itemInHand)) return;
        if (isArcher) {
            SpellDirection.RIGHT.getSendPacketRunnable().run();
        } else {
            SpellDirection.LEFT.getSendPacketRunnable().run();
        }
    }
}
