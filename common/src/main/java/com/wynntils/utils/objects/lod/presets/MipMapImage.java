/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects.lod.presets;

import com.wynntils.utils.objects.IBoundingBox.AxisAlignedBoundingBox;
import java.awt.image.BufferedImage;
import java.util.Collections;

public class MipMapImage extends AxisAlignedBoundingBox {
    public final BufferedImage image;

    public MipMapImage(BufferedImage image, AxisAlignedBoundingBox bounds) {
        super(Collections.singleton(bounds));
        this.image = image;
    }
}
