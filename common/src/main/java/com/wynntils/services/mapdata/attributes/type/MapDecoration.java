/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

// Currently only for player health bar, but can be extended to more types of
// overlays
public interface MapDecoration {
    MapDecoration NONE = new MapDecoration() {
        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public int getBarPercentage() {
            return 0;
        }
    };

    boolean isVisible();

    int getBarPercentage();
}
