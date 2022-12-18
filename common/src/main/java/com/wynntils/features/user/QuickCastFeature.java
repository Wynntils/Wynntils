/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.event.ActionBarMessageUpdateEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = FeatureInfo.Stability.STABLE)
public class QuickCastFeature extends UserFeature {
    @RegisterKeyBind
    private final KeyBind castFirstSpell = new KeyBind("Cast 1st Spell", GLFW.GLFW_KEY_Z, true, this::castFirstSpell);

    @RegisterKeyBind
    private final KeyBind castSecondSpell = new KeyBind("Cast 2nd Spell", GLFW.GLFW_KEY_X, true, this::castSecondSpell);

    @RegisterKeyBind
    private final KeyBind castThirdSpell = new KeyBind("Cast 3rd Spell", GLFW.GLFW_KEY_C, true, this::castThirdSpell);

    @RegisterKeyBind
    private final KeyBind castFourthSpell = new KeyBind("Cast 4th Spell", GLFW.GLFW_KEY_V, true, this::castFourthSpell);

    private static final Pattern SPELL_PATTERN =
            StringUtils.compileCCRegex("§([LR]|Right|Left)§-§([LR?]|Right|Left)§-§([LR?]|Right|Left)§");
    private static final Pattern INCORRECT_CLASS_PATTERN = StringUtils.compileCCRegex("§✖§ Class Req: (.+)");
    private static final Pattern LVL_MIN_NOT_REACHED_PATTERN = StringUtils.compileCCRegex("§✖§ (.+) Min: ([0-9]+)");
    private static final SpellDirection[] NO_SPELL = new SpellDirection[0];

    private SpellDirection[] spellInProgress = NO_SPELL;

    private static final Queue<Packet<?>> SPELL_PACKET_QUEUE = new LinkedList<>();

    private int packetCountdown = 0;
    private int spellCountdown = 0;
    private int lastSelectedSlot = 0;

    @SubscribeEvent
    public void onSubtitleUpdate(SubtitleSetTextEvent e) {
        // only actually used when player is still low-level
        if (!WynnUtils.onWorld()) return;
        tryUpdateSpell(e.getComponent().getString());
    }

    @SubscribeEvent
    public void onActionbarMessageUpdate(ActionBarMessageUpdateEvent e) {
        if (!WynnUtils.onWorld()) return;
        tryUpdateSpell(e.getMessage());
    }

    private void tryUpdateSpell(String text) {
        SpellDirection[] spell = getSpellFromString(text);
        if (spell == null) return;
        if (Arrays.equals(spellInProgress, spell)) return;
        if (spell.length == 3) {
            spellInProgress = NO_SPELL;
            spellCountdown = 0;
        } else {
            spellInProgress = spell;
            spellCountdown = 40;
        }
    }

    private static SpellDirection[] getSpellFromString(String string) {
        Matcher spellMatcher = SPELL_PATTERN.matcher(string);
        if (!spellMatcher.matches()) return null;

        int size = 1;
        for (; size < 3; ++size) {
            if (spellMatcher.group(size + 1).equals("?")) break;
        }

        SpellDirection[] spell = new SpellDirection[size];
        for (int i = 0; i < size; ++i) {
            spell[i] = spellMatcher.group(i + 1).charAt(0) == 'R' ? SpellDirection.RIGHT : SpellDirection.LEFT;
        }

        return spell;
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
        if (!SPELL_PACKET_QUEUE.isEmpty()) {
            sendCancelReason(Component.translatable("feature.wynntils.quickCast.anotherInProgress"));
            return;
        }

        ItemStack heldItem = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);

        if (!WynnItemMatchers.isWeapon(heldItem)) {
            sendCancelReason(Component.translatable("feature.wynntils.quickCast.notAWeapon"));
            return;
        }

        List<String> loreLines = ItemUtils.getLore(heldItem);

        boolean isArcher = false;
        for (String lore : loreLines) {
            if (lore.contains("Archer/Hunter")) isArcher = true;
            Matcher matcher = INCORRECT_CLASS_PATTERN.matcher(lore);
            if (!matcher.matches()) continue;
            sendCancelReason(Component.translatable("feature.wynntils.quickCast.classMismatch", matcher.group(1)));
            return;
        }

        for (String lore : loreLines) {
            Matcher matcher = LVL_MIN_NOT_REACHED_PATTERN.matcher(lore);
            if (!matcher.matches()) continue;
            sendCancelReason(Component.translatable(
                    "feature.wynntils.quickCast.levelRequirementNotReached", matcher.group(1), matcher.group(2)));
            return;
        }

        boolean isSpellInverted = isArcher;
        List<SpellDirection> spell = Stream.of(a, b, c)
                .map(x -> (x == SpellUnit.PRIMARY) != isSpellInverted ? SpellDirection.RIGHT : SpellDirection.LEFT)
                .toList();
        for (int i = 0; i < spellInProgress.length; ++i) {
            if (spellInProgress[i] != spell.get(i)) {
                sendCancelReason(Component.translatable("feature.wynntils.quickCast.incompatibleInProgress"));
                return;
            }
        }

        lastSelectedSlot = McUtils.inventory().selected;
        List<SpellDirection> remainder = spell.subList(spellInProgress.length, spell.size());
        remainder.stream().map(SpellDirection::getInteractionPacket).forEach(SPELL_PACKET_QUEUE::add);
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.End e) {
        if (!WynnUtils.onWorld()) return;

        // Clear spell after the 40 tick timeout period
        if (spellCountdown > 0 && --spellCountdown <= 0) spellInProgress = NO_SPELL;

        if (SPELL_PACKET_QUEUE.isEmpty()) return;
        if (--packetCountdown > 0) return;

        int currSelectedSlot = McUtils.inventory().selected;
        boolean slotChanged = currSelectedSlot != lastSelectedSlot;

        if (slotChanged) McUtils.sendPacket(new ServerboundSetCarriedItemPacket(lastSelectedSlot));
        McUtils.sendPacket(SPELL_PACKET_QUEUE.poll());
        if (slotChanged) McUtils.sendPacket(new ServerboundSetCarriedItemPacket(currSelectedSlot));

        // Waiting a few ticks is useful for avoiding lag related input-overlaps
        if (!SPELL_PACKET_QUEUE.isEmpty()) packetCountdown = 3;
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        SPELL_PACKET_QUEUE.clear();
        spellInProgress = NO_SPELL;
    }

    private static void sendCancelReason(MutableComponent reason) {
        NotificationManager.queueMessage(reason.withStyle(ChatFormatting.RED));
    }

    public enum SpellUnit {
        PRIMARY,
        SECONDARY
    }

    public enum SpellDirection {
        RIGHT(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND)),
        LEFT(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));

        private final Packet<?> interactionPacket;

        SpellDirection(Packet<?> interactionPacket) {
            this.interactionPacket = interactionPacket;
        }

        public Packet<?> getInteractionPacket() {
            return interactionPacket;
        }
    }
}
