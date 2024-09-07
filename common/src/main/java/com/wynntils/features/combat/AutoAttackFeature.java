/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ItemUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class AutoAttackFeature extends Feature {
    static final int TICKS_PER_ATTACK = 2;

    int attackCooldown = 0;

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (!McUtils.options().keyAttack.isDown()) return;
        if (Models.Character.getClassType() == ClassType.ARCHER) return;

        LocalPlayer player = McUtils.player();
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (!ItemUtils.isWeapon(heldItem)) return;
        if (ItemUtils.getItemName(heldItem).contains("Unidentified")) return;
        for (StyledText lore : LoreUtils.getLore(heldItem)) {
            if (lore.contains("✖")) return;
        }

        if (attackCooldown != 0) {
            attackCooldown -= 1;
            return;
        }

        player.swing(InteractionHand.MAIN_HAND);
        attackCooldown = TICKS_PER_ATTACK - 1;
    }
}
