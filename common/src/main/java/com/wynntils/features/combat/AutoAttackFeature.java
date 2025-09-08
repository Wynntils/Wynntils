/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.properties.RequirementItemProperty;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.MouseUtils;
import com.wynntils.utils.wynn.ItemUtils;
import java.util.Optional;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.COMBAT)
public class AutoAttackFeature extends Feature {
    private static final int TICKS_PER_ATTACK = 2;

    private long lastSpellInput = -1L;
    private int spellInputs = 0;
    private WeaponStatus weaponStatus = WeaponStatus.UNKNOWN;

    @SubscribeEvent
    public void onChangeCarriedItemEvent(ChangeCarriedItemEvent event) {
        lastSpellInput = -1L;
        spellInputs = 0;

        updateWeaponStatus();
    }

    @SubscribeEvent
    public void onSetSlotEvent(SetSlotEvent.Post event) {
        if (event.getSlot() == McUtils.inventory().selected) {
            updateWeaponStatus();
        }
    }

    @SubscribeEvent
    public void onSwing(ArmSwingEvent event) {
        if (Models.Character.getClassType() != ClassType.ARCHER && spellInputs == 0) return;

        handleInput();
    }

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        if (Models.Character.getClassType() == ClassType.ARCHER && spellInputs == 0) return;

        handleInput();
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.InteractAt event) {
        if (Models.Character.getClassType() == ClassType.ARCHER && spellInputs == 0) return;

        if (event.getEntityHitResult() != null) {
            EntityType<?> entityType = event.getEntityHitResult().getEntity().getType();
            if (entityType == EntityType.INTERACTION) return;
        }

        handleInput();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (!Models.Spell.isSpellQueueEmpty()) return;

        int tickCount = McUtils.player().tickCount;

        if (lastSpellInput + Models.Spell.SPELL_COST_RESET_TICKS < tickCount) {
            lastSpellInput = -1L;
            spellInputs = 0;
        }

        if (Models.Raid.isParasiteOvertaken()) return;
        if (tickCount % TICKS_PER_ATTACK != 0) return;
        if (lastSpellInput + Models.Spell.SPELL_COST_RESET_TICKS > McUtils.player().tickCount) return;

        if (weaponStatus == WeaponStatus.UNKNOWN) {
            updateWeaponStatus();
        }

        if (weaponStatus != WeaponStatus.USABLE) return;

        if (Models.Character.getClassType() == ClassType.ARCHER
                ? !McUtils.options().keyUse.isDown()
                : !McUtils.options().keyAttack.isDown()) return;

        MouseUtils.sendAttackInput(Models.Character.getClassType() == ClassType.ARCHER);
    }

    private void handleInput() {
        if (lastSpellInput == -1L || spellInputs < 3) {
            lastSpellInput = McUtils.player().tickCount;
            spellInputs++;

            if (spellInputs == 3) {
                spellInputs = 0;
                lastSpellInput = -1;
            }
        }
    }

    private void updateWeaponStatus() {
        ItemStack heldItem = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);
        if (!ItemUtils.isWeapon(heldItem)) {
            weaponStatus = WeaponStatus.NOT_USABLE;
            return;
        }

        Optional<RequirementItemProperty> wynnItem =
                Models.Item.asWynnItemProperty(heldItem, RequirementItemProperty.class);
        if (wynnItem.isPresent() && wynnItem.get().meetsActualRequirements()) {
            weaponStatus = WeaponStatus.USABLE;
        } else {
            weaponStatus = WeaponStatus.NOT_USABLE;
        }
    }

    private enum WeaponStatus {
        UNKNOWN,
        NOT_USABLE,
        USABLE
    }
}
