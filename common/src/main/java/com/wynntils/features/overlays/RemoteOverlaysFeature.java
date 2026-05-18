/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.overlays.annotations.RemoteOverlayHolder;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;

@ConfigCategory(Category.OVERLAYS)
@RemoteOverlayHolder
public class RemoteOverlaysFeature extends Feature {
    public RemoteOverlaysFeature() {
        super(ProfileDefault.ENABLED);
    }
}
