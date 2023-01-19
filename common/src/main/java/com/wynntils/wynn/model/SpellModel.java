/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.event.SpellCastEvent;
import com.wynntils.wynn.event.SpellEvent;
import com.wynntils.wynn.event.SpellProgressEvent;
import com.wynntils.wynn.model.actionbar.event.SpellSegmentUpdateEvent;
import com.wynntils.wynn.objects.SpellDirection;
import com.wynntils.wynn.objects.SpellType;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpellModel extends Model {
    private static final Pattern SPELL_TITLE_PATTERN =
            StringUtils.compileCCRegex("§([LR]|Right|Left)§-§([LR?]|Right|Left)§-§([LR?]|Right|Left)§");

    @SubscribeEvent
    public void onSpellSegmentUpdate(SpellSegmentUpdateEvent e) {
        Matcher matcher = e.getMatcher();
        if (!matcher.matches()) return;

        SpellDirection[] spell = getSpellFromMatcher(e.getMatcher());

        WynntilsMod.postEvent(new SpellProgressEvent(spell, SpellEvent.Source.HOTBAR));

        if (!matcher.group(3).equals("?")) {
            WynntilsMod.postEvent(new SpellCastEvent(spell, SpellEvent.Source.HOTBAR, SpellType.fromSpellDirectionArray(spell)));
        }
    }

    @SubscribeEvent
    public void onSubtitleSetText(SubtitleSetTextEvent e) {
        Matcher matcher = SPELL_TITLE_PATTERN.matcher(e.getComponent().getString());
        if (!matcher.matches()) return;

        SpellDirection[] spell = getSpellFromMatcher(matcher);
        // This check looks for the "t" in Right and Left, that do not exist in L and R, to set the source
        SpellEvent.Source source = (matcher.group(1).endsWith("t")) ? SpellEvent.Source.TITLE_FULL : SpellEvent.Source.TITLE_LETTER;

        WynntilsMod.postEvent(new SpellProgressEvent(spell, source));

        if (!matcher.group(3).equals("?")) {
            WynntilsMod.postEvent(new SpellCastEvent(spell, source, SpellType.fromSpellDirectionArray(spell)));
        }
    }

    private static SpellDirection[] getSpellFromMatcher(MatchResult spellMatcher) {
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
}
