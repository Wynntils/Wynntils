package com.wynntils.models.containers.containers.crafting;

import com.wynntils.models.containers.type.HighlightableProfessionProperty;
import com.wynntils.models.profession.type.ProfessionType;

import java.util.regex.Pattern;

public class JewelingContainer extends CraftingStationContainer implements HighlightableProfessionProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFF8\uE053\uDAFF\uDF80\uF044");
    private static final ProfessionType PROFESSION_TYPE = ProfessionType.JEWELING;

    public JewelingContainer() {
        super(TITLE_PATTERN, PROFESSION_TYPE);
    }
}
