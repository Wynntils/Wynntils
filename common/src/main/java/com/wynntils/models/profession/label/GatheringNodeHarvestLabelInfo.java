/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.models.profession.type.HarvestMaterial;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.mc.type.Location;
import java.util.Optional;
import net.minecraft.world.entity.Entity;

public class GatheringNodeHarvestLabelInfo extends LabelInfo {
    private final ProfessionType professionType;
    private final float xpGain;
    private final float currentXp;
    private final Optional<HarvestMaterial> harvestMaterial;

    public GatheringNodeHarvestLabelInfo(
            StyledText label,
            Location location,
            Entity entity,
            ProfessionType professionType,
            float xpGain,
            float currentXp,
            Optional<HarvestMaterial> harvestMaterial) {
        super(label, location, entity);
        this.professionType = professionType;
        this.xpGain = xpGain;
        this.currentXp = currentXp;
        this.harvestMaterial = harvestMaterial;
    }

    public ProfessionType getProfessionType() {
        return professionType;
    }

    public float getXpGain() {
        return xpGain;
    }

    public float getCurrentXp() {
        return currentXp;
    }

    public Optional<HarvestMaterial> getHarvestMaterial() {
        return harvestMaterial;
    }
}
