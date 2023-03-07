/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.RenderState;
import com.wynntils.core.features.overlays.TextOverlay;
import com.wynntils.mc.event.RenderEvent;
import java.util.ArrayList;
import java.util.List;

@ConfigCategory(Category.OVERLAYS)
public class InfoBoxFeature extends UserFeature {
    @OverlayGroup(instances = 7, renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.Pre)
    private final List<InfoBoxOverlay> infoBoxOverlays = new ArrayList<>();

    //    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    //    private final Overlay infoBox1Overlay = new InfoBoxOverlay(1);
    //
    //    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    //    private final Overlay infoBox2Overlay = new InfoBoxOverlay(2);
    //
    //    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    //    private final Overlay infoBox3Overlay = new InfoBoxOverlay(3);
    //
    //    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    //    private final Overlay infoBox4Overlay = new InfoBoxOverlay(4);
    //
    //    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    //    private final Overlay infoBox5Overlay = new InfoBoxOverlay(5);
    //
    //    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    //    private final Overlay infoBox6Overlay = new InfoBoxOverlay(6);
    //
    //    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    //    private final Overlay infoBox7Overlay = new InfoBoxOverlay(
    //            7,
    //            "{x:0} {y:0} {z:0}",
    //            new OverlayPosition(
    //                    160, 20, VerticalAlignment.Top, HorizontalAlignment.Left,
    // OverlayPosition.AnchorSection.TopLeft),
    //            HorizontalAlignment.Center,
    //            VerticalAlignment.Middle,
    //            0);

    public static class InfoBoxOverlay extends TextOverlay {
        @Config
        public String content = "";

        public InfoBoxOverlay(int id) {
            super(id);
        }

        @Override
        public String getTemplate() {
            return content;
        }

        @Override
        public String getPreviewTemplate() {
            return "&cX: {x:0}, &9Y: {y:0}, &aZ: {z:0}";
        }
    }
}
