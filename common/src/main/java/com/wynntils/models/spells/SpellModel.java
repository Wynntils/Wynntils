/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.event.ItemRenamedEvent;
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.models.character.CharacterModel;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.actionbar.SpellSegment;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.spells.event.SpellSegmentUpdateEvent;
import com.wynntils.models.spells.type.PartialSpellSource;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.models.spells.type.SpellFailureReason;
import com.wynntils.models.spells.type.SpellType;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.EventPriority;
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
    private SpellDirection[] spellPrediction = SpellDirection.NO_SPELL;
    private Instant lastSpellPrediction = Instant.EPOCH;

    public SpellModel(CharacterModel characterModel) {
        super(List.of(characterModel));

        Handlers.ActionBar.registerSegment(spellSegment);
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

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onSwing(ArmSwingEvent event) {
        addSpellPrediction(SpellDirection.LEFT);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onUse(UseItemEvent event) {
        addSpellPrediction(SpellDirection.RIGHT);
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

    public void setSpellPrediction(SpellDirection[] spellPrediction) {
        this.spellPrediction = spellPrediction;
        lastSpellPrediction = Instant.now();
    }

    public SpellDirection[] getSpellPrediction() {
        // We should only ever have one empty SpellDirection array
        if (spellPrediction == SpellDirection.NO_SPELL) return spellPrediction;
        if (Duration.between(lastSpellPrediction, Instant.now()).toSeconds() > 4)
            return spellPrediction = SpellDirection.NO_SPELL;
        return spellPrediction;
    }

    public void addSpellPrediction(SpellDirection spellDirection) {
        // We should only ever have one empty SpellDirection array
        SpellDirection[] spellPrediction = getSpellPrediction();
        if (spellPrediction == SpellDirection.NO_SPELL) {
            if (spellDirection
                    != (Models.Character.getClassType() == ClassType.ARCHER
                            ? SpellDirection.LEFT
                            : SpellDirection.RIGHT)) return;
            setSpellPrediction(new SpellDirection[] {spellDirection, null, null});
            return;
        }

        if (spellPrediction[2] != null) {
            setSpellPrediction(SpellDirection.NO_SPELL);
            return;
        }

        if (spellPrediction[1] != null) {
            spellPrediction[2] = spellDirection;
            setSpellPrediction(spellPrediction);
            return;
        }

        spellPrediction[1] = spellDirection;
        setSpellPrediction(spellPrediction);
    }
}
