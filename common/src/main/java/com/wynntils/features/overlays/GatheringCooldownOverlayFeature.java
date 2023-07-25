/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.overlays.OverlayPosition;
import com.wynntils.core.consumers.features.overlays.OverlaySize;
import com.wynntils.core.consumers.features.overlays.RenderState;
import com.wynntils.core.consumers.features.overlays.TextOverlay;
import com.wynntils.core.consumers.features.overlays.annotations.OverlayInfo;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class GatheringCooldownOverlayFeature extends Feature {
    private static final Pattern GATHERING_COOLDOWN_MESSAGE =
            Pattern.compile("§4You need to wait §c\\d+ seconds§4 after logging in to gather from this resource!");

    @OverlayInfo(renderAt = RenderState.PRE, renderType = RenderEvent.ElementType.GUI)
    private final GatheringCooldownOverlay gatheringCooldownOverlay = new GatheringCooldownOverlay();

    @SubscribeEvent
    public void onChat(ChatMessageReceivedEvent event) {
        if (event.getStyledText().matches(GATHERING_COOLDOWN_MESSAGE)) {
            gatheringCooldownOverlay.showGatheringCooldown = true;
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        gatheringCooldownOverlay.showGatheringCooldown = false;
    }

    public static class GatheringCooldownOverlay extends TextOverlay {
        private static final String TEMPLATE =
                "{if_str(gt(gathering_cooldown; 0);concat(string(gathering_cooldown); \"s gathering cooldown\");\"\")}";

        private boolean showGatheringCooldown = false;

        protected GatheringCooldownOverlay() {
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

        @Override
        public String getTemplate() {
            return showGatheringCooldown ? TEMPLATE : "";
        }

        @Override
        public String getPreviewTemplate() {
            return "10s gathering cooldown";
        }
    }
}
