/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.label;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.models.profession.type.MaterialProfile;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.Pair;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;

public class GatheringNodeLabelParser implements LabelParser<ProfessionGatheringNodeLabelInfo> {
    // A few nodes have an extra suffix so handle them here so that SourceMaterial
    // does not need a "node name" field
    private static final Pattern GATHERING_NODE_LABEL = Pattern.compile(
            "^§(?<resourceColor>.)(?:Cembra )?(?<resourceName>.+?)(?: Roots| Seed| Fish| Eel)?\\n§(?:a✔|c✖)(?: §f|§f ).(?: §7|§7 )(?:Woodcutting|Farming|Fishing|Mining) Lv\\.? Min: §f(?<lvlMin>\\d+)(?:\\n.*)+$");

    @Override
    public ProfessionGatheringNodeLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        if (label.isEmpty()) return null;

        Matcher matcher = label.getMatcher(GATHERING_NODE_LABEL);
        if (matcher.matches()) {
            String resourceColor = matcher.group("resourceColor");
            String resourceName = matcher.group("resourceName");
            Integer lvlMin = Integer.parseInt(matcher.group("lvlMin"), 10);

            Optional<Pair<MaterialProfile.MaterialType, MaterialProfile.SourceMaterial>> materialLookup =
                    MaterialProfile.findByMaterialName(resourceName, ChatFormatting.getByCode(resourceColor.charAt(0)));

            if (materialLookup.isEmpty()) {
                WynntilsMod.warn("Failed to find material with name " + resourceName + " and color " + resourceColor);
                return null;
            }

            Pair<MaterialProfile.MaterialType, MaterialProfile.SourceMaterial> materialTypeSourceMaterialPair =
                    materialLookup.get();
            MaterialProfile.MaterialType materialType = materialTypeSourceMaterialPair.key();
            MaterialProfile.SourceMaterial sourceMaterial = materialTypeSourceMaterialPair.value();

            if (sourceMaterial.level() != lvlMin) {
                WynntilsMod.warn("Resource Node with material " + resourceName
                        + " has level requirement " + lvlMin
                        + " but we're expecting "
                        + sourceMaterial.level());
            }

            return new ProfessionGatheringNodeLabelInfo(
                    label, resourceName + " Node", location, entity, sourceMaterial, materialType);
        }

        return null;
    }
}
