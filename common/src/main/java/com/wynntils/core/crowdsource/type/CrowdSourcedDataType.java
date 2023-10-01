/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.crowdsource.type;

import com.google.common.base.CaseFormat;
import com.wynntils.core.crowdsource.datatype.LootrunTaskLocation;
import net.minecraft.client.resources.language.I18n;

/**
 * This enum represents the type of crowd sourced data that is being collected.
 */
public enum CrowdSourcedDataType {
    LOOTRUN_TASK_LOCATIONS(LootrunTaskLocation.class);

    private final Class<? extends Comparable<?>> dataClass;

    CrowdSourcedDataType(Class<? extends Comparable<?>> dataClass) {
        this.dataClass = dataClass;
    }

    public Class<? extends Comparable<?>> getDataClass() {
        return dataClass;
    }

    public String getTranslatedName() {
        return I18n.get(getTranslationKey() + ".name");
    }

    public String getTranslatedDescription() {
        return I18n.get(getTranslationKey() + ".description");
    }

    private String getTranslationKey() {
        return "crowdSourcedDataType.wynntils." + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this.name());
    }
}
