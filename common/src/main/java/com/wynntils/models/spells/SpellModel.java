/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.event.ActionBarRenderEvent;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.handlers.item.event.ItemRenamedEvent;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.spells.actionbar.matchers.SpellSegmentMatcher;
import com.wynntils.models.spells.actionbar.segments.SpellSegment;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.models.spells.type.SpellFailureReason;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.neoforged.bus.api.SubscribeEvent;

public final class SpellModel extends Model {
    private static final Pattern SPELL_CAST =
            Pattern.compile("^§7(.*) spell cast! §3\\[§b-([0-9]+) ✺§3\\](?: §4\\[§c-([0-9]+) ❤§4\\])?$");
    public static final int SPELL_COST_RESET_TICKS = 60;

    private static final Queue<SpellDirection> SPELL_PACKET_QUEUE = new LinkedList<>();

    private boolean hideSpellInputs = false;

    private SpellDirection[] lastSpell = SpellDirection.NO_SPELL;
    private String lastBurstSpellName = "";
    private String lastSpellName = "";
    private int repeatedBurstSpellCount = 0;
    private int repeatedSpellCount = 0;
    private int ticksSinceCastBurst = 0;
    private int ticksSinceCast = 0;

    private boolean expireNextClear = false;

    public SpellModel() {
        super(List.of());

        Handlers.ActionBar.registerSegment(new SpellSegmentMatcher());
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
            int healthCost =
                    Integer.parseInt(Optional.ofNullable(spellMatcher.group(3)).orElse("0"));
            WynntilsMod.postEvent(new SpellEvent.Cast(spellType, manaCost, healthCost));
        }
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresentOrElse(SpellSegment.class, this::updateFromSpellSegment, this::handleExpiredSpell);
    }

    @SubscribeEvent
    public void onActionBarRender(ActionBarRenderEvent event) {
        if (hideSpellInputs) {
            event.setSegmentEnabled(SpellSegment.class, false);
        }
    }

    @SubscribeEvent
    public void onSpellCast(SpellEvent.Cast e) {
        ticksSinceCastBurst = 0;
        ticksSinceCast = 0;

        if (e.getSpellType().getName().equals(lastBurstSpellName)) {
            repeatedBurstSpellCount++;
        } else {
            repeatedBurstSpellCount = 1;
        }
        if (e.getSpellType().getName().equals(lastSpellName)) {
            repeatedSpellCount++;
        } else {
            repeatedSpellCount = 1;
        }

        lastBurstSpellName = e.getSpellType().getName();
        lastSpellName = e.getSpellType().getName();
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if (!lastBurstSpellName.isEmpty()) {
            ticksSinceCastBurst++;
        }
        if (!lastSpellName.isEmpty()) {
            ticksSinceCast++;
        }

        if (ticksSinceCastBurst >= SPELL_COST_RESET_TICKS) {
            lastBurstSpellName = "";
            repeatedBurstSpellCount = 0;
            ticksSinceCastBurst = 0;
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent e) {
        SPELL_PACKET_QUEUE.clear();
        lastSpell = SpellDirection.NO_SPELL;
        lastBurstSpellName = "";
        lastSpellName = "";
        repeatedBurstSpellCount = 0;
        repeatedSpellCount = 0;
        ticksSinceCastBurst = 0;
        ticksSinceCast = 0;
    }

    @SubscribeEvent
    public void onHeldItemChange(ChangeCarriedItemEvent event) {
        SPELL_PACKET_QUEUE.clear();
        // We need to reset lastSpell here as the actual inputs are now cleared, but they are still visible
        // so we don't post the expired event until the action bar has actually updated with the cleared inputs
        lastSpell = SpellDirection.NO_SPELL;
        expireNextClear = true;
    }

    public void addSpellToQueue(List<SpellDirection> spell) {
        if (!SPELL_PACKET_QUEUE.isEmpty()) return;

        SPELL_PACKET_QUEUE.addAll(spell);
    }

    public SpellDirection checkNextSpellDirection() {
        return SPELL_PACKET_QUEUE.peek();
    }

    public void sendNextSpell() {
        if (SPELL_PACKET_QUEUE.isEmpty()) return;

        SpellDirection spellDirection = SPELL_PACKET_QUEUE.poll();
        spellDirection.getSendPacketRunnable().run();
    }

    public void setHideSpellInputs(boolean hideSpellInputs) {
        this.hideSpellInputs = hideSpellInputs;
    }

    public boolean isSpellQueueEmpty() {
        return SPELL_PACKET_QUEUE.isEmpty();
    }

    public String getLastBurstSpellName() {
        return lastBurstSpellName;
    }

    public String getLastSpellName() {
        return lastSpellName;
    }

    public SpellDirection[] getLastSpell() {
        return lastSpell.clone();
    }

    public int getRepeatedBurstSpellCount() {
        return repeatedBurstSpellCount;
    }

    public int getRepeatedSpellCount() {
        return repeatedSpellCount;
    }

    public int getTicksSinceCastBurst() {
        return ticksSinceCastBurst;
    }

    public int getTicksSinceCast() {
        return ticksSinceCast;
    }

    private void updateFromSpellSegment(SpellSegment spellSegment) {
        // noop if the spell state hasn't changed
        if (Arrays.equals(spellSegment.getDirections(), lastSpell)) return;
        lastSpell = spellSegment.getDirections();

        WynntilsMod.postEvent(new SpellEvent.Partial(lastSpell));

        if (lastSpell.length == 3) {
            WynntilsMod.postEvent(new SpellEvent.Completed(lastSpell, SpellType.fromSpellDirectionArray(lastSpell)));
        }
    }

    private void handleExpiredSpell() {
        if (lastSpell.length != 0) {
            if (lastSpell.length != 3) {
                lastSpell = SpellDirection.NO_SPELL;
            }
            WynntilsMod.postEvent(new SpellEvent.Expired());
        } else if (expireNextClear) {
            expireNextClear = false;
            WynntilsMod.postEvent(new SpellEvent.Expired());
        }
    }
}
