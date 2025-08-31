/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.properties.ClassableItemProperty;
import com.wynntils.models.items.properties.RequirementItemProperty;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ItemUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.COMBAT)
public class QuickCastFeature extends Feature {
    @RegisterKeyBind
    private final KeyBind castFirstSpell = new KeyBind("Cast 1st Spell", GLFW.GLFW_KEY_Z, false, this::castFirstSpell);

    @RegisterKeyBind
    private final KeyBind castSecondSpell =
            new KeyBind("Cast 2nd Spell", GLFW.GLFW_KEY_X, false, this::castSecondSpell);

    @RegisterKeyBind
    private final KeyBind castThirdSpell = new KeyBind("Cast 3rd Spell", GLFW.GLFW_KEY_C, false, this::castThirdSpell);

    @RegisterKeyBind
    private final KeyBind castFourthSpell =
            new KeyBind("Cast 4th Spell", GLFW.GLFW_KEY_V, false, this::castFourthSpell);

    @Persisted
    private final Config<Integer> leftClickTickDelay = new Config<>(3);

    @Persisted
    private final Config<Integer> rightClickTickDelay = new Config<>(3);

    @Persisted
    private final Config<Boolean> blockAttacks = new Config<>(true);

    @Persisted
    private final Config<Boolean> checkValidWeapon = new Config<>(true);

    @Persisted
    private final Config<SafeCastType> safeCasting = new Config<>(SafeCastType.NONE);

    @Persisted
    private final Config<Integer> spellCooldown = new Config<>(0);

    private int lastSpellTick = 0;
    private int packetCountdown = 0;

    @SubscribeEvent
    public void onSwing(ArmSwingEvent event) {
        lastSpellTick = McUtils.player().tickCount;

        if (!blockAttacks.get()) return;
        if (event.getActionContext() != ArmSwingEvent.ArmSwingContext.ATTACK_OR_START_BREAKING_BLOCK) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (Models.Spell.isSpellQueueEmpty()) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        lastSpellTick = McUtils.player().tickCount;

        if (!blockAttacks.get()) return;
        if (Models.Spell.isSpellQueueEmpty()) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onHeldItemChange(ChangeCarriedItemEvent event) {
        resetState();
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        resetState();
    }

    private void castFirstSpell() {
        tryCastSpell(SpellUnit.PRIMARY, SpellUnit.SECONDARY, SpellUnit.PRIMARY);
    }

    private void castSecondSpell() {
        tryCastSpell(SpellUnit.PRIMARY, SpellUnit.PRIMARY, SpellUnit.PRIMARY);
    }

    private void castThirdSpell() {
        tryCastSpell(SpellUnit.PRIMARY, SpellUnit.SECONDARY, SpellUnit.SECONDARY);
    }

    private void castFourthSpell() {
        tryCastSpell(SpellUnit.PRIMARY, SpellUnit.PRIMARY, SpellUnit.SECONDARY);
    }

    private void tryCastSpell(SpellUnit a, SpellUnit b, SpellUnit c) {
        if (!Models.Spell.isSpellQueueEmpty()) return;

        SpellDirection[] spellInProgress = Models.Spell.getLastSpell();
        // SpellModel keeps the last spell for other uses but here we just want to know the inputs so if a full spell
        // is the last spell then we just reset it to empty
        if (spellInProgress.length == 3) {
            spellInProgress = SpellDirection.NO_SPELL;
        }

        if (safeCasting.get() == SafeCastType.BLOCK_ALL && spellInProgress.length != 0) {
            sendCancelReason(Component.translatable("feature.wynntils.quickCast.spellInProgress"));
            return;
        }
        if (safeCasting.get() == SafeCastType.FINISH_COMPATIBLE && spellInProgress.length != 0 && lastSpellTick == 0) {
            sendCancelReason(Component.translatable("feature.wynntils.quickCast.spellInProgress"));
            return;
        }

        boolean isArcher = Models.Character.getClassType() == ClassType.ARCHER;

        if (checkValidWeapon.get()) {
            ItemStack heldItem = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);

            if (!ItemUtils.isWeapon(heldItem)) {
                sendCancelReason(Component.translatable("feature.wynntils.quickCast.notAWeapon"));
                return;
            }

            // First check if the character is an archer or not in case CharacterModel failed to parse correctly
            Optional<ClassableItemProperty> classItemPropOpt =
                    Models.Item.asWynnItemProperty(heldItem, ClassableItemProperty.class);

            if (classItemPropOpt.isEmpty()) {
                sendCancelReason(Component.translatable("feature.wynntils.quickCast.notAWeapon"));
                return;
            } else {
                isArcher = classItemPropOpt.get().getRequiredClass() == ClassType.ARCHER;
            }

            // Now check for met requirements
            Optional<RequirementItemProperty> reqItemPropOpt =
                    Models.Item.asWynnItemProperty(heldItem, RequirementItemProperty.class);

            if (reqItemPropOpt.isPresent() && !reqItemPropOpt.get().meetsActualRequirements()) {
                sendCancelReason(Component.translatable("feature.wynntils.quickCast.notMetRequirements"));
                return;
            }
        }

        boolean isSpellInverted = isArcher;
        List<SpellDirection> unconfirmedSpell = Stream.of(a, b, c)
                .map(x -> (x == SpellUnit.PRIMARY) == isSpellInverted ? SpellDirection.LEFT : SpellDirection.RIGHT)
                .toList();

        List<SpellDirection> confirmedSpell = new ArrayList<>(unconfirmedSpell);

        if (safeCasting.get() == SafeCastType.FINISH_COMPATIBLE && spellInProgress.length != 0) {
            for (int i = 0; i < spellInProgress.length; i++) {
                if (spellInProgress[i] == unconfirmedSpell.get(i)) {
                    confirmedSpell.removeFirst();
                } else {
                    sendCancelReason(Component.translatable("feature.wynntils.quickCast.incompatibleInProgress"));
                    return;
                }
            }
        }

        Models.Spell.addSpellToQueue(confirmedSpell);
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if (!Models.WorldState.onWorld()) return;

        if (packetCountdown > 0) {
            packetCountdown--;
        }

        if (packetCountdown > 0) return;

        if (Models.Spell.isSpellQueueEmpty()) return;

        SpellDirection nextDirection = Models.Spell.checkNextSpellDirection();

        if (nextDirection == null) return;

        int comparisonTime =
                nextDirection == SpellDirection.LEFT ? leftClickTickDelay.get() : rightClickTickDelay.get();
        if (McUtils.player().tickCount - lastSpellTick < comparisonTime) return;

        Models.Spell.sendNextSpell();
        lastSpellTick = McUtils.player().tickCount;

        if (Models.Spell.isSpellQueueEmpty()) {
            lastSpellTick = 0;
            packetCountdown = Math.max(packetCountdown, spellCooldown.get());
        }
    }

    private void resetState() {
        lastSpellTick = 0;
        packetCountdown = 0;
    }

    private static void sendCancelReason(MutableComponent reason) {
        Managers.Notification.queueMessage(reason.withStyle(ChatFormatting.RED));
    }

    public enum SpellUnit {
        PRIMARY,
        SECONDARY
    }

    public enum SafeCastType {
        NONE,
        BLOCK_ALL,
        FINISH_COMPATIBLE
    }
}
