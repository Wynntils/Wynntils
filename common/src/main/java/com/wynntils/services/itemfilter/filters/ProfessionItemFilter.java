/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.services.itemfilter.type.ItemFilter;
import com.wynntils.services.itemfilter.type.ItemFilterMatcher;
import com.wynntils.utils.type.ErrorOr;
import java.util.List;

public class ProfessionItemFilter extends ItemFilter {
    public ProfessionItemFilter() {
        super(List.of("prof"));
    }

    @Override
    public ErrorOr<ItemFilterMatcher> createMatcher(String inputString) {
        ProfessionType profession = ProfessionType.fromString(inputString);
        if (profession == null) {
            return ErrorOr.error(getTranslation("invalidProfession", inputString));
        } else {
            return ErrorOr.of(new ProfessionItemFilterMatcher(profession));
        }
    }
}
