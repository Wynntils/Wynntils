/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.objects.CommonColors;

public class LabelPoi extends Poi {
    private static final int LABEL_Y = 64;

    private final Label label;

    public LabelPoi(Label label) {
        super(new MapLocation(label.getX(), LABEL_Y, label.getZ()));
        this.label = label;
    }

    @Override
    public int getWidth() {
        return FontRenderer.getInstance().getFont().width(label.getName());
    }

    @Override
    public int getHeight() {
        return FontRenderer.getInstance().getFont().lineHeight;
    }

    @Override
    public void renderAt(PoseStack poseStack, float renderX, float renderZ, boolean hovered, float scale) {
        // TODO hovered behavior?
        // TODO reimplement minscaleforlabel through fading instead

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        label.getName(),
                        renderX,
                        renderZ,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Middle,
                        FontRenderer.TextShadow.NORMAL);
    }

    @Override
    public String getName() {
        return label.getName();
    }

    public Label getLabel() {
        return label;
    }
}
