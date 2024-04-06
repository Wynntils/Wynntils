/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import org.apache.commons.lang3.StringUtils;

public final class WynnUtils {
    /**
     * Removes the characters 'À' ('\u00c0') and ֎ ('\u058e') that is sometimes added in Wynn APIs and
     * replaces '’' ('\u2019') (RIGHT SINGLE QUOTATION MARK) with '\''
     *
     * @param input string
     * @return the string without these two chars
     */
    public static String normalizeBadString(String input) {
        if (input == null) return "";
        return StringUtils.replaceEach(input, new String[] {"ÀÀÀ", "À", "֎", "’"}, new String[] {" ", "", "", "'"});
    }
}
