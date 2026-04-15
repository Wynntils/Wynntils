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
import java.util.EnumMap;
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

    private final EnumMap<SpellType, Integer> ticksSinceSpecificSpellMap = new EnumMap<>(SpellType.class);

    private SpellDirection[] lastSpell = SpellDirection.NO_SPELL;
    private String lastBurstSpellName = "";
    private String lastSpellName = "";
    private int repeatedBurstSpellCount = 0;
    private int repeatedSpellCount = 0;
    private int ticksSinceCastBurst = 0;
    private int ticksSinceCast = 0;
    private int ticksSinceSpellInputActivity = SPELL_COST_RESET_TICKS;

    private boolean expireNextClear = false;
    private boolean ignoreSpellInputsUntilClear = false;
    private boolean spellInputsActive = false;
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
        event.runIfPresentOrElse(
                SpellInputsSegment.class,
                spellInputsSegment -> updateFromSpellSegment(spellInputsSegment.getDirections()),
                this::handleExpiredSpell);
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
        ticksSinceSpecificSpellMap.put(e.getSpellType(), 0);
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if (!lastBurstSpellName.isEmpty()) {
            ticksSinceCastBurst++;
        }
        if (!lastSpellName.isEmpty()) {
            ticksSinceCast++;
        }
        if (spellInputsActive && ticksSinceSpellInputActivity < SPELL_COST_RESET_TICKS) {
            ticksSinceSpellInputActivity++;
            if (ticksSinceSpellInputActivity >= SPELL_COST_RESET_TICKS) {
                spellInputsActive = false;
            }
        }

        if (ticksSinceCastBurst >= SPELL_COST_RESET_TICKS) {
            lastBurstSpellName = "";
            repeatedBurstSpellCount = 0;
            ticksSinceCastBurst = 0;
        }
        ticksSinceSpecificSpellMap.replaceAll((number, tick) -> tick + 1);
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
        ticksSinceSpecificSpellMap.clear();
        clearSpellInputActivity();
        ignoreSpellInputsUntilClear = true;
    }

    @SubscribeEvent
    public void onHeldItemChange(ChangeCarriedItemEvent event) {
        clearSpellInputsForHeldItemChange();
    }

    public void addSpellToQueue(List<SpellDirection> spell) {
        if (!SPELL_PACKET_QUEUE.isEmpty()) return;

        SPELL_PACKET_QUEUE.addAll(spell);
    }

    public SpellDirection checkNextSpellDirection() {
        return SPELL_PACKET_QUEUE.peek();
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

    public boolean hasActiveSpellInputs() {
        return spellInputsActive;
    }

    public boolean isSpellCastActive() {
        return spellTextActive;
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

    public int getTicksSinceCast(String name) {
        return ticksSinceSpecificSpellMap.getOrDefault(SpellType.fromName(name), -1);
    }

    private void updateFromSpellSegment(SpellDirection[] directions) {
        if (ignoreSpellInputsUntilClear) {
            if (directions.length == 0) {
                ignoreSpellInputsUntilClear = false;
                if (expireNextClear) {
                    expireNextClear = false;
                    WynntilsMod.postEvent(new SpellEvent.Expired());
                }
            }
            clearSpellInputActivity();
            return;
        }

        spellInputsActive = directions.length > 0;
        ticksSinceSpellInputActivity = spellInputsActive ? 0 : SPELL_COST_RESET_TICKS;

        // noop if the spell state hasn't changed
        if (Arrays.equals(directions, lastSpell)) return;
        lastSpell = directions;

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
        ignoreSpellInputsUntilClear = false;
        clearSpellInputActivity();

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
        updateFromSpellSegment(spellCastSegment.getSpellType().getSpellDirectionArray());
        WynntilsMod.postEvent(new SpellEvent.Cast(
                spellCastSegment.getSpellType(), spellCastSegment.getManaCost(), spellCastSegment.getHealthCost()));
    }

    private void spellCastExpire() {
        if (!spellTextActive) return;

        spellTextActive = false;
        WynntilsMod.postEvent(new SpellEvent.CastExpired());
    }

    private void clearSpellInputsForHeldItemChange() {
        // The actual input state is cleared immediately, but the action bar may still show stale inputs
        // until the next packet, so keep the deferred Expired event behavior.
        lastSpell = SpellDirection.NO_SPELL;
        clearSpellInputActivity();
        expireNextClear = true;
        ignoreSpellInputsUntilClear = true;
    }

    private void clearSpellInputActivity() {
        spellInputsActive = false;
        ticksSinceSpellInputActivity = SPELL_COST_RESET_TICKS;
    }
}
