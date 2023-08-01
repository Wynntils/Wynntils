/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.mc.event.AddEntityEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.mc.event.TeleportEntityEvent;
import com.wynntils.models.beacons.event.BeaconEvent;
import com.wynntils.models.beacons.type.Beacon;
import com.wynntils.models.beacons.type.BeaconColor;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.TimedSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.compress.utils.Lists;

public class BeaconModel extends Model {
    // Amount of armor stands above each other to consider this a beacon
    // (A beacon typically has around 17 in total)
    private static final int VERIFICATION_ENTITY_COUNT = 6;

    private final TimedSet<UnverifiedBeacon> unverifiedBeacons = new TimedSet<>(1000, TimeUnit.MILLISECONDS, true);
    // Maps base entity id to corresponding beacon
    private final Map<Integer, Beacon> verifiedBeacons = new HashMap<>();

    public BeaconModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onEntityAdd(AddEntityEvent event) {
        if (event.getType() != EntityType.ARMOR_STAND) return;

        Entity entity = event.getEntity();
        Location location = Location.containing(entity.position());

        if (isDuplicateBeacon(location)) return;

        UnverifiedBeacon unverifiedBeacon = getUnverifiedBeaconAt(location);
        if (unverifiedBeacon == null) {
            unverifiedBeacons.put(new UnverifiedBeacon(location, entity));
            return;
        }

        boolean correctLocation = unverifiedBeacon.addEntity(entity);

        if (!correctLocation) {
            unverifiedBeacons.remove(unverifiedBeacon);
            return;
        }

        if (unverifiedBeacon.getEntities().size() == VERIFICATION_ENTITY_COUNT) {
            BeaconColor beaconColor = getBeaconColor(unverifiedBeacon);

            if (beaconColor == null) {
                WynntilsMod.warn("Could not determine beacon color at " + location + " for entities "
                        + unverifiedBeacon.getEntities());
                unverifiedBeacons.remove(unverifiedBeacon);
                return;
            }

            Beacon verifiedBeacon = new Beacon(unverifiedBeacon.getLocation(), beaconColor);
            int baseEntityId = unverifiedBeacon.getEntities().get(0).getId();
            verifiedBeacons.put(baseEntityId, verifiedBeacon);
            WynntilsMod.postEvent(new BeaconEvent.Added(verifiedBeacon));

            unverifiedBeacons.remove(unverifiedBeacon);
        }
    }

    @SubscribeEvent
    public void onEntityTeleport(TeleportEntityEvent event) {
        Beacon movedBeacon = verifiedBeacons.get(event.getEntity().getId());
        if (movedBeacon == null) return;

        Beacon newBeacon = new Beacon(Location.containing(event.getNewPosition()), movedBeacon.color());
        // Replace the old map entry
        verifiedBeacons.put(event.getEntity().getId(), newBeacon);
        WynntilsMod.postEvent(new BeaconEvent.Moved(movedBeacon, newBeacon));
    }

    @SubscribeEvent
    public void onEntityRemoved(RemoveEntitiesEvent event) {
        event.getEntityIds().stream().filter(verifiedBeacons::containsKey).forEach(entityId -> {
            Beacon removedBeacon = verifiedBeacons.get(entityId);
            verifiedBeacons.remove(entityId);
            WynntilsMod.postEvent(new BeaconEvent.Removed(removedBeacon));
        });
    }

    private boolean isDuplicateBeacon(Location location) {
        return verifiedBeacons.values().stream()
                .anyMatch(verifiedBeacon -> location.equalsIgnoringY(verifiedBeacon.location()));
    }

    private UnverifiedBeacon getUnverifiedBeaconAt(Location location) {
        return unverifiedBeacons.stream()
                .filter(unverifiedBeacon -> location.equalsIgnoringY(unverifiedBeacon.getLocation()))
                .findFirst()
                .orElse(null);
    }

    private BeaconColor getBeaconColor(UnverifiedBeacon unverifiedBeacon) {
        List<Entity> entities = unverifiedBeacon.getEntities();
        if (entities.isEmpty()) return null;

        Entity entity = entities.get(0);
        List<ItemStack> armorSlots = Lists.newArrayList(entity.getArmorSlots().iterator());
        if (armorSlots.size() != 4) return null;

        ItemStack bootsItem = armorSlots.get(3);
        return BeaconColor.fromItemStack(bootsItem);
    }

    private static final class UnverifiedBeacon {
        private static final float POSITION_OFFSET_Y = 7.5f;

        private final Location location;
        private final List<Entity> entities = new ArrayList<>();

        private UnverifiedBeacon(Location location, Entity entity) {
            this.location = location;
            entities.add(entity);
        }

        public Location getLocation() {
            return location;
        }

        public List<Entity> getEntities() {
            return entities;
        }

        public boolean addEntity(Entity entity) {
            Position entityPosition = entity.position();
            Position lastEntityPosition = entities.get(entities.size() - 1).position();

            if (entityPosition.y() - lastEntityPosition.y() == POSITION_OFFSET_Y) {
                entities.add(entity);
                return true;
            }

            return false;
        }
    }
}
