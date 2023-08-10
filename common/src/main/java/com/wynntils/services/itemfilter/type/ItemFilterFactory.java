/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import com.wynntils.utils.type.ErrorOr;

public interface ItemFilterFactory {
    /**
     * Creates a new item filter from the given input string.
     *
     * @param inputString the input string
     * @return the created item filter, or a translated error string if the input string is invalid
     */
    ErrorOr<? extends ItemFilter> create(String inputString);

    /**
     * @return the keyword used to identify this filter
     */
    String getKeyword();

    /**
     * @return the i18n base key for this filter
     */
    String getI18nKey();
}
