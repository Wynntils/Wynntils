/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.models.token.event.TokenGatekeeperEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class TokenTrackerBellFeature extends Feature {
    @Persisted
    private final Config<Boolean> playSound = new Config<>(true);

    public TokenTrackerBellFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(
                        ConfigProfile.NEW_PLAYER, ConfigProfile.LITE, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onInventoryUpdated(TokenGatekeeperEvent.InventoryUpdated event) {
        if (!playSound.get()) return;

        // Do not play sound when depositing from the inventory
        if (event.getCount() < event.getOldCount()) return;

        CappedValue deposited = event.getGatekeeper().getDeposited();
        int collected = Models.Token.inInventory(event.getGatekeeper()) + deposited.current();

        if (collected < deposited.max()) return;
        // Do not keep playing the sound if we go too far over the needed amount
        if (collected > deposited.max() + 5) return;

        McUtils.mc().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BELL_BLOCK, 0.7f, 0.75f));
    }
}
