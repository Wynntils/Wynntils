/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.features.user.inventory.ItemTextOverlayFeature;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.type.TextOverlayProperty;
import java.util.regex.Matcher;

public class ServerCountProperty extends ItemProperty implements TextOverlayProperty {
    private final ItemTextOverlayFeature.TextOverlay textOverlay;

    public ServerCountProperty(WynnItemStack item) {
        super(item);

        Matcher matcher = WynnItemMatchers.serverItemMatcher(item.getHoverName());

        // Need to run matches to calculate group
        if (!matcher.matches()) {
            throw new IllegalStateException("ServerItem did not match regex.");
        }

        int serverId = Integer.parseInt(matcher.group(1));

        item.setCount(1);

        float scale = serverId > 100 ? 0.85f : 1f;
        textOverlay = new ItemTextOverlayFeature.TextOverlay(
                new TextRenderTask(
                        String.valueOf(serverId),
                        TextRenderSetting.DEFAULT
                                .withCustomColor(CommonColors.WHITE)
                                .withHorizontalAlignment(HorizontalAlignment.Right)),
                16,
                8,
                scale);
    }

    @Override
    public ItemTextOverlayFeature.TextOverlay getTextOverlay() {
        return textOverlay;
    }

    @Override
    public boolean isTextOverlayEnabled() {
        return true;
    }
}
