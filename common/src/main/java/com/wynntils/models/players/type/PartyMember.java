/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type;

import com.wynntils.core.text.StyledText;

public record PartyMember(StyledText line, String name, Integer health, Integer level, Boolean online, Boolean alive) {
    public static final PartyMember EMPTY = new PartyMember(StyledText.EMPTY, "", 0, 0, false, false);
}
