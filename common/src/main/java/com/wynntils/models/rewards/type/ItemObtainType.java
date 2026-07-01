/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum ItemObtainType {
    KEY_GUARDIAN("screens.wynntils.wynntilsGuides.obtain.keyGuardian"),
    LOOTRUN("screens.wynntils.wynntilsGuides.obtain.lootrun"),
    RAID("screens.wynntils.wynntilsGuides.obtain.raid"),
    RUNE_GUARDIAN("screens.wynntils.wynntilsGuides.obtain.runeGuardian"),
    SHRINE_ALTAR("screens.wynntils.wynntilsGuides.obtain.shrineAltar"),
    WORLD_EVENT("screens.wynntils.wynntilsGuides.obtain.worldEvent");

    private final String translationKey;

    ItemObtainType(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public MutableComponent getTranslatedComponent() {
        return Component.translatable(translationKey);
    }
}
