/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.chat.type;

import com.wynntils.handlers.chat.type.RecipientType;
import java.util.Set;

public record ChatTab(
        String name,
        boolean consuming,
        String autoCommand,
        Set<RecipientType> filteredTypes,
        String customRegexString) {}
