/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

public class WebUtils {
    public static String encodeForCargoQuery(String name) {
        return StringUtils.encodeUrl("'" + name.replace("'", "\\'") + "'");
    }

    public static String encodeForWikiTitle(String pageTitle) {
        return StringUtils.encodeUrl(pageTitle.replace(" ", "_"));
    }
}
