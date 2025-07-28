/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.models.character.actionbar.matchers.CharacterWardrobeSegmentMacher;
import com.wynntils.models.character.actionbar.segments.CharacterWardrobeSegment;
import com.wynntils.models.characterstats.actionbar.segments.HealthBarSegment;
import java.util.List;
import net.neoforged.bus.api.SubscribeEvent;

public class CharacterWardrobeModel extends Model {
    private boolean inWardrobe = false;
    private boolean afterAnimation = false;

    public CharacterWardrobeModel() {
        super(List.of());
        Handlers.ActionBar.registerSegment(new CharacterWardrobeSegmentMacher());
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresent(CharacterWardrobeSegment.class, this::enableInWardrobe);
        event.runIfPresent(HealthBarSegment.class, this::disableInWardrobe);
    }

    @SubscribeEvent
    public void ContainerClickEvent(ContainerClickEvent event) {
        if (event.getItemStack().getCustomName() != null
                && event.getItemStack().getCustomName().getString().equals("§7Wardrobe")) inWardrobe = true;
    }

    private void enableInWardrobe(CharacterWardrobeSegment characterWardrobeSegment) {
        afterAnimation = true;
        inWardrobe = true;
    }

    private void disableInWardrobe(HealthBarSegment healthBarSegment) {
        if (afterAnimation) {
            inWardrobe = false;
            afterAnimation = false;
        }
    }

    public boolean isInWardrobe() {
        return inWardrobe;
    }
}
