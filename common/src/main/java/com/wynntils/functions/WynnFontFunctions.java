/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.colors.CustomColor;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class WynnFontFunctions {


    @TemplateFunction(name = "to_fancy_text")
    public static String toFancyTextFunction(String text) {
        return WynnFont.asFancyFont(text);

    }


    @TemplateFunction(name = "to_background_text")
    public static String toBackgroundTextFunction(String text, CustomColor textColor, CustomColor backgroundColor, String leftEdge, String rightEdge) {
        return WynnFont.asBackgroundFont(text, textColor, backgroundColor, leftEdge, rightEdge);
    }
}
