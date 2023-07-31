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
import com.wynntils.models.beacons.type.BeaconColor;
import com.wynntils.models.beacons.type.UnverifiedBeacon;
import com.wynntils.models.beacons.type.VerifiedBeacon;
import com.wynntils.utils.type.TimedSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BeaconModel extends Model {
    // Amount of armor stands above each other to consider this a beacon
    private static final int VERIFICATION_ENTITY_COUNT = 6;

    private final TimedSet<UnverifiedBeacon> unverifiedBeacons = new TimedSet<>(1000, TimeUnit.MILLISECONDS, true);

    private final Set<VerifiedBeacon> verifiedBeacons = new HashSet<>();

    public BeaconModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onEntityAdd(AddEntityEvent event) {
        if (event.getType() != EntityType.ARMOR_STAND) return;

        Entity entity = event.getEntity();
        Position position = entity.position();

        if (isDuplicateBeacon(position)) return;

        UnverifiedBeacon unverifiedBeacon = getUnverifiedBeaconAt(position);
        if (unverifiedBeacon == null) {
            unverifiedBeacons.put(new UnverifiedBeacon(position, entity));
            return;
        }

        boolean correctPosition = unverifiedBeacon.addEntity(entity);

        if (!correctPosition) {
            unverifiedBeacons.remove(unverifiedBeacon);
            return;
        }

        if (unverifiedBeacon.getEntities().size() == VERIFICATION_ENTITY_COUNT) {
            BeaconColor beaconColor = BeaconColor.fromUnverifiedBeacon(unverifiedBeacon);

            if (beaconColor == null) {
                WynntilsMod.warn("Could not determine beacon color at " + position + " for entities "
                        + unverifiedBeacon.getEntities());
                unverifiedBeacons.remove(unverifiedBeacon);
                return;
            }

            VerifiedBeacon verifiedBeacon = VerifiedBeacon.fromUnverifiedBeacon(unverifiedBeacon, beaconColor);
            verifiedBeacons.add(verifiedBeacon);
            WynntilsMod.postEvent(new BeaconEvent.Added(verifiedBeacon));

            unverifiedBeacons.remove(unverifiedBeacon);
        }
    }

    @SubscribeEvent
    public void onEntityTeleport(TeleportEntityEvent event) {
        Optional<VerifiedBeacon> verifiedBeaconOpt = verifiedBeacons.stream()
                .filter(verifiedBeacon -> verifiedBeacon.getBaseEntity().equals(event.getEntity()))
                .findFirst();

        if (verifiedBeaconOpt.isEmpty()) return;

        VerifiedBeacon verifiedBeacon = verifiedBeaconOpt.get();
        verifiedBeacon.updatePosition(event.getNewPosition());
        WynntilsMod.postEvent(new BeaconEvent.Moved(verifiedBeacon));
    }

    @SubscribeEvent
    public void onEntityRemoved(RemoveEntitiesEvent event) {
        List<Integer> entityIds = event.getEntityIds();

        List<VerifiedBeacon> removedBeacons = verifiedBeacons.stream()
                .filter(verifiedBeacon ->
                        entityIds.contains(verifiedBeacon.getBaseEntity().getId()))
                .toList();

        for (VerifiedBeacon removedBeacon : removedBeacons) {
            verifiedBeacons.remove(removedBeacon);
            WynntilsMod.postEvent(new BeaconEvent.Removed(removedBeacon));
        }
    }

    private boolean isDuplicateBeacon(Position position) {
        return verifiedBeacons.stream().anyMatch(verifiedBeacon -> {
            Position beaconPosition = verifiedBeacon.getPosition();
            return beaconPosition.x() == position.x() && beaconPosition.z() == position.z();
        });
    }

    private UnverifiedBeacon getUnverifiedBeaconAt(Position position) {
        return unverifiedBeacons.stream()
                .filter(unverifiedBeacon -> {
                    Position beaconPosition = unverifiedBeacon.getPosition();
                    return beaconPosition.x() == position.x() && beaconPosition.z() == position.z();
                })
                .findFirst()
                .orElse(null);
    }
}
