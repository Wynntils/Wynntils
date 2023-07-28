/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter;

import net.minecraft.client.resources.language.I18n;

public class InvalidSyntaxException extends Exception {
    public InvalidSyntaxException(String translateKey, String... parameters) {
        super(I18n.get(translateKey, parameters));
    }
}
