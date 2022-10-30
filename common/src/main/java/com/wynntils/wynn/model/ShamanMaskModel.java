/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.wynntils.core.managers.Model;
import com.wynntils.features.user.overlays.ShamanMasksOverlayFeature;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.objects.ShamanMaskType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ShamanMaskModel extends Model {
    private static final Pattern MASK_PATTERN = Pattern.compile("§cMask of the (Coward|Lunatic|Fanatic)");

    private static ShamanMaskType currentMaskType = ShamanMaskType.NONE;

    public static void init() {}

    @SubscribeEvent
    public static void onTitle(SubtitleSetTextEvent event) {
        String title = ComponentUtils.getCoded(event.getComponent());

        if (title.contains("Mask of the ") || title.contains("➤")) {
            parseMask(title);

            if (ShamanMasksOverlayFeature.INSTANCE.hideMaskTitles
                    && ShamanMasksOverlayFeature.INSTANCE.shamanMaskOverlay.isEnabled()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldStateChange(WorldStateEvent event) {
        currentMaskType = ShamanMaskType.NONE;
    }

    private static void parseMask(String title) {
        Matcher matcher = MASK_PATTERN.matcher(title);

        ShamanMaskType parsedMask = ShamanMaskType.NONE;

        if (matcher.matches()) {
            parsedMask = ShamanMaskType.find(matcher.group(1));
        } else {
            for (ShamanMaskType type : ShamanMaskType.values()) {
                if (type.getParseString() == null) continue;

                if (title.contains(type.getParseString())) {
                    parsedMask = type;
                    break;
                }
            }
        }

        currentMaskType = parsedMask;
    }

    public static ShamanMaskType getCurrentMaskType() {
        return currentMaskType;
    }
}
