/*
 * Copyright © Wynntils 2022-2024.
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
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ItemUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
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
    private final Config<Integer> spellCooldown = new Config<>(0);

    private static final Queue<SpellDirection> SPELL_PACKET_QUEUE = new LinkedList<>();

    private int lastSpellTick = 0;
    private int packetCountdown = 0;

    @SubscribeEvent
    public void onSwing(ArmSwingEvent event) {
        if (!blockAttacks.get()) return;
        if (event.getActionContext() != ArmSwingEvent.ArmSwingContext.ATTACK_OR_START_BREAKING_BLOCK) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        event.setCanceled(!SPELL_PACKET_QUEUE.isEmpty());
    }

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        if (!blockAttacks.get()) return;

        event.setCanceled(!SPELL_PACKET_QUEUE.isEmpty());
    }

    @SubscribeEvent
    public void onHeldItemChange(ChangeCarriedItemEvent event) {
        SPELL_PACKET_QUEUE.clear();
        lastSpellTick = 0;
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
        if (!SPELL_PACKET_QUEUE.isEmpty()) return;

        boolean isArcher = Models.Character.getClassType() == ClassType.ARCHER;

        if (checkValidWeapon.get()) {
            ItemStack heldItem = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);

            if (!ItemUtils.isWeapon(heldItem)) {
                sendCancelReason(Component.translatable("feature.wynntils.quickCast.notAWeapon"));
                return;
            }

            Optional<GearItem> gearItemOpt = Models.Item.asWynnItem(heldItem, GearItem.class);

            if (gearItemOpt.isEmpty()) {
                sendCancelReason(Component.translatable("feature.wynntils.quickCast.notAWeapon"));
                return;
            } else if (!gearItemOpt.get().meetsActualRequirements()) {
                sendCancelReason(Component.translatable("feature.wynntils.quickCast.notMetRequirements"));
                return;
            }

            isArcher = gearItemOpt.get().getRequiredClass() == ClassType.ARCHER;
        }

        boolean isSpellInverted = isArcher;
        List<SpellDirection> spell = Stream.of(a, b, c)
                .map(x -> (x == SpellUnit.PRIMARY) != isSpellInverted ? SpellDirection.RIGHT : SpellDirection.LEFT)
                .toList();

        SPELL_PACKET_QUEUE.addAll(spell);
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if (!Models.WorldState.onWorld()) return;

        if (packetCountdown > 0) {
            packetCountdown--;
        }

        if (packetCountdown > 0) return;

        if (SPELL_PACKET_QUEUE.isEmpty()) return;

        int comparisonTime =
                SPELL_PACKET_QUEUE.peek() == SpellDirection.LEFT ? leftClickTickDelay.get() : rightClickTickDelay.get();
        if (McUtils.player().tickCount - lastSpellTick < comparisonTime) return;

        SpellDirection spellDirection = SPELL_PACKET_QUEUE.poll();
        spellDirection.getSendPacketRunnable().run();
        lastSpellTick = McUtils.player().tickCount;

        if (SPELL_PACKET_QUEUE.isEmpty()) {
            packetCountdown = Math.max(packetCountdown, spellCooldown.get());
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        SPELL_PACKET_QUEUE.clear();
        lastSpellTick = 0;
    }

    private static void sendCancelReason(MutableComponent reason) {
        Managers.Notification.queueMessage(reason.withStyle(ChatFormatting.RED));
    }

    public enum SpellUnit {
        PRIMARY,
        SECONDARY
    }
}
