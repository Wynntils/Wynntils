/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.WynnItemMatchers;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.COMBAT)
public class QuickCastFeature extends Feature {
    @RegisterKeyBind
    private final KeyBind castFirstSpell = new KeyBind("Cast 1st Spell", GLFW.GLFW_KEY_Z, true, this::castFirstSpell);

    @RegisterKeyBind
    private final KeyBind castSecondSpell = new KeyBind("Cast 2nd Spell", GLFW.GLFW_KEY_X, true, this::castSecondSpell);

    @RegisterKeyBind
    private final KeyBind castThirdSpell = new KeyBind("Cast 3rd Spell", GLFW.GLFW_KEY_C, true, this::castThirdSpell);

    @RegisterKeyBind
    private final KeyBind castFourthSpell = new KeyBind("Cast 4th Spell", GLFW.GLFW_KEY_V, true, this::castFourthSpell);

    @RegisterConfig
    private final Config<Integer> rightClickCooldown = new Config<>(3);

    @RegisterConfig
    private final Config<Boolean> safeCasting = new Config<>(true);

    @RegisterConfig
    private final Config<Integer> spellCooldown = new Config<>(2);

    private static final Pattern INCORRECT_CLASS_PATTERN = compileCCRegex("§✖§ Class Req: (.+)");
    private static final Pattern LVL_MIN_NOT_REACHED_PATTERN = compileCCRegex("§✖§ (.+) Min: ([0-9]+)");

    private static final Queue<SpellDirection> SPELL_PACKET_QUEUE = new LinkedList<>();

    private final Queue<SpellDirection> currentSpell = new LinkedList<>();
    private final Queue<Spell> spells = new LinkedList<>();
    private Spell spell = null;
    private int packetCountdown = 0;
    private int spellCountdown = 0;
    private int lastSelectedSlot = 0;

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
        ItemStack heldItem = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);

        if (!WynnItemMatchers.isWeapon(heldItem)) {
            sendCancelReason(Component.translatable("feature.wynntils.quickCast.notAWeapon"));
            return;
        }

        List<StyledText> loreLines = LoreUtils.getLore(heldItem);

        for (StyledText lore : loreLines) {
            Matcher matcher = lore.getMatcher(INCORRECT_CLASS_PATTERN);
            if (!matcher.matches()) continue;
            sendCancelReason(Component.translatable("feature.wynntils.quickCast.classMismatch", matcher.group(1)));
            return;
        }

        for (StyledText lore : loreLines) {
            Matcher matcher = lore.getMatcher(LVL_MIN_NOT_REACHED_PATTERN);
            if (!matcher.matches()) continue;
            sendCancelReason(Component.translatable(
                    "feature.wynntils.quickCast.levelRequirementNotReached", matcher.group(1), matcher.group(2)));
            return;
        }

        Spell spell = new Spell(b, c, Models.Character.getClassType() == ClassType.ARCHER);
        if (spells.contains(spell)) {
            return;
        }

        spells.offer(spell);
        lastSelectedSlot = McUtils.inventory().selected;
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if (--packetCountdown > 0) return;
        if (currentSpell.isEmpty() && !pollSpell()) return;

        int currSelectedSlot = McUtils.inventory().selected;
        boolean slotChanged = currSelectedSlot != lastSelectedSlot;
        if (slotChanged) McUtils.sendPacket(new ServerboundSetCarriedItemPacket(lastSelectedSlot));
        if (safeCasting.get()) {
            currentSpell.poll().getSendPacketRunnable().run();
        } else {
            do {
                currentSpell.peek().getSendPacketRunnable().run();
            } while (currentSpell.poll() == SpellDirection.LEFT && !currentSpell.isEmpty());
            while (currentSpell.peek() == SpellDirection.LEFT) {
                currentSpell.poll().getSendPacketRunnable().run();
            }
        }
        if (slotChanged) McUtils.sendPacket(new ServerboundSetCarriedItemPacket(currSelectedSlot));

        // Waiting a few ticks is useful for avoiding lag related input-overlaps
        packetCountdown = Math.max(1, rightClickCooldown.get());
    }

    private boolean pollSpell() {
        spells.remove(spell);
        spell = null;
        if (--spellCountdown > 0) return false;
        if (spells.isEmpty()) return false;
        SpellDirection[] progress = Models.Spell.getLastSpell();
        SpellDirection[] currentProgress =
                progress.length != 3 && Models.Spell.isLastSpellStillValid() ? progress : SpellDirection.NO_SPELL;
        Optional<Spell> first = spells.stream()
                .filter(s -> s.poll(currentSpell, currentProgress))
                .findFirst();
        if (first.isEmpty()) return false;
        spell = first.get();
        spellCountdown = spellCooldown.get();
        return true;
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        currentSpell.clear();
        spells.clear();
        spell = null;
    }

    private static void sendCancelReason(MutableComponent reason) {
        Managers.Notification.queueMessage(reason.withStyle(ChatFormatting.RED));
    }

    private static Pattern compileCCRegex(String regex) {
        return Pattern.compile(regex.replace("§", "(?:§[0-9a-fklmnor])*"));
    }

    public enum SpellUnit {
        PRIMARY,
        SECONDARY;

        public SpellDirection toSpellDirection(boolean isInverted) {
            return (this == PRIMARY) != isInverted ? SpellDirection.RIGHT : SpellDirection.LEFT;
        }
    }

    private record Spell(SpellUnit a, SpellUnit b, boolean isInverted) {
        private boolean poll(Queue<SpellDirection> queue, SpellDirection[] current) {
            List<SpellDirection> spellDirection = List.of(
                    SpellUnit.PRIMARY.toSpellDirection(isInverted),
                    a.toSpellDirection(isInverted),
                    b.toSpellDirection(isInverted));
            for (int i = 0; i < current.length; i++) {
                if (current[i] != spellDirection.get(i)) return false;
            }
            queue.addAll(spellDirection.subList(current.length, spellDirection.size()));
            return true;
        }
    }
}
