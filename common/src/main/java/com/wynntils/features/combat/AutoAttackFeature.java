/*
 * Copyright © Wynntils 2024-2026.
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
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.QueuedMeleeScheduler;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class AutoAttackFeature extends Feature {
    @Persisted
    private final Config<Boolean> adaptiveLagCorrection = new Config<>(true);

    private long lastSpellInput = -1L;
    private int spellInputs = 0;

    public AutoAttackFeature() {
        super(ProfileDefault.DISABLED);
    }

    @SubscribeEvent
    public void onChangeCarriedItemEvent(ChangeCarriedItemEvent event) {
        lastSpellInput = -1L;
        spellInputs = 0;
    }

    @SubscribeEvent
    public void onSetSlotEvent(SetSlotEvent.Post event) {
        if (event.getSlot() != McUtils.inventory().selected) return;

        lastSpellInput = -1L;
        spellInputs = 0;
    }

    @SubscribeEvent
    public void onSwing(ArmSwingEvent event) {
        if (Models.Character.getClassType() != ClassType.ARCHER && spellInputs == 0) return;

        handleInput(false);
    }

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        if (Models.Character.getClassType() == ClassType.ARCHER && spellInputs == 0) return;

        handleInput(false);
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.InteractAt event) {
        if (Models.Character.getClassType() == ClassType.ARCHER && spellInputs == 0) return;

        if (event.getEntityHitResult() != null) {
            EntityType<?> entityType = event.getEntityHitResult().getEntity().getType();
            if (entityType == EntityType.INTERACTION) return;
        }

        handleInput(true);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (McUtils.player() == null) return;
        if (!Models.WorldState.onWorld()) return;

        int tickCount = McUtils.player().tickCount;

        if (lastSpellInput + Models.Spell.SPELL_COST_RESET_TICKS < tickCount) {
            lastSpellInput = -1L;
            spellInputs = 0;
        }

        boolean triggerHeld = Models.Character.getClassType() == ClassType.ARCHER
                ? McUtils.options().keyUse.isDown()
                : McUtils.options().keyAttack.isDown();

        if (!shouldQueueHeldAutoAttack(
                true,
                true,
                Models.SpellCaster.isSendingInputs(),
                Managers.Feature.getFeatureInstance(QuickCastFeature.class)
                        .isNormalAutoAttackTriggerActingAsSpellModifier(),
                Models.Raid.isParasiteOvertaken(),
                lastSpellInput + Models.Spell.SPELL_COST_RESET_TICKS > tickCount,
                triggerHeld)) {
            return;
        }

        QueuedMeleeScheduler.queueCurrentMelee(0, 0, adaptiveLagCorrection.get(), true);
    }

    static boolean shouldQueueHeldAutoAttack(
            boolean playerPresent,
            boolean onWorld,
            boolean spellCasterSending,
            boolean actingAsSpellModifier,
            boolean parasiteOvertaken,
            boolean inSpellInputWindow,
            boolean triggerHeld) {
        if (!playerPresent) return false;
        if (!onWorld) return false;
        if (spellCasterSending) return false;
        if (actingAsSpellModifier) return false;
        if (parasiteOvertaken) return false;
        if (inSpellInputWindow) return false;

        return triggerHeld;
    }

    private void handleInput(boolean interaction) {
        if (lastSpellInput == -1L || spellInputs < 3) {
            lastSpellInput = McUtils.player().tickCount;
            spellInputs++;

            // If the input came from PlayerInteractEvent.InteractAt then a UseItemEvent was
            // also sent so we need to only include one of them
            if (interaction) {
                spellInputs--;
            }

            if (spellInputs == 3) {
                spellInputs = 0;
                lastSpellInput = -1;
            }
        }
    }
}
