/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.lootrun.LootrunBeaconCountOverlay;
import com.wynntils.overlays.lootrun.LootrunTaskNameOverlay;

public class LootrunOverlaysFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay lootrunTaskNameOverlay = new LootrunTaskNameOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay lootrunBeaconCountOverlay = new LootrunBeaconCountOverlay();
}
