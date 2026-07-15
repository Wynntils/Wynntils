/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.wynnalphabet.WynnAlphabet;
import java.util.Locale;
import com.wynntils.templates.annotations.TemplateFunction;

//Functions are accessed via reflection
@SuppressWarnings("unused")
public class WynnAlphabetFunctions {

    @TemplateFunction(name = "transcribe_gavellian", aliases = { "gavellian" })
    public String transcribeGavellianFunction(String gavellian) {
        String input = gavellian.toLowerCase(Locale.ROOT);
        if (input == null)
            return "";
        return Models.WynnAlphabet.transcribeMessageToWynnAlphabet(input, WynnAlphabet.GAVELLIAN);
    }

    @TemplateFunction(name = "transcribe_wynnic", aliases = { "wynnic" })
    public String transcribeWynnicFunction(String wynnic) {
        String input = wynnic.toLowerCase(Locale.ROOT);
        if (input == null)
            return "";
        return Models.WynnAlphabet.transcribeMessageToWynnAlphabet(input, WynnAlphabet.WYNNIC);
    }
}
