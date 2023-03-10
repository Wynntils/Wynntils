/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.TextOverlay;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.resources.language.I18n;

@ConfigCategory(Category.OVERLAYS)
public class InfoBoxFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay infoBox1Overlay = new InfoBoxOverlay(1);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay infoBox2Overlay = new InfoBoxOverlay(2);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay infoBox3Overlay = new InfoBoxOverlay(3);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay infoBox4Overlay = new InfoBoxOverlay(4);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay infoBox5Overlay = new InfoBoxOverlay(5);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay infoBox6Overlay = new InfoBoxOverlay(6);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay infoBox7Overlay = new InfoBoxOverlay(
            7,
            "{x:0} {y:0} {z:0}",
            new OverlayPosition(
                    160, 20, VerticalAlignment.Top, HorizontalAlignment.Left, OverlayPosition.AnchorSection.TopLeft),
            HorizontalAlignment.Center,
            VerticalAlignment.Middle,
            0);

    public static class InfoBoxOverlay extends TextOverlay {
        @Config
        public String content = "";

        private final int id;

        protected InfoBoxOverlay(int id) {
            super(
                    new OverlayPosition(
                            -65 + (15 * id),
                            5,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Left,
                            OverlayPosition.AnchorSection.MiddleLeft),
                    new GuiScaledOverlaySize(120, 10),
                    HorizontalAlignment.Left,
                    VerticalAlignment.Middle);
            this.id = id;
        }

        protected InfoBoxOverlay(
                int id,
                String content,
                OverlayPosition position,
                HorizontalAlignment horizontalAlignment,
                VerticalAlignment verticalAlignment,
                float secondsPerRecalculation) {
            super(position, new GuiScaledOverlaySize(120, 10), horizontalAlignment, verticalAlignment);
            this.id = id;
            this.content = content;
            this.secondsPerRecalculation = secondsPerRecalculation;
        }

        @Override
        public String getTemplate() {
            return content;
        }

        @Override
        public String getPreviewTemplate() {
            return "&cX: {x:0}, &9Y: {y:0}, &aZ: {z:0}";
        }

        @Override
        public String getTranslatedName() {
            return I18n.get(
                    "feature.wynntils." + getDeclaringFeatureNameCamelCase() + ".overlay." + getNameCamelCase()
                            + ".name",
                    id);
        }

        @Override
        public String getConfigJsonName() {
            return super.getConfigJsonName() + id;
        }
    }
}
