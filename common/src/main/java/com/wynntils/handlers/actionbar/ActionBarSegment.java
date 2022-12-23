/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ActionBarSegment {
    Pattern getPattern();

    void handleMatch(Matcher matcher);

    ActionBarPosition getPosition();

    default void removed() {}

    boolean isHidden();
}
