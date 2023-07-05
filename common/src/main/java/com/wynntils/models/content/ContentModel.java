/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content;

import com.wynntils.core.components.Model;
import com.wynntils.models.content.type.ContentDifficulty;
import com.wynntils.models.content.type.ContentDistance;
import com.wynntils.models.content.type.ContentInfo;
import com.wynntils.models.content.type.ContentLength;
import com.wynntils.models.content.type.ContentType;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class ContentModel extends Model {
    public ContentModel() {
        super(List.of());
    }

    public ContentInfo parseItem(String name, ContentType type, ItemStack itemStack) {
        return new ContentInfo(
                type,
                name,
                null,
                null,
                null,
                0,
                ContentDistance.MEDIUM,
                List.of(),
                ContentDifficulty.MEDIUM,
                ContentLength.MEDIUM,
                List.of(),
                false);
    }
}
