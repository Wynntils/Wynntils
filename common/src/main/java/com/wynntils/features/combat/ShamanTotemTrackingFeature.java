/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.extension.EntityExtension;
import com.wynntils.models.abilities.event.TotemEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import java.util.Comparator;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class ShamanTotemTrackingFeature extends Feature {
    private static final int ENTITY_GLOWING_FLAG = 6;

    @Persisted
    private final Config<Boolean> highlightShamanTotems = new Config<>(true);

    @Persisted
    private final Config<CustomColor> firstTotemColor = new Config<>(CommonColors.WHITE);

    @Persisted
    private final Config<CustomColor> secondTotemColor = new Config<>(CommonColors.BLUE);

    @Persisted
    private final Config<CustomColor> thirdTotemColor = new Config<>(CommonColors.RED);

    @Persisted
    private final Config<CustomColor> fourthTotemColor = new Config<>(CommonColors.GREEN);

    public ShamanTotemTrackingFeature() {
        super(ProfileDefault.ENABLED);
    }

    @SubscribeEvent
    public void onTotemActivated(TotemEvent.Activated e) {
        if (!highlightShamanTotems.get()) return;

        int totemNumber = e.getTotemNumber();

        Entity totemEntity = e.getTotemDisplay().getVehicle();

        if (totemEntity == null) return;

        WynntilsMod.info("Found vehicle " + totemEntity.toString());

        CustomColor color =
                switch (totemNumber) {
                    case 1 -> firstTotemColor.get();
                    case 2 -> secondTotemColor.get();
                    case 3 -> thirdTotemColor.get();
                    case 4 -> fourthTotemColor.get();
                    default ->
                        throw new IllegalArgumentException(
                                "totemNumber should be 1, 2, 3 or 4! (color switch in #onTotemSummoned in ShamanTotemTrackingFeature");
                };

        ((EntityExtension) totemEntity).setGlowColor(color);

        totemEntity.setGlowingTag(true);
        totemEntity.setSharedFlag(ENTITY_GLOWING_FLAG, true);
    }

    private Display.ItemDisplay findTotemDisplay(Position totemPosition) {
        if (McUtils.mc().level == null) return null;

        double x = totemPosition.x();
        double y = totemPosition.y();
        double z = totemPosition.z();

        AABB search = new AABB(x - 1.25, y - 3.0, z - 1.25, x + 1.25, y + 0.25, z + 1.25);

        return McUtils.mc().level.getEntitiesOfClass(Display.ItemDisplay.class, search).stream()
                .filter(item -> {
                    boolean isBoatDisplay = item.itemRenderState().itemStack().is(Items.OAK_BOAT);
                    double yDelta = y - item.getY();
                    return (yDelta >= 0.75 && yDelta <= 2.75) && isBoatDisplay;
                })
                .min(Comparator.comparingDouble(item -> Math.abs((y - item.getY()) - 1.5)))
                .orElse(null);
    }
}
