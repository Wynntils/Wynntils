/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.event.KeyInputEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.containers.containers.CharacterSelectionContainer;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class CharacterSelectionUtilitiesFeature extends Feature {
    @Persisted
    private final Config<Boolean> blockThirdPerson = new Config<>(true);

    @Persisted
    private final Config<Boolean> hideCrosshair = new Config<>(true);

    @SubscribeEvent
    public void onInventoryKeyPress(InventoryKeyPressEvent e) {
        if (!(Models.Container.getCurrentContainer() instanceof CharacterSelectionContainer)) return;

        KeyMapping[] keyHotbarSlots = McUtils.options().keyHotbarSlots;
        List<Integer> validSlots = Models.CharacterSelection.getValidCharacterSlots();

        for (int i = 0; i < Math.min(keyHotbarSlots.length, validSlots.size()); i++) {
            if (!keyHotbarSlots[i].matches(e.getKeyCode(), e.getScanCode())) continue;

            int slot = validSlots.get(i);
            Models.CharacterSelection.playWithCharacter(slot);
            break;
        }
    }

    @SubscribeEvent
    public void onKeyPress(KeyInputEvent e) {
        if (!blockThirdPerson.get()) return;
        if (Models.WorldState.getCurrentState() != WorldState.CHARACTER_SELECTION) return;

        KeyMapping perspectiveKey = McUtils.options().keyTogglePerspective;
        if (perspectiveKey.matches(e.getKey(), e.getScanCode())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent e) {
        if (!blockThirdPerson.get()) return;
        if (e.getNewState() != WorldState.CHARACTER_SELECTION) return;

        McUtils.options().setCameraType(CameraType.FIRST_PERSON);
    }

    @SubscribeEvent
    public void onRenderCrosshair(RenderEvent.Pre event) {
        if (!hideCrosshair.get()) return;
        if (event.getType() != RenderEvent.ElementType.CROSSHAIR) return;
        if (Models.WorldState.getCurrentState() != WorldState.CHARACTER_SELECTION) return;

        event.setCanceled(true);
    }
}
