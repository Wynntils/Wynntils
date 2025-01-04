/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import com.wynntils.utils.colors.CustomColor;
import java.util.Optional;
import net.minecraft.world.phys.Vec3;

public record BeaconMarker(
        Vec3 position, BeaconMarkerKind beaconMarkerKind, Optional<Integer> distance, Optional<CustomColor> color) {}
