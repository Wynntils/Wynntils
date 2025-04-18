package com.wynntils.models.spells;

import com.wynntils.core.components.Model;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.models.worlds.event.WorldStateEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public final class SpellCasterModel extends Model {
    private final Queue<SpellDirection> spellPacketQueue = new LinkedList<>();

    public SpellCasterModel() {
        super(List.of());
    }

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
