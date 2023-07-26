/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GatheringCooldownOverlay extends TextOverlay {
    private static final Pattern GATHERING_COOLDOWN_MESSAGE =
            Pattern.compile("§4You need to wait §c\\d+ seconds§4 after logging in to gather from this resource!");

    private static final String TEMPLATE =
            "{if_str(gt(gathering_cooldown; 0);concat(string(gathering_cooldown); \"s gathering cooldown\");\"\")}";

    private boolean showGatheringCooldown = false;

    public GatheringCooldownOverlay() {
        super(
                new OverlayPosition(
                        165,
                        -5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.RIGHT,
                        OverlayPosition.AnchorSection.TOP_RIGHT),
                new OverlaySize(130, 20),
                HorizontalAlignment.RIGHT,
                VerticalAlignment.MIDDLE);
    }

    @SubscribeEvent
    public void onChat(ChatMessageReceivedEvent event) {
        if (event.getStyledText().matches(GATHERING_COOLDOWN_MESSAGE)) {
            showGatheringCooldown = true;
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        showGatheringCooldown = false;
    }

    @Override
    public String getTemplate() {
        return showGatheringCooldown ? TEMPLATE : "";
    }

    @Override
    public String getPreviewTemplate() {
        return "10s gathering cooldown";
    }
}
