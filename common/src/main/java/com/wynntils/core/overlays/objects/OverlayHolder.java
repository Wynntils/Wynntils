/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.overlays.objects;

import com.wynntils.core.config.objects.StorageHolder;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.overlays.properties.Overlay;
import java.lang.reflect.Field;

public class OverlayHolder extends StorageHolder {
    private final Overlay metadata;

    public OverlayHolder(Object parent, Field field, Overlay metadata) {
        super(
                parent,
                field,
                field.getType(),
                parent.getClass().getAnnotation(FeatureInfo.class).category(),
                false);

        this.metadata = metadata;
    }

    public Overlay getMetadata() {
        return metadata;
    }

    public boolean getToggled() {
        // Type casting to allow #isToggled to be called, it should be one anyway
        return ((OverlayPosition) getValue()).isToggled();
    }

    public void setToggled(boolean toggled) {
        // Type casting to allow #setToggled to be called, it should be one anyway
        setValue(((OverlayPosition) getValue()).setToggled(true));
    }
}
