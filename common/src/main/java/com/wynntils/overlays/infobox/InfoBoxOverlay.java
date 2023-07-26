/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.infobox;

import com.wynntils.core.config.Config;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.overlays.TextOverlay;

public class InfoBoxOverlay extends TextOverlay {
    @RegisterConfig
    public final Config<String> content = new Config<>("");

    public InfoBoxOverlay(int id) {
        super(id);
    }

    @Override
    public String getTemplate() {
        return content.get();
    }

    @Override
    public String getPreviewTemplate() {
        if (!content.get().isEmpty()) {
            return content.get();
        }

        return "&cX: {x(my_loc):0}, &9Y: {y(my_loc):0}, &aZ: {z(my_loc):0}";
    }
}
