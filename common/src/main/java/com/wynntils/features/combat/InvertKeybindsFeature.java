/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.KeyMappingEvent;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.neoforged.bus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.COMBAT)
public class InvertKeybindsFeature extends Feature {
    @Persisted
    public final Config<Boolean> invertWarrior = new Config<>(false);

    @Persisted
    public final Config<Boolean> invertArcher = new Config<>(true);

    @Persisted
    public final Config<Boolean> invertAssassin = new Config<>(false);

    @Persisted
    public final Config<Boolean> invertMage = new Config<>(false);

    @Persisted
    public final Config<Boolean> invertShaman = new Config<>(false);

    private final Map<InputConstants.Key, InputConstants.Key> activeRemappings = new HashMap<>();

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

    public InputConstants.Key remapKey(InputConstants.Key key) {
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
        if (!switch (Models.Character.getClassType()) {
            case MAGE -> invertMage.get();
            case ARCHER -> invertArcher.get();
            case WARRIOR -> invertWarrior.get();
            case ASSASSIN -> invertAssassin.get();
            case SHAMAN -> invertShaman.get();
            default -> false;
        }) {
            return null;
        }

        // Ensure the held item is a weapon
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(InventoryUtils.getItemInHand());
        if (wynnItemOpt.isEmpty()
                || !(wynnItemOpt.get() instanceof GearTypeItemProperty gearTypeItem)
                || !gearTypeItem.getGearType().isWeapon()) return null;

        return target.key;
    }
}
