/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.event.ItemRenamedEvent;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.models.spells.actionbar.SpellSegment;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.spells.event.SpellSegmentUpdateEvent;
import com.wynntils.models.spells.type.PartialSpellSource;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.models.spells.type.SpellFailureReason;
import com.wynntils.models.spells.type.SpellType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpellModel extends Model {
    // If you modify please test with link below
    // If you pass the tests and it still doesn't work, please resync tests with the game and update the link here
    // https://regexr.com/76ijo
    private static final Pattern SPELL_TITLE_PATTERN = Pattern.compile(
            "§a([LR]|Right|Left)§7-§[a7](?:§n)?([LR?]|Right|Left)§7-§r§[a7](?:§n)?([LR?]|Right|Left)§r");
    private static final Pattern SPELL_CAST = Pattern.compile("^§7(.*) spell cast! §3\\[§b-([0-9]+) ✺§3\\]$");

    private final SpellSegment spellSegment = new SpellSegment();

    private SpellDirection[] lastSpell = SpellDirection.NO_SPELL;

    public SpellModel() {
        super(List.of());

        Handlers.ActionBar.registerSegment(spellSegment);
        Handlers.Item.registerKnownMarkerNames(getKnownMarkerNames());
    }

    private List<Pattern> getKnownMarkerNames() {
        List<Pattern> knownMarkerNames = new ArrayList<>();
        knownMarkerNames.add(SPELL_CAST);
        knownMarkerNames.addAll(Arrays.stream(SpellFailureReason.values())
                .map(s -> Pattern.compile(s.getMessage().getString()))
                .toList());
        return knownMarkerNames;
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onItemRenamed(ItemRenamedEvent event) {
        StyledText msg = event.getNewName();
        SpellFailureReason failureReason = SpellFailureReason.fromMsg(msg);
        if (failureReason != null) {
            WynntilsMod.postEvent(new SpellEvent.Failed(failureReason));
            return;
        }

        Matcher spellMatcher = msg.getMatcher(SPELL_CAST);
        if (spellMatcher.matches()) {
            SpellType spellType = SpellType.fromName(spellMatcher.group(1));
            int manaCost = Integer.parseInt(spellMatcher.group(2));
            WynntilsMod.postEvent(new SpellEvent.Cast(spellType, manaCost));
        }
    }

    @SubscribeEvent
    public void onSpellSegmentUpdate(SpellSegmentUpdateEvent e) {
        Matcher matcher = e.getMatcher();
        if (!matcher.matches()) return;

        SpellDirection[] spell = getSpellFromMatcher(e.getMatcher());
        if (Arrays.equals(spell, lastSpell)) return; // Wynn sometimes sends duplicate packets, skip those
        lastSpell = spell;

        WynntilsMod.postEvent(new SpellEvent.Partial(spell, PartialSpellSource.HOTBAR));

        if (!matcher.group(3).equals("?")) {
            WynntilsMod.postEvent(new SpellEvent.Completed(
                    spell, PartialSpellSource.HOTBAR, SpellType.fromSpellDirectionArray(spell)));
        }
    }

    @SubscribeEvent
    public void onSubtitleSetText(SubtitleSetTextEvent e) {
        Matcher matcher = SPELL_TITLE_PATTERN.matcher(e.getComponent().getString());
        if (!matcher.matches()) return;

        SpellDirection[] spell = getSpellFromMatcher(matcher);
        if (Arrays.equals(spell, lastSpell)) return; // Wynn sometimes sends duplicate packets, skip those
        lastSpell = spell;

        // This check looks for the "t" in Right and Left, that do not exist in L and R, to set the source
        PartialSpellSource partialSpellSource =
                (matcher.group(1).endsWith("t")) ? PartialSpellSource.TITLE_FULL : PartialSpellSource.TITLE_LETTER;

        WynntilsMod.postEvent(new SpellEvent.Partial(spell, partialSpellSource));

        if (!matcher.group(3).equals("?")) {
            WynntilsMod.postEvent(
                    new SpellEvent.Completed(spell, partialSpellSource, SpellType.fromSpellDirectionArray(spell)));
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
