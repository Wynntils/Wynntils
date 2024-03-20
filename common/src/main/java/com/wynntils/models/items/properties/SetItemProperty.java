package com.wynntils.models.items.properties;

import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.gear.type.SetInstance;

import java.util.Optional;

public interface SetItemProperty {
    Optional<SetInfo> getSetInfo();

    Optional<SetInstance> getSetInstance();
}
