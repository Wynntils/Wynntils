package com.wynntils.models.aspects.type;

import com.wynntils.models.character.type.ClassType;
import java.util.List;

public record SavableAspectSet(List<String> aspectNames, ClassType classType) {
    public int getAspectCount() {
        return aspectNames == null ? 0 : aspectNames.size();
    }
}