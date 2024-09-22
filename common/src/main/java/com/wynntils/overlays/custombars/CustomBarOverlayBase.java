/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.custombars;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.BarOverlay;
import com.wynntils.core.consumers.overlays.CustomNameProperty;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.utils.type.ErrorOr;

public abstract class CustomBarOverlayBase extends BarOverlay implements CustomNameProperty {
    @Persisted
    public final HiddenConfig<String> customName = new HiddenConfig<>("");

    @Persisted(i18nKey = "feature.wynntils.customBarsOverlay.overlay.customBarBase.textTemplate")
    public final Config<String> textTemplate = new Config<>("");

    @Persisted(i18nKey = "feature.wynntils.customBarsOverlay.overlay.customBarBase.valueTemplate")
    public final Config<String> valueTemplate = new Config<>("");

    @Persisted(i18nKey = "feature.wynntils.customBarsOverlay.overlay.customBarBase.enabledTemplate")
    public final Config<String> enabledTemplate = new Config<>("");

    protected CustomBarOverlayBase(int id, OverlaySize overlaySize) {
        super(id, overlaySize);
    }

    @Override
    public BarOverlayTemplatePair getTemplate() {
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
    public boolean isRendered() {
        // If the text template is empty, the overlay is not rendered.
        if (valueTemplate.get().isEmpty()) return false;

        // If the enabled template is empty,
        // the overlay is rendered when the player is in the world.
        if (enabledTemplate.get().isEmpty()) return Models.WorldState.onWorld();

        // If the enabled template is not empty,
        // the overlay is rendered when the template evaluates to true.
        ErrorOr<Boolean> enabledOrError = Managers.Function.tryGetRawValueOfType(enabledTemplate.get(), Boolean.class);
        return !enabledOrError.hasError() && enabledOrError.getValue();
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
