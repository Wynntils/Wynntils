/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.ArmSwingEvent.ArmSwingContext;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.properties.RequirementItemProperty;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ItemUtils;
import java.util.Optional;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.COMBAT)
public class AutoAttackFeature extends Feature {
    private static final int TICKS_PER_ATTACK = 2;
    private static final int SPELL_TIMEOUT_TICKS = 60;

    private WeaponStatus weaponStatus = WeaponStatus.UNKNOWN;
    private int lastSelectedSlot;
    private int preventWrongCast = Integer.MIN_VALUE;

    @SubscribeEvent
    public void onChangeCarriedItemEvent(ChangeCarriedItemEvent event) {
        weaponStatus = WeaponStatus.UNKNOWN;
    }

    @SubscribeEvent
    public void onSetSlotEvent(SetSlotEvent.Post event) {
        if (event.getSlot() == McUtils.inventory().selected) {
            weaponStatus = WeaponStatus.UNKNOWN;
        }
    }

    @SubscribeEvent
    public void onSwing(ArmSwingEvent event) {
        if (event.getActionContext() != ArmSwingContext.ATTACK_OR_START_BREAKING_BLOCK) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (Models.Character.getClassType() != ClassType.ARCHER) return;
        lastSelectedSlot = McUtils.inventory().selected;
        preventWrongCast = McUtils.player().tickCount + SPELL_TIMEOUT_TICKS;
    }

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        if (Models.Character.getClassType() == ClassType.ARCHER) return;
        lastSelectedSlot = McUtils.inventory().selected;
        preventWrongCast = McUtils.player().tickCount + SPELL_TIMEOUT_TICKS;
    }

    @SubscribeEvent
    public void onSpellCastCompleted(SpellEvent.Completed event) {
        preventWrongCast = Integer.MIN_VALUE;
    }

    private boolean isHoldingUsableWeapon() {
        ItemStack heldItem = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);
        if (!ItemUtils.isWeapon(heldItem)) return false;

        Optional<RequirementItemProperty> wynnItem =
                Models.Item.asWynnItemProperty(heldItem, RequirementItemProperty.class);
        return wynnItem.isPresent() && wynnItem.get().meetsActualRequirements();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!Models.WorldState.onWorld()) return;

        LocalPlayer player = McUtils.player();
        int currentSelectedSlot = McUtils.inventory().selected;
        if (currentSelectedSlot == lastSelectedSlot) {
            if (preventWrongCast > player.tickCount) return;
        } else {
            lastSelectedSlot = currentSelectedSlot;
            preventWrongCast = Integer.MIN_VALUE;
        }

        if (player.tickCount % TICKS_PER_ATTACK != 0) return;

        if (weaponStatus == WeaponStatus.UNKNOWN) {
            weaponStatus = isHoldingUsableWeapon() ? WeaponStatus.USABLE : WeaponStatus.NOT_USABLE;
        }

        if (weaponStatus != WeaponStatus.USABLE) return;

        if (Models.Character.getClassType() == ClassType.ARCHER) {
            if (!McUtils.options().keyUse.isDown()) return;
            SpellDirection.RIGHT.getSendPacketRunnable().run();
        } else {
            if (!McUtils.options().keyAttack.isDown()) return;
            // SpellDirection.LEFT doesn't do the swing animation
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private enum WeaponStatus {
        UNKNOWN,
        NOT_USABLE,
        USABLE
    }
}
