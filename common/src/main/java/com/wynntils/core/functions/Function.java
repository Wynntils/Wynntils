/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions;

public interface Function<T> {
    T getValue(String argument);

    // TODO: Could/should we turn this into an annotation instead?
    String getName();

    // FIXME: this should be extracted automagically from i18n
    String getDescription();
}
