/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.profession.type.MaterialProfile;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class MaterialAnnotator implements GameItemAnnotator {
    private static final Pattern MATERIAL_PATTERN = Pattern.compile("^\uDAFC\uDC00§#3cb0e6ff(.*) ([^ ]+)\uDAFC\uDC00$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(MATERIAL_PATTERN);
        if (!matcher.matches()) return null;

        String materialSource = matcher.group(1);
        String resourceType = matcher.group(2);
        int tier = WynnItemParser.parseProfessionTier(itemStack);

        MaterialProfile materialProfile = MaterialProfile.lookup(materialSource, resourceType, tier);
        if (materialProfile == null) return null;

        return new MaterialItem(materialProfile);
    }
}
