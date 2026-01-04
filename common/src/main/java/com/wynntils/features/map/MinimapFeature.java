/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.RenderState;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.minimap.CoordinateOverlay;
import com.wynntils.overlays.minimap.MinimapOverlay;
import com.wynntils.overlays.minimap.TerritoryOverlay;

@ConfigCategory(Category.MAP)
public class MinimapFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    public final MinimapOverlay minimapOverlay = new MinimapOverlay();

    @OverlayInfo(renderAt = RenderState.PRE, renderType = RenderEvent.ElementType.GUI)
    private final Overlay coordinatesOverlay = new CoordinateOverlay();

    @OverlayInfo(renderAt = RenderState.PRE, renderType = RenderEvent.ElementType.GUI)
    private final Overlay territoryOverlay = new TerritoryOverlay();

    @RegisterKeyBind
    public final KeyBind zoomIn = KeyBindDefinition.MINIMAP_ZOOM_IN.create(() -> minimapOverlay.adjustZoomLevel(2));

    @RegisterKeyBind
    public final KeyBind zoomOut = KeyBindDefinition.MINIMAP_ZOOM_OUT.create(() -> minimapOverlay.adjustZoomLevel(-2));

    public MinimapFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }
}
