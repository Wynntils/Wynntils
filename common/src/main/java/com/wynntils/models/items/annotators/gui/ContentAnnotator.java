/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.content.type.ContentInfo;
import com.wynntils.models.content.type.ContentType;
import com.wynntils.models.items.items.gui.ContentItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ContentAnnotator implements ItemAnnotator {
    private static final Pattern CONTENT_PATTERN =
            Pattern.compile("^§(?<color>[56abcdef])(?<name>.+)§7 \\[(?<type>.+)\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (itemStack.getItem() != Items.GOLDEN_AXE) return null;

        Matcher matcher = name.getMatcher(CONTENT_PATTERN);
        if (!matcher.matches()) return null;

        ContentType contentType = ContentType.from(matcher.group("color"), matcher.group("type"));
        if (contentType == null) return null;

        String contentName = matcher.group("name");
        ContentInfo contentInfo = Models.Content.parseItem(contentName, contentType, itemStack);
        if (contentInfo == null) return null;

        return new ContentItem(contentInfo);
    }
}
