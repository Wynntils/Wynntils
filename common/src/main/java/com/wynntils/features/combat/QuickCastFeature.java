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
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.DestroyBlockEvent;
import com.wynntils.mc.event.PlayerAttackEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.CombatClickType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
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
    private final QuickCastMeleeInsertionState meleeInsertionState = new QuickCastMeleeInsertionState();

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

    @Persisted
    private final Config<Boolean> adaptiveLagCorrection = new Config<>(true);

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
        Models.SpellCaster.clear();
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
    public void onSwing(ArmSwingEvent event) {
        if (shouldSuppressAttackInput(Models.SpellCaster.isSendingInputs(), shouldSuppressNormalAttackTriggerInput())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onAttack(PlayerAttackEvent event) {
        if (shouldSuppressAttackInput(Models.SpellCaster.isSendingInputs(), shouldSuppressNormalAttackTriggerInput())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onDestroyBlock(DestroyBlockEvent event) {
        if (shouldSuppressDestroyBlockInput(
                Models.SpellCaster.isSendingInputs(), shouldSuppressNormalAttackTriggerInput())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        if (shouldSuppressMainHandInput(
                Models.SpellCaster.isSendingInputs(), shouldSuppressNormalUseTriggerInput(), event.getHand())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.Interact event) {
        if (shouldSuppressMainHandInput(
                Models.SpellCaster.isSendingInputs(), shouldSuppressNormalUseTriggerInput(), event.getHand())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onInteractAt(PlayerInteractEvent.InteractAt event) {
        if (shouldSuppressMainHandInput(
                Models.SpellCaster.isSendingInputs(), shouldSuppressNormalUseTriggerInput(), event.getHand())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onUseItemOnBlock(PlayerInteractEvent.RightClickBlock event) {
        if (shouldSuppressMainHandInput(
                Models.SpellCaster.isSendingInputs(), shouldSuppressNormalUseTriggerInput(), event.getHand())) {
            event.setCanceled(true);
        }
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
        if (!QueuedMeleeScheduler.canHandleCombatInput()) {
            discardBlockedPendingInputs();
            return;
        }
        if (meleeKeyJustPressed) {
            meleeInsertionState.onStandaloneMeleePressed();
        }

        if (hasSelectedOrHeldSpell()) {
            meleeInsertionState.onSpellSelected();
        }
        tryCastHeldSpell();
        tryCastBufferedStandaloneMelee();
        tryCastHeldMelee();
    }

    private void tryCastHeldSpell() {
        if (Models.SpellCaster.isBusy()) return;

        SpellSelection spellSelection = getNextSpellSelection();
        if (spellSelection == null) return;

        boolean autoInsertMelee = shouldAutoInsertMelee();
        boolean quickMeleeLegal = isQuickMeleeLegal();
        List<CombatClickType> clicks = spellSelection.spellBinding().clicks();
        // Quick Cast stays responsive: if melee is still on cooldown, cast the spell now.
        if (meleeInsertionState.shouldPrependMelee(autoInsertMelee, quickMeleeLegal)) {
            clicks = prependMelee(clicks);
        }

        if (tryCastSpell(clicks, spellSelection.notifyInvalidWeapon())) {
            meleeInsertionState.onSpellQueued();
            if (spellSelection.index() == pendingNextSpellIndex) {
                pendingNextSpellIndex = NO_PENDING_SPELL;
            }
        }
    }

    private void tryCastHeldMelee() {
        boolean meleeHeld = castMeleeAttack.getKeyMapping().isDown();
        boolean pendingStandaloneMeleePress = meleeInsertionState.hasPendingStandaloneMeleePress();
        if (!meleeHeld && !pendingStandaloneMeleePress) return;
        if (Models.SpellCaster.isBusy()) return;
        if (pendingStandaloneMeleePress) {
            if (!shouldQueueDeferredStandaloneMelee(meleeHeld, meleeKeyJustPressed, repeatMelee.get())) return;

            QueuedMeleeScheduler.MeleeQueueResult meleeAttemptResult = tryCastMelee(true);
            if (meleeAttemptResult == QueuedMeleeScheduler.MeleeQueueResult.QUEUED) {
                meleeInsertionState.onStandaloneMeleeQueued();
            } else if (meleeAttemptResult == QueuedMeleeScheduler.MeleeQueueResult.BLOCKED_BY_COOLDOWN) {
                meleeInsertionState.onStandaloneMeleeCooldownBlocked();
            } else {
                meleeInsertionState.onStandaloneMeleeRejected();
            }
            return;
        }

        if (!repeatMelee.get()) return;

        if (tryCastMelee(false) == QueuedMeleeScheduler.MeleeQueueResult.QUEUED) {
            meleeInsertionState.onStandaloneMeleeQueued();
        }
    }

    private void tryCastBufferedStandaloneMelee() {
        if (!meleeInsertionState.hasBufferedStandaloneMelee()) return;
        if (Models.SpellCaster.isBusy()) return;

        QueuedMeleeScheduler.MeleeQueueResult meleeAttemptResult = tryCastMelee(false);
        if (meleeAttemptResult == QueuedMeleeScheduler.MeleeQueueResult.QUEUED) {
            meleeInsertionState.onStandaloneMeleeQueued();
        } else if (meleeAttemptResult != QueuedMeleeScheduler.MeleeQueueResult.BLOCKED_BY_COOLDOWN) {
            meleeInsertionState.onSpellSelected();
        }
    }

    private boolean tryCastSpell(List<CombatClickType> clicks, boolean notifyInvalidWeapon) {
        if (!QueuedMeleeScheduler.canHandleCombatInput()) return false;

        QueuedMeleeScheduler.WeaponContext weaponContext = getWeaponContext(notifyInvalidWeapon);
        if (!weaponContext.valid()) return false;

        return Models.SpellCaster.queueClicks(
                clicks,
                weaponContext.isArcher(),
                leftClickDelayMs.get(),
                rightClickDelayMs.get(),
                spellCooldownMs.get(),
                adaptiveLagCorrection.get());
    }

    private QueuedMeleeScheduler.MeleeQueueResult tryCastMelee(boolean notifyInvalidWeapon) {
        if (!QueuedMeleeScheduler.canHandleCombatInput()) return QueuedMeleeScheduler.MeleeQueueResult.NOT_QUEUED;
        if (!meleeInsertionState.shouldQueueStandaloneMelee(isQuickMeleeLegal())) {
            return QueuedMeleeScheduler.MeleeQueueResult.BLOCKED_BY_COOLDOWN;
        }

        QueuedMeleeScheduler.WeaponContext weaponContext = getWeaponContext(notifyInvalidWeapon);
        if (!weaponContext.valid()) return QueuedMeleeScheduler.MeleeQueueResult.NOT_QUEUED;

        return QueuedMeleeScheduler.queueCurrentMelee(
                weaponContext, leftClickDelayMs.get(), rightClickDelayMs.get(), adaptiveLagCorrection.get());
    }

    private void discardBlockedPendingInputs() {
        Arrays.fill(spellKeysJustPressed, false);
        meleeKeyJustPressed = false;
        pendingNextSpellIndex = NO_PENDING_SPELL;
        meleeInsertionState.clear();
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
        meleeInsertionState.clear();
    }

    private boolean hasSelectedOrHeldSpell() {
        if (pendingNextSpellIndex >= 0 && pendingNextSpellIndex < spellBindings.length) {
            return true;
        }

        return getNewestHeldSpellIndex() != NO_PENDING_SPELL;
    }

    private boolean shouldAutoInsertMelee() {
        return isAutoInsertMeleeTriggerHeld(
                Models.Character.getClassType(),
                Managers.Feature.getFeatureInstance(AutoAttackFeature.class).isEnabled(),
                castMeleeAttack.getKeyMapping().isDown(),
                McUtils.options().keyAttack.isDown(),
                McUtils.options().keyUse.isDown());
    }

    static boolean isAutoInsertMeleeTriggerHeld(
            ClassType classType,
            boolean autoAttackEnabled,
            boolean quickCastMeleeHeld,
            boolean normalAttackHeld,
            boolean normalUseHeld) {
        if (quickCastMeleeHeld) {
            return true;
        }

        if (!autoAttackEnabled) {
            return false;
        }

        return classType == ClassType.ARCHER ? normalUseHeld : normalAttackHeld;
    }

    boolean isNormalAutoAttackTriggerActingAsSpellModifier() {
        return isNormalAutoAttackTriggerActingAsSpellModifier(
                isEnabled(),
                canQuickCastNowSilently(),
                Models.Character.getClassType(),
                Managers.Feature.getFeatureInstance(AutoAttackFeature.class).isEnabled(),
                McUtils.options().keyAttack.isDown(),
                McUtils.options().keyUse.isDown(),
                hasRawSpellKeyDown(),
                hasSelectedOrHeldSpell(),
                Models.SpellCaster.isSendingInputs());
    }

    static boolean isNormalAutoAttackTriggerActingAsSpellModifier(
            boolean quickCastEnabled,
            boolean quickCastCastable,
            ClassType classType,
            boolean autoAttackEnabled,
            boolean normalAttackHeld,
            boolean normalUseHeld,
            boolean rawSpellKeyDown,
            boolean spellSelectedOrHeld,
            boolean spellCasterSendingInputs) {
        if (!quickCastEnabled || !quickCastCastable || !autoAttackEnabled) {
            return false;
        }

        boolean triggerHeld = classType == ClassType.ARCHER ? normalUseHeld : normalAttackHeld;
        if (!triggerHeld) {
            return false;
        }

        return rawSpellKeyDown || spellSelectedOrHeld || spellCasterSendingInputs;
    }

    static boolean shouldSuppressAttackInput(boolean sendingInputs, boolean suppressNormalAttackTriggerInput) {
        return sendingInputs || suppressNormalAttackTriggerInput;
    }

    static boolean shouldSuppressDestroyBlockInput(boolean sendingInputs, boolean suppressNormalAttackTriggerInput) {
        return sendingInputs || suppressNormalAttackTriggerInput;
    }

    static boolean shouldSuppressMainHandInput(
            boolean sendingInputs, boolean suppressNormalUseTriggerInput, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND && (sendingInputs || suppressNormalUseTriggerInput);
    }

    static boolean shouldQueueDeferredStandaloneMelee(
            boolean meleeHeld, boolean meleeJustPressed, boolean repeatMeleeEnabled) {
        if (meleeJustPressed) {
            return false;
        }

        return repeatMeleeEnabled || !meleeHeld;
    }

    private boolean isQuickMeleeLegal() {
        return QueuedMeleeScheduler.isMeleeReady();
    }

    private boolean shouldSuppressNormalAttackTriggerInput() {
        return isNormalAutoAttackTriggerActingAsSpellModifier() && Models.Character.getClassType() != ClassType.ARCHER;
    }

    private boolean shouldSuppressNormalUseTriggerInput() {
        return isNormalAutoAttackTriggerActingAsSpellModifier() && Models.Character.getClassType() == ClassType.ARCHER;
    }

    private boolean hasRawSpellKeyDown() {
        for (SpellBinding spellBinding : spellBindings) {
            if (spellBinding.keyBind().getKeyMapping().isDown()) {
                return true;
            }
        }

        return false;
    }

    private static List<CombatClickType> prependMelee(List<CombatClickType> clicks) {
        List<CombatClickType> combinedClicks = new ArrayList<>(clicks.size() + 1);
        combinedClicks.add(CombatClickType.MELEE);
        combinedClicks.addAll(clicks);
        return List.copyOf(combinedClicks);
    }

    private boolean canQuickCastNowSilently() {
        return QueuedMeleeScheduler.canQueueCombatInput(checkValidWeapon.get());
    }

    private QueuedMeleeScheduler.WeaponContext getWeaponContext(boolean notifyInvalidWeapon) {
        QueuedMeleeScheduler.WeaponContext weaponContext =
                QueuedMeleeScheduler.resolveWeaponContext(checkValidWeapon.get());
        if (!weaponContext.valid()) {
            if (weaponContext.invalidWeaponReason() == QueuedMeleeScheduler.InvalidWeaponReason.NOT_A_WEAPON) {
                sendCancelReason(notifyInvalidWeapon, Component.translatable("feature.wynntils.quickCast.notAWeapon"));
            } else if (weaponContext.invalidWeaponReason()
                    == QueuedMeleeScheduler.InvalidWeaponReason.REQUIREMENTS_UNMET) {
                sendCancelReason(
                        notifyInvalidWeapon, Component.translatable("feature.wynntils.quickCast.notMetRequirements"));
            }
        }

        return weaponContext;
    }

    private static void sendCancelReason(boolean notifyInvalidWeapon, MutableComponent reason) {
        if (!notifyInvalidWeapon) return;

        Managers.Notification.queueMessage(reason.withStyle(ChatFormatting.RED));
    }

    private record SpellBinding(KeyBind keyBind, List<CombatClickType> clicks) {}

    private record SpellSelection(int index, SpellBinding spellBinding, boolean notifyInvalidWeapon) {}
}
