/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.NpcDialogueOverlay;
import org.lwjgl.glfw.GLFW;

@StartDisabled
@ConfigCategory(Category.OVERLAYS)
public class NpcDialogueOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final NpcDialogueOverlay npcDialogueOverlay = new NpcDialogueOverlay();

    @RegisterKeyBind
    public final KeyBind cancelAutoProgressKeybind =
            new KeyBind("Cancel Dialog Auto Progress", GLFW.GLFW_KEY_Y, false, npcDialogueOverlay::cancelAutoProgress);
}
