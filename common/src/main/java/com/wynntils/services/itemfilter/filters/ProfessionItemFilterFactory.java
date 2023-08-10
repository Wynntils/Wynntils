/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.services.itemfilter.type.ItemFilterFactory;
import com.wynntils.utils.type.ErrorOr;
import net.minecraft.client.resources.language.I18n;

public class ProfessionItemFilterFactory implements ItemFilterFactory {
    @Override
    public ErrorOr<ProfessionItemFilter> create(String inputString) {
        ProfessionType profession = ProfessionType.fromString(inputString);
        if (profession == null) {
            return ErrorOr.error(I18n.get(getI18nKey() + ".invalidProfession", inputString));
        } else {
            return ErrorOr.of(new ProfessionItemFilter(profession));
        }
    }

    @Override
    public String getKeyword() {
        return "prof";
    }

    @Override
    public String getI18nKey() {
        return "feature.wynntils.itemFilter.profession";
    }
}
