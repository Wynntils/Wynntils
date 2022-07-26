/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.event.TitleSetTextEvent;
import com.wynntils.wc.event.WorldStateEvent;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpellManager {
    private static final Pattern SPELL_PATTERN = Pattern.compile(
            "§([LR]|Right|Left)§-§([LR?]|Right|Left)§-§([LR?]|Right|Left)§".replace("§", "(?:§[0-9a-fklmnor])*"));
    public static final SpellUnit[] NO_SPELL = new SpellUnit[0];
    private static SpellUnit[] lastSpell = NO_SPELL;
    private static int spellCountdown = 0;

    private static SpellUnit[] getSpellFromString(String string) {
        Matcher spellMatcher = SPELL_PATTERN.matcher(string);
        if (!spellMatcher.matches()) return null;

        int size = 1;
        for (; size < 3; ++size) {
            if (spellMatcher.group(size + 1).equals("?")) break;
        }

        SpellUnit[] spell = new SpellUnit[size];
        for (int i = 0; i < size; ++i) {
            spell[i] = spellMatcher.group(i + 1).charAt(0) == 'R' ? SpellUnit.RIGHT : SpellUnit.LEFT;
        }

        return spell;
    }

    public static void tryUpdateSpell(String text) {
        SpellUnit[] spell = getSpellFromString(text);
        if (spell == null) return;
        if (Arrays.equals(lastSpell, spell)) return;
        if (spell.length == 3) {
            lastSpell = NO_SPELL;
            spellCountdown = 0;
        } else {
            lastSpell = spell;
            spellCountdown = 40;
        }
    }

    @SubscribeEvent
    public static void onTitleUpdate(TitleSetTextEvent e) {
        if (!WynnUtils.onWorld()) return;
        tryUpdateSpell(e.getComponent().getString());
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent e) {
        if (!WynnUtils.onWorld() || e.getTickPhase() != ClientTickEvent.Phase.END) return;
        if (spellCountdown <= 0 || --spellCountdown > 0) return;
        lastSpell = NO_SPELL;
    }

    @SubscribeEvent
    public static void onWorldChange(WorldStateEvent e) {
        lastSpell = NO_SPELL;
    }

    public static void init() {
        WynntilsMod.getEventBus().register(SpellManager.class);
    }

    public static SpellUnit[] getLastSpell() {
        return lastSpell;
    }

    public enum SpellUnit {
        RIGHT,
        LEFT
    }
}
