/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.handleditems.items.game.MaterialItem;
import com.wynntils.wynn.objects.profiles.material.MaterialProfile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class MaterialAnnotator implements ItemAnnotator {
    private static final Pattern MATERIAL_PATTERN = Pattern.compile("§f(.*) ([^ ]+)§6 \\[§e✫((?:§8)?✫(?:§8)?)✫§6\\]");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        String name = ComponentUtils.getCoded(itemStack.getHoverName());

        Matcher matcher = MATERIAL_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        String materialSource = matcher.group(1);
        String resourceType = matcher.group(2);
        String tierIndicator = matcher.group(3);
        int tier =
                switch (tierIndicator) {
                    case "§8✫" -> 1;
                    case "✫§8" -> 2;
                    case "✫" -> 3;
                    default -> {
                        WynntilsMod.warn("Cannot parse tier from material: " + name);
                        yield 1;
                    }
                };

        MaterialProfile materialProfile = MaterialProfile.lookup(materialSource, resourceType, tier);
        if (materialProfile == null) return null;

        return new MaterialItem(materialProfile);
    }
}
