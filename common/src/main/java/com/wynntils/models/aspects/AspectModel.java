/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.aspects;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.aspects.type.AspectInfo;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.items.game.AspectItem;
import java.util.List;
import java.util.stream.Stream;

public final class AspectModel extends Model {
    private final AspectInfoRegistry aspectInfoRegistry = new AspectInfoRegistry();

    public AspectModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        aspectInfoRegistry.registerDownloads(registry);
    }

    public Stream<AspectInfo> getAllAspectInfos() {
        return aspectInfoRegistry.getAllAspectInfos();
    }

    public ItemAnnotation fromNameAndClass(StyledText name, ClassType classType, int tier) {
        AspectInfo aspectInfo =
                aspectInfoRegistry.getFromClassAndDisplayName(classType, name.getStringWithoutFormatting());

        if (aspectInfo == null) {
            WynntilsMod.warn("Could not find aspect info for " + name.getStringWithoutFormatting());
            return null;
        }

        return new AspectItem(aspectInfo, tier);
    }
}
