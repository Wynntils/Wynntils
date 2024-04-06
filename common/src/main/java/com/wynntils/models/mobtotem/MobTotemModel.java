/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mobtotem;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.labels.event.EntityLabelChangedEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.PosUtils;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MobTotemModel extends Model {
    private static final Pattern MOB_TOTEM_NAME = Pattern.compile("^§f§l(.*)'s§6§l Mob Totem$");
    private static final Pattern MOB_TOTEM_TIMER = Pattern.compile("^§c§l([0-9]+:[0-9]+)$");
    private static final double TOTEM_COORDINATE_DIFFERENCE = 0.2d;

    private final Map<Integer, MobTotem> mobTotems = new LinkedHashMap<>();

    public MobTotemModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onTotemRename(EntityLabelChangedEvent e) {
        if (!Models.WorldState.onWorld()) return;

        Entity entity = e.getEntity();
        if (!(entity instanceof ArmorStand as)) return;

        // If a new mob totem just appeared, add it to the unstarted list
        // Totem timers do not match the MOB_TOTEM_NAME pattern
        Matcher nameMatcher = e.getName().getMatcher(MOB_TOTEM_NAME);
        if (nameMatcher.find()) {
            int mobTotemId = e.getEntity().getId();

            if (mobTotems.containsKey(mobTotemId)) return; // If the totem is already in the list, don't add it again

            mobTotems.put(mobTotemId, new MobTotem(PosUtils.newPosition(as), nameMatcher.group(1)));
            return;
        }

        Matcher timerMatcher = e.getName().getMatcher(MOB_TOTEM_TIMER);
        if (!timerMatcher.find()) return;

        mobTotems.values().stream()
                .filter(
                        // Exact equality is fine here because the totem is stationary
                        mobTotem -> as.getX() == mobTotem.getPosition().x()
                                && as.getY() == (mobTotem.getPosition().y() + TOTEM_COORDINATE_DIFFERENCE)
                                && as.getZ() == mobTotem.getPosition().z())
                .forEach(mobTotem -> mobTotem.setTimerString(timerMatcher.group(1)));
    }

    @SubscribeEvent
    public void onTotemDestroy(RemoveEntitiesEvent e) {
        if (!Models.WorldState.onWorld()) return;

        e.getEntityIds().forEach(mobTotems::remove);
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        mobTotems.clear();
    }

    public List<MobTotem> getMobTotems() {
        return mobTotems.values().stream()
                .sorted(Comparator.comparing(MobTotem::getOwner))
                .toList();
    }

    public MobTotem getMobTotem(int index) {
        if (index < 0 || index >= mobTotems.size()) {
            return null;
        }

        return getMobTotems().get(index);
    }
}
