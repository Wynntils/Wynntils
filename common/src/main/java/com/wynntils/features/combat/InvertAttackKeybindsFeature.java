/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.KeyMappingEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import com.wynntils.utils.wynn.ItemUtils;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class InvertAttackKeybindsFeature extends Feature {
    @Persisted
    private final Config<Boolean> invertWarrior = new Config<>(false);

    @Persisted
    private final Config<Boolean> invertArcher = new Config<>(true);

    @Persisted
    private final Config<Boolean> invertAssassin = new Config<>(false);

    @Persisted
    private final Config<Boolean> invertMage = new Config<>(false);

    @Persisted
    private final Config<Boolean> invertShaman = new Config<>(false);

    private final Map<InputConstants.Key, InputConstants.Key> activeRemappings = new HashMap<>();

    public InvertAttackKeybindsFeature() {
        super(ProfileDefault.DISABLED);
    }

    @SubscribeEvent
    public void onKeyMappingOperation(KeyMappingEvent event) {
        switch (event.getOperation()) {
            case SET -> {
                InputConstants.Key target = remapKey(event.getKey());
                if (target != null) {
                    event.setCanceled(true);
                    KeyMapping.set(target, true);
                    activeRemappings.put(event.getKey(), target);
                }
            }
            case UNSET -> {
                InputConstants.Key target = activeRemappings.remove(event.getKey());
                if (target != null) {
                    event.setCanceled(true);
                    KeyMapping.set(target, false);
                }
            }
            case CLICK -> {
                InputConstants.Key target = activeRemappings.get(event.getKey());
                if (target != null) {
                    event.setCanceled(true);
                    KeyMapping.click(target);
                }
            }
        }
    }

    private boolean isInvertingForClass(ClassType classType) {
        return switch (Models.Character.getClassType()) {
            case MAGE -> invertMage.get();
            case ARCHER -> invertArcher.get();
            case WARRIOR -> invertWarrior.get();
            case ASSASSIN -> invertAssassin.get();
            case SHAMAN -> invertShaman.get();
            default -> false;
        };
    }

    private InputConstants.Key remapKey(InputConstants.Key key) {
        Options options = McUtils.options();

        // Ensure the key is either the attack or spell key
        KeyMapping target;
        if (key.equals(options.keyAttack.key)) {
            target = options.keyUse;
        } else if (key.equals(options.keyUse.key)) {
            target = options.keyAttack;
        } else {
            return null;
        }

        // Ensure key inversion is enabled for the current class
        if (!isInvertingForClass(Models.Character.getClassType())) return null;

        // Ensure the held item is a weapon
        if (!ItemUtils.isWeapon(InventoryUtils.getItemInHand())) return null;

        return target.key;
    }
}
