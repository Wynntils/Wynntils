package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.FullscreenContainerProperty;

import java.util.regex.Pattern;

public class CharacterCreationContainer extends Container implements FullscreenContainerProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFD0\uE025");

    public CharacterCreationContainer() {
        super(TITLE_PATTERN);
    }
}
