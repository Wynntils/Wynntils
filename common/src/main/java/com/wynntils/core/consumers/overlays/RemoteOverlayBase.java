/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.overlays;

import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import org.spongepowered.asm.mixin.Overwrite;

public abstract class RemoteOverlayBase extends DynamicOverlay implements CustomNameProperty {
    @Persisted
    private final HiddenConfig<String> customName = new HiddenConfig<>("");

    private final String nameId;

    protected RemoteOverlayBase(String nameId) {
        super(
                new OverlayPosition(
                        0,
                        0,
                        VerticalAlignment.MIDDLE,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.MIDDLE),
                new OverlaySize(100f, 20f),
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE,
                1);
        this.nameId = nameId;
    }

    @Overwrite
    public String getJsonName() {
        return super.getJsonName() + "_" + nameId;
    }

    @Override
    public String getTranslatedName() {
        return super.getTranslatedName() + " (" + nameId + ")";
    }

    @Override
    public Config<String> getCustomName() {
        return customName;
    }

    @Override
    public void setCustomName(String newName) {
        if (newName.isBlank()) {
            customName.setValue("");
            return;
        }
        customName.setValue(newName + " (" + getNameId() + ")");
    }

    public String getNameId() {
        return nameId;
    }
}
