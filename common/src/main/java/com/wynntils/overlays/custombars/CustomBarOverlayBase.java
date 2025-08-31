/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.custombars;

import com.wynntils.core.consumers.overlays.BarOverlay;
import com.wynntils.core.consumers.overlays.CustomNameProperty;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.HiddenConfig;

public abstract class CustomBarOverlayBase extends BarOverlay implements CustomNameProperty {
    @Persisted
    private final HiddenConfig<String> customName = new HiddenConfig<>("");

    @Persisted(i18nKey = "feature.wynntils.customBarsOverlay.overlay.customBarBase.textTemplate")
    private final Config<String> textTemplate = new Config<>("");

    @Persisted(i18nKey = "feature.wynntils.customBarsOverlay.overlay.customBarBase.valueTemplate")
    private final Config<String> valueTemplate = new Config<>("");

    protected CustomBarOverlayBase(int id, OverlaySize overlaySize) {
        super(id, overlaySize);
    }

    @Override
    protected BarOverlayTemplatePair getTemplate() {
        return new BarOverlayTemplatePair(textTemplate.get(), valueTemplate.get());
    }

    @Override
    public BarOverlayTemplatePair getPreviewTemplate() {
        if (!valueTemplate.get().isEmpty()) {
            return getTemplate();
        }

        return getActualPreviewTemplate();
    }

    @Override
    public boolean isVisible() {
        return !valueTemplate.get().isEmpty();
    }

    @Override
    public Config<String> getCustomName() {
        return customName;
    }

    @Override
    public void setCustomName(String newName) {
        customName.setValue(newName);
    }

    protected abstract BarOverlayTemplatePair getActualPreviewTemplate();
}
