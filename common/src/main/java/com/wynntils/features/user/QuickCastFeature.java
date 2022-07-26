/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyHolder;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.event.WorldStateEvent;
import com.wynntils.wc.utils.SpellManager;
import com.wynntils.wc.utils.WynnItemMatchers;
import com.wynntils.wc.utils.WynnUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
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
    private final KeyHolder castFirstSpell =
            new KeyHolder("Cast 1st Spell", GLFW.GLFW_KEY_Z, "Wynntils", true, QuickCastFeature::castFirstSpell);

    @RegisterKeyBind
    private final KeyHolder castSecondSpell =
            new KeyHolder("Cast 2nd Spell", GLFW.GLFW_KEY_X, "Wynntils", true, QuickCastFeature::castSecondSpell);

    @RegisterKeyBind
    private final KeyHolder castThirdSpell =
            new KeyHolder("Cast 3rd Spell", GLFW.GLFW_KEY_C, "Wynntils", true, QuickCastFeature::castThirdSpell);

    @RegisterKeyBind
    private final KeyHolder castFourthSpell =
            new KeyHolder("Cast 4th Spell", GLFW.GLFW_KEY_V, "Wynntils", true, QuickCastFeature::castFourthSpell);

    private static final Pattern INCORRECT_CLASS_PATTERN =
            Pattern.compile("§✖§ Class Req: (.+)".replace("§", "(?:§[0-9a-fklmnor])*"));
    private static final Pattern LVL_MIN_NOT_REACHED_PATTERN =
            Pattern.compile("§✖§ (.+) Min: ([0-9]+)".replace("§", "(?:§[0-9a-fklmnor])*"));
    private static final Packet<?> RIGHT_CLICK_PACKET = new ServerboundUseItemPacket(InteractionHand.MAIN_HAND);
    private static final Packet<?> LEFT_CLICK_PACKET = new ServerboundSwingPacket(InteractionHand.MAIN_HAND);
    private static final Queue<Packet<?>> SPELL_PACKET_QUEUE = new LinkedList<>();
    private static final boolean RIGHT = true;
    private static final boolean LEFT = false;
    private static int lastSelectedSlot = 0;
    private static int packetCountdown = 0;

    private static void castFirstSpell() {
        tryCastSpell(RIGHT, LEFT, RIGHT);
    }

    private static void castSecondSpell() {
        tryCastSpell(RIGHT, RIGHT, RIGHT);
    }

    private static void castThirdSpell() {
        tryCastSpell(RIGHT, LEFT, LEFT);
    }

    private static void castFourthSpell() {
        tryCastSpell(RIGHT, RIGHT, LEFT);
    }

    private static void tryCastSpell(boolean a, boolean b, boolean c) {

        if (!SPELL_PACKET_QUEUE.isEmpty()) {
            McUtils.sendMessageToClient(new TranslatableComponent("feature.wynntils.quickCast.anotherInProgress")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        ItemStack heldItem = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);

        if (!WynnItemMatchers.isWeapon(heldItem)) {
            McUtils.sendMessageToClient(
                    new TranslatableComponent("feature.wynntils.quickCast.notAWeapon").withStyle(ChatFormatting.RED));
            return;
        }

        List<String> loreLines = ItemUtils.getLore(heldItem);

        boolean isArcher = false;
        for (String lore : loreLines) {
            Matcher matcher = INCORRECT_CLASS_PATTERN.matcher(lore);
            if (matcher.matches()) {
                McUtils.sendMessageToClient(
                        new TranslatableComponent("feature.wynntils.quickCast.classMismatch", matcher.group(1))
                                .withStyle(ChatFormatting.RED));
                return;
            }
            if (lore.contains("Archer/Hunter")) isArcher = true;
        }

        for (String lore : loreLines) {
            Matcher matcher = LVL_MIN_NOT_REACHED_PATTERN.matcher(lore);
            if (matcher.matches()) {
                McUtils.sendMessageToClient(new TranslatableComponent(
                                "feature.wynntils.quickCast.levelRequirementNotReached",
                                matcher.group(1),
                                matcher.group(2))
                        .withStyle(ChatFormatting.RED));
                return;
            }
        }

        boolean isSpellInverted = isArcher;
        List<Boolean> spell = Stream.of(a, b, c).map(x -> isSpellInverted != x).toList();
        boolean[] partialSpell = SpellManager.getLastSpell();
        for (int i = 0; i < partialSpell.length; ++i) {
            if (partialSpell[i] != spell.get(i)) {
                McUtils.sendMessageToClient(
                        new TranslatableComponent("feature.wynntils.quickCast.incompatibleInProgress")
                                .withStyle(ChatFormatting.RED));
                return;
            }
        }

        lastSelectedSlot = McUtils.inventory().selected;
        List<Boolean> remainder = spell.subList(partialSpell.length, spell.size());
        remainder.stream().map(x -> x ? RIGHT_CLICK_PACKET : LEFT_CLICK_PACKET).forEach(SPELL_PACKET_QUEUE::add);
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent e) {
        if (!WynnUtils.onWorld() || e.getTickPhase() != ClientTickEvent.Phase.END) return;
        if (SPELL_PACKET_QUEUE.isEmpty()) return;
        if (--packetCountdown > 0) return;
        int currSelectedSlot = McUtils.inventory().selected;
        boolean flag = currSelectedSlot != lastSelectedSlot;
        if (flag) McUtils.sendPacket(new ServerboundSetCarriedItemPacket(lastSelectedSlot));
        McUtils.sendPacket(SPELL_PACKET_QUEUE.poll());
        if (flag) McUtils.sendPacket(new ServerboundSetCarriedItemPacket(currSelectedSlot));
        if (!SPELL_PACKET_QUEUE.isEmpty()) packetCountdown = 3;
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        SPELL_PACKET_QUEUE.clear();
    }
}
