/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.spell;

import com.wynntils.core.components.Handler;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.models.worlds.event.WorldStateEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import net.neoforged.bus.api.SubscribeEvent;

public final class SpellCastHandler extends Handler {
    private final Queue<SpellDirection> spellPacketQueue = new LinkedList<>();

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent e) {
        spellPacketQueue.clear();
    }

    @SubscribeEvent
    public void onHeldItemChange(ChangeCarriedItemEvent e) {
        spellPacketQueue.clear();
    }

    public void addSpellToQueue(List<SpellDirection> spell) {
        if (!spellPacketQueue.isEmpty()) return;

        spellPacketQueue.addAll(spell);
    }

    public SpellDirection checkNextSpellDirection() {
        return spellPacketQueue.peek();
    }

    public void sendNextSpell() {
        if (spellPacketQueue.isEmpty()) return;

        SpellDirection spellDirection = spellPacketQueue.poll();
        spellDirection.getSendPacketRunnable().run();
    }

    public boolean isSpellQueueEmpty() {
        return spellPacketQueue.isEmpty();
    }
}
