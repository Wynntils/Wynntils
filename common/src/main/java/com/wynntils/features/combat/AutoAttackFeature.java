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
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.ArmSwingEvent.ArmSwingContext;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ItemUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class AutoAttackFeature extends Feature {
    private static final int TICKS_PER_ATTACK = 2;

    private Boolean canUseWeapon = null;
    private int lastSelectedSlot;
    private boolean preventWrongCast = false;

    @SubscribeEvent
    public void onChangeCarriedItemEvent(ChangeCarriedItemEvent event) {
        canUseWeapon = null;
    }

    @SubscribeEvent
    public void onSetSlotEvent(SetSlotEvent.Post event) {
        if (event.getSlot() == McUtils.inventory().selected) {
            canUseWeapon = null;
        }
    }

    @SubscribeEvent
    public void onSwing(ArmSwingEvent event) {
        if (event.getActionContext() != ArmSwingContext.ATTACK_OR_START_BREAKING_BLOCK) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (Models.Character.getClassType() != ClassType.ARCHER) return;
        lastSelectedSlot = McUtils.inventory().selected;
        preventWrongCast = true;
    }

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        if (Models.Character.getClassType() == ClassType.ARCHER) return;
        lastSelectedSlot = McUtils.inventory().selected;
        preventWrongCast = true;
    }

    @SubscribeEvent
    public void onSpellCastCompleted(SpellEvent.Completed event) {
        preventWrongCast = false;
    }

    private boolean checkWeapon() {
        ItemStack heldItem = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);
        if (!ItemUtils.isWeapon(heldItem)) return false;
        if (ItemUtils.getItemName(heldItem).contains("Unidentified")) return false;
        for (StyledText lore : LoreUtils.getLore(heldItem)) {
            if (lore.contains("✖")) return false;
        }
        return true;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!Models.WorldState.onWorld()) return;

        int currentSelectedSlot = McUtils.inventory().selected;
        if (currentSelectedSlot == lastSelectedSlot) {
            if (preventWrongCast) return;
        } else {
            lastSelectedSlot = currentSelectedSlot;
            preventWrongCast = false;
        }

        LocalPlayer player = McUtils.player();
        if (player.tickCount % TICKS_PER_ATTACK != 0) return;

        if (canUseWeapon == null) {
            canUseWeapon = checkWeapon();
        }

        if (!canUseWeapon) return;

        boolean isArcher = Models.Character.getClassType() == ClassType.ARCHER;
        if (isArcher) {
            if (!McUtils.options().keyUse.isDown()) return;
        } else {
            if (!McUtils.options().keyAttack.isDown()) return;
        }

        if (isArcher) {
            SpellDirection.RIGHT.getSendPacketRunnable().run();
        } else {
            player.swing(InteractionHand.MAIN_HAND);
        }
    }
}
