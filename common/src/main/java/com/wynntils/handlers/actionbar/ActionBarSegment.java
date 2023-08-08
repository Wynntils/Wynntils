/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar;

import com.wynntils.handlers.actionbar.type.ActionBarPosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ActionBarSegment {
    // What the segment tells the handler
    ActionBarPosition getPosition();

    Pattern getPattern();

    boolean isHidden();

    // What the handler tells the segment
    void appeared(Matcher matcher);

    void update(Matcher matcher);

    default void removed() {}
}
