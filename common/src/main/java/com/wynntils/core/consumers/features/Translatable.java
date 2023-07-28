/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.features;

public interface Translatable {
    String getTranslatedName();

    String getTranslation(String keySuffix);
}
