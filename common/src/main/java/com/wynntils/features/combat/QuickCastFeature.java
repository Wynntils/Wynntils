/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.properties.ClassableItemProperty;
import com.wynntils.models.items.properties.RequirementItemProperty;
import com.wynntils.models.spells.type.CombatClickType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ItemUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class QuickCastFeature extends Feature {
    private static final int NO_PENDING_SPELL = -1;
    private static final List<CombatClickType> FIRST_SPELL_SEQUENCE =
            List.of(CombatClickType.PRIMARY, CombatClickType.SECONDARY, CombatClickType.PRIMARY);
    private static final List<CombatClickType> SECOND_SPELL_SEQUENCE =
            List.of(CombatClickType.PRIMARY, CombatClickType.PRIMARY, CombatClickType.PRIMARY);
    private static final List<CombatClickType> THIRD_SPELL_SEQUENCE =
            List.of(CombatClickType.PRIMARY, CombatClickType.SECONDARY, CombatClickType.SECONDARY);
    private static final List<CombatClickType> FOURTH_SPELL_SEQUENCE =
            List.of(CombatClickType.PRIMARY, CombatClickType.PRIMARY, CombatClickType.SECONDARY);
    private static final List<CombatClickType> MELEE_SEQUENCE = List.of(CombatClickType.MELEE);

    @RegisterKeyBind
    private final KeyBind castFirstSpell = KeyBindDefinition.CAST_FIRST_SPELL.create(() -> {});

    @RegisterKeyBind
    private final KeyBind castSecondSpell = KeyBindDefinition.CAST_SECOND_SPELL.create(() -> {});

    @RegisterKeyBind
    private final KeyBind castThirdSpell = KeyBindDefinition.CAST_THIRD_SPELL.create(() -> {});

    @RegisterKeyBind
    private final KeyBind castFourthSpell = KeyBindDefinition.CAST_FOURTH_SPELL.create(() -> {});

    @RegisterKeyBind
    private final KeyBind castMeleeAttack = KeyBindDefinition.CAST_MELEE_ATTACK.create(() -> {});

    private final SpellBinding[] spellBindings = {
        new SpellBinding(castFirstSpell, FIRST_SPELL_SEQUENCE),
        new SpellBinding(castSecondSpell, SECOND_SPELL_SEQUENCE),
        new SpellBinding(castThirdSpell, THIRD_SPELL_SEQUENCE),
        new SpellBinding(castFourthSpell, FOURTH_SPELL_SEQUENCE)
    };
    private final boolean[] spellKeysWereDown = new boolean[spellBindings.length];
    private final boolean[] spellKeysJustPressed = new boolean[spellBindings.length];
    private final long[] spellPressOrders = new long[spellBindings.length];
    private boolean meleeKeyWasDown = false;
    private boolean meleeKeyJustPressed = false;
    private long spellPressOrderCounter = 0L;
    private int pendingNextSpellIndex = NO_PENDING_SPELL;

    @Persisted
    private final Config<Integer> leftClickDelayMs = new Config<>(100);

    @Persisted
    private final Config<Integer> rightClickDelayMs = new Config<>(100);

    @Persisted
    private final Config<Boolean> repeatMelee = new Config<>(false);

    @Persisted
    private final Config<Boolean> checkValidWeapon = new Config<>(true);

    @Persisted
    private final Config<Integer> spellCooldownMs = new Config<>(0);

    public QuickCastFeature() {
        super(ProfileDefault.ENABLED);
    }

    @Override
    public void onEnable() {
        clearInputSelectionState();
        Models.SpellCaster.setIdleListener(this::tryCastHeldInputsImmediately);
    }

    @Override
    public void onDisable() {
        Models.SpellCaster.setIdleListener(null);
        clearInputSelectionState();
    }

    @SubscribeEvent
    public void onHeldItemChange(ChangeCarriedItemEvent event) {
        clearInputSelectionState();
        Models.SpellCaster.clear();
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent event) {
        clearInputSelectionState();
        Models.SpellCaster.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!Models.WorldState.onWorld()) {
            refreshInputSelectionState();
            discardBlockedPendingInputs();
            return;
        }

        tryCastHeldInputsImmediately();
    }

    private void tryCastHeldInputsImmediately() {
        refreshInputSelectionState();
        if (!isEnabled()) return;
        if (!canHandleInput()) {
            discardBlockedPendingInputs();
            return;
        }
        if (pendingNextSpellIndex == spellBindings.length) {
            if (Models.SpellCaster.isBusy()) return;

            tryCastMelee(meleeKeyJustPressed);
            pendingNextSpellIndex = NO_PENDING_SPELL;
            return;
        }

        tryCastHeldSpell();
        tryCastHeldMelee();
    }

    private void tryCastHeldSpell() {
        if (Models.SpellCaster.isBusy()) return;

        SpellSelection spellSelection = getNextSpellSelection();
        if (spellSelection == null) return;

        if (tryCastSpell(spellSelection.spellBinding().clicks(), spellSelection.notifyInvalidWeapon())
                && spellSelection.index() == pendingNextSpellIndex) {
            pendingNextSpellIndex = NO_PENDING_SPELL;
        }
    }

    private void tryCastHeldMelee() {
        if (!castMeleeAttack.getKeyMapping().isDown()) return;
        if (Models.SpellCaster.isBusy()) return;
        if (!repeatMelee.get() && !meleeKeyJustPressed) return;

        tryCastMelee(meleeKeyJustPressed);
    }

    private boolean tryCastSpell(List<CombatClickType> clicks, boolean notifyInvalidWeapon) {
        if (!canHandleInput()) return false;

        WeaponContext weaponContext = getWeaponContext(notifyInvalidWeapon);
        if (!weaponContext.valid()) return false;

        return Models.SpellCaster.queueClicks(
                clicks,
                weaponContext.isArcher(),
                leftClickDelayMs.get(),
                rightClickDelayMs.get(),
                spellCooldownMs.get());
    }

    private void tryCastMelee(boolean notifyInvalidWeapon) {
        if (!canHandleInput()) return;

        WeaponContext weaponContext = getWeaponContext(notifyInvalidWeapon);
        if (!weaponContext.valid()) return;

        Models.SpellCaster.queueClicks(
                MELEE_SEQUENCE, weaponContext.isArcher(), leftClickDelayMs.get(), rightClickDelayMs.get(), 0);
    }

    private boolean canHandleInput() {
        if (!Models.WorldState.onWorld()) return false;
        if (Models.WorldState.inCharacterWardrobe()) return false;

        return true;
    }

    private void discardBlockedPendingInputs() {
        Arrays.fill(spellKeysJustPressed, false);
        meleeKeyJustPressed = false;
        pendingNextSpellIndex = NO_PENDING_SPELL;
    }

    private void refreshInputSelectionState() {
        for (int i = 0; i < spellBindings.length; i++) {
            boolean isDown = spellBindings[i].keyBind().getKeyMapping().isDown();
            boolean justPressed = isDown && !spellKeysWereDown[i];
            spellKeysJustPressed[i] = justPressed;

            if (justPressed) {
                spellPressOrders[i] = ++spellPressOrderCounter;
                pendingNextSpellIndex = i;
            }

            spellKeysWereDown[i] = isDown;
        }

        boolean meleeDown = castMeleeAttack.getKeyMapping().isDown();
        meleeKeyJustPressed = meleeDown && !meleeKeyWasDown;
        if (meleeKeyJustPressed) {
            pendingNextSpellIndex = spellBindings.length;
        }
        meleeKeyWasDown = meleeDown;
    }

    private SpellSelection getNextSpellSelection() {
        if (pendingNextSpellIndex >= 0 && pendingNextSpellIndex < spellBindings.length) {
            return new SpellSelection(
                    pendingNextSpellIndex,
                    spellBindings[pendingNextSpellIndex],
                    spellKeysJustPressed[pendingNextSpellIndex]);
        }

        int newestHeldSpellIndex = getNewestHeldSpellIndex();
        if (newestHeldSpellIndex == NO_PENDING_SPELL) return null;

        return new SpellSelection(
                newestHeldSpellIndex, spellBindings[newestHeldSpellIndex], spellKeysJustPressed[newestHeldSpellIndex]);
    }

    private int getNewestHeldSpellIndex() {
        int newestHeldSpellIndex = NO_PENDING_SPELL;
        long newestHeldPressOrder = Long.MIN_VALUE;

        for (int i = 0; i < spellBindings.length; i++) {
            if (!spellKeysWereDown[i]) continue;
            if (spellPressOrders[i] <= newestHeldPressOrder) continue;

            newestHeldSpellIndex = i;
            newestHeldPressOrder = spellPressOrders[i];
        }

        return newestHeldSpellIndex;
    }

    private void clearInputSelectionState() {
        Arrays.fill(spellKeysWereDown, false);
        Arrays.fill(spellKeysJustPressed, false);
        Arrays.fill(spellPressOrders, 0L);
        meleeKeyWasDown = false;
        meleeKeyJustPressed = false;
        spellPressOrderCounter = 0L;
        pendingNextSpellIndex = NO_PENDING_SPELL;
    }

    private WeaponContext getWeaponContext(boolean notifyInvalidWeapon) {
        boolean isArcher = Models.Character.getClassType() == ClassType.ARCHER;
        if (!checkValidWeapon.get()) {
            return new WeaponContext(true, isArcher);
        }

        ItemStack heldItem = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);
        if (!ItemUtils.isWeapon(heldItem)) {
            sendCancelReason(notifyInvalidWeapon, Component.translatable("feature.wynntils.quickCast.notAWeapon"));
            return WeaponContext.invalid();
        }

        Optional<ClassableItemProperty> classItemPropOpt =
                Models.Item.asWynnItemProperty(heldItem, ClassableItemProperty.class);
        if (classItemPropOpt.isEmpty()) {
            sendCancelReason(notifyInvalidWeapon, Component.translatable("feature.wynntils.quickCast.notAWeapon"));
            return WeaponContext.invalid();
        }

        isArcher = classItemPropOpt.get().getRequiredClass() == ClassType.ARCHER;

        Optional<RequirementItemProperty> reqItemPropOpt =
                Models.Item.asWynnItemProperty(heldItem, RequirementItemProperty.class);
        if (reqItemPropOpt.isPresent() && !reqItemPropOpt.get().meetsActualRequirements()) {
            sendCancelReason(
                    notifyInvalidWeapon, Component.translatable("feature.wynntils.quickCast.notMetRequirements"));
            return WeaponContext.invalid();
        }

        return new WeaponContext(true, isArcher);
    }

    private static void sendCancelReason(boolean notifyInvalidWeapon, MutableComponent reason) {
        if (!notifyInvalidWeapon) return;

        Managers.Notification.queueMessage(reason.withStyle(ChatFormatting.RED));
    }

    private record WeaponContext(boolean valid, boolean isArcher) {
        private static WeaponContext invalid() {
            return new WeaponContext(false, false);
        }
    }

    private record SpellBinding(KeyBind keyBind, List<CombatClickType> clicks) {}

    private record SpellSelection(int index, SpellBinding spellBinding, boolean notifyInvalidWeapon) {}
}
