/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ContainerClickEvent;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class AntiVoidFeature extends Feature {
    public static final int INVENTORY_SLOTS = 46;
    public static final int CHARACTER_INFO_SLOT = 43;
    public static final int CONTENT_BOOK_SLOT = 44;

    public AntiVoidFeature() {
        super(ProfileDefault.ENABLED);
    }

    @SubscribeEvent
    public void onContainerClick(ContainerClickEvent e) {
        if (Models.Cutscene.isCutsceneActive()) {
            if (e.getContainerMenu().slots.size() == INVENTORY_SLOTS
                    && e.getSlotNum() != CHARACTER_INFO_SLOT
                    && e.getSlotNum() != CONTENT_BOOK_SLOT) {
                e.setCanceled(true);
            }
        }
    }
}
