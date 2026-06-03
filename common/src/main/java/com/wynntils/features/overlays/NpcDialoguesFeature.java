/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.overlays.DialogueOverlay;
import com.wynntils.utils.type.RenderElementType;

@ConfigCategory(Category.OVERLAYS)
public class NpcDialoguesFeature extends Feature {
    @RegisterOverlay(renderType = RenderElementType.TITLE)
    private final Overlay detachedDialogueOverlay = new DialogueOverlay();

    @Persisted
    protected final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    @Persisted
    public final Config<Boolean> hideFadeEffect = new Config<>(false);

    public NpcDialoguesFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER)
                .build());
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        Models.Dialogue.setHideSegments(!isEnabled(), !shouldDisplayOriginal.get(), hideFadeEffect.get());
    }
}
