/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.aspects.type;

import com.wynntils.models.character.type.ClassType;
import java.util.List;

public record SavableAspectSet(List<String> aspectNames, ClassType classType) {
    public int getAspectCount() {
        return aspectNames == null ? 0 : aspectNames.size();
    }

    public int getLevel() {
        int count = getAspectCount();
        return count <= 1 ? count : (count - 1) * 20;
    }
}
