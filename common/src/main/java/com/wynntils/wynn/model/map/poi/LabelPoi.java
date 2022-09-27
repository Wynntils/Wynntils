/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.wynntils.gui.render.Texture;

public class LabelPoi extends Poi {
    private static final int LABEL_Y = 64;

    private final Label label;

    public LabelPoi(Label label) {
        super(new MapLocation(label.getX(), LABEL_Y, label.getZ()));
        this.label = label;
    }

    @Override
    public Texture getIcon() {
        // Labels do not have icons
        return null;
    }

    @Override
    public String getName() {
        return label.getName();
    }

    public Label getLabel() {
        return label;
    }
}
