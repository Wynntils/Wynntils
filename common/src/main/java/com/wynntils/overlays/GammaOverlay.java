/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class GammaOverlay extends Overlay {

    public GammaOverlay() {
        super(
                0,
                11,
                true,
                OverlayGrowFrom.CENTER,
                OverlayGrowFrom.CENTER,
                RenderEvent.ElementType.EXPERIENCE);
    }

    @Override
    public MutableComponent getNameComponent() {
        return new TranslatableComponent("overlay.wynntils.gamma.name");
    }

    @Override
    public void tick() {}

    @Override
    public boolean render(RenderEvent.Pre e) {
        return false;
    }

    @Override
    public void render(RenderEvent.Post e) {
        if (McUtils.options().gamma >= 1000) {
            // TODO some rendering utils like old wynntils's ScreenRenderer but less centrallized
            // TODO merge with ColorUtils when/if that gets merged
            McUtils.mc().font.draw(e.getStack(), "GammaBright", 0, 0, 0xff9000);
        }
    }
}
