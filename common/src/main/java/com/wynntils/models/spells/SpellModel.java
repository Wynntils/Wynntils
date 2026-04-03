/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.event.ActionBarRenderEvent;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.spells.actionbar.matchers.SpellCastSegmentMatcher;
import com.wynntils.models.spells.actionbar.matchers.SpellInputsSegmentMatcher;
import com.wynntils.models.spells.actionbar.matchers.UltimateTypeSegmentMatcher;
import com.wynntils.models.spells.actionbar.segments.SpellCastSegment;
import com.wynntils.models.spells.actionbar.segments.SpellInputsSegment;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.models.spells.type.SpellFailureReason;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.StyledTextUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import net.neoforged.bus.api.SubscribeEvent;

public final class SpellModel extends Model {
    public static final int SPELL_COST_RESET_TICKS = 60;

    private static final Queue<SpellDirection> SPELL_PACKET_QUEUE = new LinkedList<>();

    private final Set<Class<? extends ActionBarSegment>> hiddenSegments = new HashSet<>();

    private SpellDirection[] lastSpell = SpellDirection.NO_SPELL;
    private String lastBurstSpellName = "";
    private String lastSpellName = "";
    private int repeatedBurstSpellCount = 0;
    private int repeatedSpellCount = 0;
    private int ticksSinceCastBurst = 0;
    private int ticksSinceCast = 0;

    private boolean expireNextClear = false;
    // This keeps track of if the spell cast text is currently displayed so that we don't send multiple events
    private boolean spellTextActive = false;

    private SpellFailureReason failureReason = null;

    public SpellModel() {
        super(List.of());

        Handlers.ActionBar.registerSegment(new SpellInputsSegmentMatcher());
        Handlers.ActionBar.registerSegment(new SpellCastSegmentMatcher());
        Handlers.ActionBar.registerSegment(new UltimateTypeSegmentMatcher());
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresentOrElse(SpellInputsSegment.class, this::updateFromSpellSegment, this::handleExpiredSpell);
        event.runIfPresentOrElse(SpellCastSegment.class, this::handleSpellCast, this::spellCastExpire);
    }

    @SubscribeEvent
    public void onActionBarRender(ActionBarRenderEvent event) {
        hiddenSegments.forEach(segment -> event.setSegmentEnabled(segment, false));
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent.Match e) {
        StyledText message = StyledTextUtils.unwrap(e.getMessage()).stripAlignment();

        failureReason = SpellFailureReason.fromMsg(message);
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

    public void setHideSpellInputs(boolean shouldHide) {
        if (shouldHide) {
            hiddenSegments.add(SpellInputsSegment.class);
        } else {
            hiddenSegments.remove(SpellInputsSegment.class);
        }
    }

    public void setHideSpellCasts(boolean shouldHide) {
        if (shouldHide) {
            hiddenSegments.add(SpellCastSegment.class);
        } else {
            hiddenSegments.remove(SpellCastSegment.class);
        }
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

    private void updateFromSpellSegment(SpellInputsSegment spellInputsSegment) {
        // noop if the spell state hasn't changed
        if (Arrays.equals(spellInputsSegment.getDirections(), lastSpell)) return;
        lastSpell = spellInputsSegment.getDirections();

        WynntilsMod.postEvent(new SpellEvent.Partial(lastSpell));

        if (lastSpell.length == 3) {
            if (failureReason != null) {
                WynntilsMod.postEvent(new SpellEvent.Failed(failureReason));
                failureReason = null;
            } else {
                WynntilsMod.postEvent(
                        new SpellEvent.Completed(lastSpell, SpellType.fromSpellDirectionArray(lastSpell)));
            }
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

    private void handleSpellCast(SpellCastSegment spellCastSegment) {
        if (spellTextActive) return;

        spellTextActive = true;
        WynntilsMod.postEvent(new SpellEvent.Cast(
                spellCastSegment.getSpellType(), spellCastSegment.getManaCost(), spellCastSegment.getHealthCost()));
    }

    private void spellCastExpire() {
        if (!spellTextActive) return;

        spellTextActive = false;
        WynntilsMod.postEvent(new SpellEvent.CastExpired());
    }
}
