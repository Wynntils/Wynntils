/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.spells.actionbar.segments.SpellCastSegment;
import com.wynntils.models.spells.actionbar.segments.SpellInputsSegment;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import java.util.List;
import net.neoforged.bus.api.SubscribeEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestSpellModel {
    private SpellLifecycleRecorder recorder;

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @BeforeEach
    public void beforeEach() {
        resetSpellModelState();

        recorder = new SpellLifecycleRecorder();
        WynntilsMod.registerEventListener(recorder);
    }

    @AfterEach
    public void afterEach() {
        WynntilsMod.unregisterEventListener(recorder);
        resetSpellModelState();
    }

    @Test
    public void heldItemChangeDoesNotResetActiveCastStateOrDuplicateCastEvents() {
        Models.Spell.onActionBarUpdate(castActionBarUpdate());

        Assertions.assertEquals(1, recorder.castCount);
        Assertions.assertTrue(Models.Spell.isSpellCastActive());

        Models.Spell.onHeldItemChange(new ChangeCarriedItemEvent());

        Assertions.assertTrue(Models.Spell.isSpellCastActive());

        Models.Spell.onActionBarUpdate(castActionBarUpdate());

        Assertions.assertEquals(1, recorder.castCount);
        Assertions.assertTrue(Models.Spell.isSpellCastActive());

        Models.Spell.onActionBarUpdate(emptyActionBarUpdate());

        Assertions.assertEquals(1, recorder.castExpiredCount);
        Assertions.assertFalse(Models.Spell.isSpellCastActive());
    }

    @Test
    public void worldChangeDoesNotPreventPendingCastExpire() {
        Models.Spell.onActionBarUpdate(castActionBarUpdate());

        Assertions.assertEquals(1, recorder.castCount);
        Assertions.assertTrue(Models.Spell.isSpellCastActive());

        Models.Spell.onWorldStateChange(new WorldStateEvent(WorldState.HUB, WorldState.WORLD, "test", false));

        Assertions.assertTrue(Models.Spell.isSpellCastActive());

        Models.Spell.onActionBarUpdate(emptyActionBarUpdate());

        Assertions.assertEquals(1, recorder.castExpiredCount);
        Assertions.assertFalse(Models.Spell.isSpellCastActive());
    }

    @Test
    public void heldItemChangeIgnoresStaleSpellInputSegmentsUntilTheyClear() {
        Models.Spell.onActionBarUpdate(spellInputsActionBarUpdate(SpellDirection.RIGHT));

        Assertions.assertEquals(1, recorder.partialCount);
        Assertions.assertTrue(Models.Spell.hasActiveSpellInputs());

        Models.Spell.onHeldItemChange(new ChangeCarriedItemEvent());

        Assertions.assertFalse(Models.Spell.hasActiveSpellInputs());

        Models.Spell.onActionBarUpdate(spellInputsActionBarUpdate(SpellDirection.RIGHT));

        Assertions.assertEquals(1, recorder.partialCount);
        Assertions.assertFalse(Models.Spell.hasActiveSpellInputs());

        Models.Spell.onActionBarUpdate(emptyActionBarUpdate());

        Assertions.assertEquals(1, recorder.expiredCount);
    }

    @Test
    public void spellInputsActiveExpiresWithoutActionBarUpdates() {
        Models.Spell.onActionBarUpdate(spellInputsActionBarUpdate(SpellDirection.RIGHT));

        Assertions.assertTrue(Models.Spell.hasActiveSpellInputs());

        for (int i = 0; i < SpellModel.SPELL_COST_RESET_TICKS - 1; i++) {
            Models.Spell.onTick(new TickEvent());
        }

        Assertions.assertTrue(Models.Spell.hasActiveSpellInputs());

        Models.Spell.onTick(new TickEvent());

        Assertions.assertFalse(Models.Spell.hasActiveSpellInputs());
    }

    private static void resetSpellModelState() {
        Models.Spell.onActionBarUpdate(emptyActionBarUpdate());
        Models.Spell.onWorldStateChange(new WorldStateEvent(WorldState.HUB, WorldState.WORLD, "test", false));
        Models.Spell.onActionBarUpdate(emptyActionBarUpdate());
    }

    private static ActionBarUpdatedEvent castActionBarUpdate() {
        return new ActionBarUpdatedEvent(List.of(new SpellCastSegment("cast", 0, 4, SpellType.FIRST_SPELL, 0, 0)));
    }

    private static ActionBarUpdatedEvent spellInputsActionBarUpdate(SpellDirection... directions) {
        return new ActionBarUpdatedEvent(List.of(new SpellInputsSegment("inputs", 0, 6, directions)));
    }

    private static ActionBarUpdatedEvent emptyActionBarUpdate() {
        return new ActionBarUpdatedEvent(List.of());
    }

    private static final class SpellLifecycleRecorder {
        private int castCount = 0;
        private int castExpiredCount = 0;
        private int expiredCount = 0;
        private int partialCount = 0;

        @SubscribeEvent
        public void onSpellCast(SpellEvent.Cast event) {
            castCount++;
        }

        @SubscribeEvent
        public void onSpellCastExpired(SpellEvent.CastExpired event) {
            castExpiredCount++;
        }

        @SubscribeEvent
        public void onSpellExpired(SpellEvent.Expired event) {
            expiredCount++;
        }

        @SubscribeEvent
        public void onSpellPartial(SpellEvent.Partial event) {
            partialCount++;
        }
    }
}
