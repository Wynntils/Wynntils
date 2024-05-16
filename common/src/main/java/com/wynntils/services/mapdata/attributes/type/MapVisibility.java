/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

public abstract class MapVisibility {
    public static final FullMapVisibility NEVER = new FullMapVisibility(100, 0, 6);
    public static final FullMapVisibility ALWAYS = new FullMapVisibility(0, 100, 6);
    public static final FullMapVisibility DEFAULT_ICON_VISIBILITY = new FullMapVisibility(0, 100, 6);
    public static final FullMapVisibility DEFAULT_LABEL_VISIBILITY = new FullMapVisibility(0, 100, 3);

    public abstract float getMin();

    public abstract float getMax();

    public abstract float getFade();
}
