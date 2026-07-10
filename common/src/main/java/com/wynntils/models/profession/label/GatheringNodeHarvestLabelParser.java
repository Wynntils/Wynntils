/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.label;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.models.profession.type.HarvestMaterial;
import com.wynntils.models.profession.type.MaterialInfo;
import com.wynntils.models.profession.type.MiscGatheringType;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.profession.type.ResourceType;
import com.wynntils.models.profession.type.SourceMaterial;
import com.wynntils.utils.mc.type.Location;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class GatheringNodeHarvestLabelParser implements LabelParser<GatheringNodeHarvestLabelInfo> {
    // Test in GatheringNodeHarvestLabelParser_EXPERIENCE_PATTERN
    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile(
            "(§(.+?)\\[§(.+?)x\\d§(.+?)\\] )?§(.+?)\\+(§d)?(?<gain>\\d+) §7[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ] (?<name>.+) XP §6\\[(?<current>[\\d.]+)%\\]");

    // Test in GatheringNodeHarvestLabelParser_HARVEST_PATTERN
    private static final Pattern HARVEST_PATTERN = Pattern.compile(
            "(§(.+?)\\[§(.+?)x\\d§(.+?)\\] )?§(.+?)\\+\\d+ §7(?<type>.+) (?<material>.+)§6 \\[§e✫((?:§8)?✫(?:§8)?)✫§6\\]");

    @Override
    public GatheringNodeHarvestLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        StyledText[] lines = label.split("\n");

        if (lines.length == 0) return null;

        Matcher experienceMatcher = lines[0].getMatcher(EXPERIENCE_PATTERN);

        if (experienceMatcher.matches()) {
            ProfessionType profession = ProfessionType.fromString(experienceMatcher.group("name"));
            float gain = Float.parseFloat(experienceMatcher.group("gain"));
            float current = Float.parseFloat(experienceMatcher.group("current"));

            Optional<HarvestMaterial> gatheredMaterial = Optional.empty();

            if (lines.length == 2) {
                Matcher materialMatcher = lines[1].getMatcher(HARVEST_PATTERN);

                if (materialMatcher.matches()) {
                    String type = materialMatcher.group("type");
                    String material = materialMatcher.group("material");

                    // Tier isn't shown in the label anymore
                    gatheredMaterial = getHarvestMaterial(type, material, 1);
                }
            }

            return new GatheringNodeHarvestLabelInfo(
                    label, location, entity, profession, gain, current, gatheredMaterial);
        }

        return null;
    }

    private Optional<HarvestMaterial> getHarvestMaterial(String sourceMaterialName, String resourceTypeName, int tier) {
        ResourceType resourceType = ResourceType.fromString(resourceTypeName);
        if (resourceType == null) return Optional.empty();

        Optional<MaterialInfo> materialInfo =
                Models.Profession.findMaterialInfoFromSourceAndResource(sourceMaterialName, resourceTypeName);
        if (materialInfo.isPresent()) {
            return Optional.of(new HarvestMaterial(
                    resourceType,
                    new SourceMaterial(sourceMaterialName, materialInfo.get().level()),
                    tier));
        }

        MiscGatheringType miscGatheringType = MiscGatheringType.fromResourceName(sourceMaterialName);
        if (miscGatheringType == null
                || miscGatheringType.getProfessionType()
                        != resourceType.getMaterialType().getProfessionType()) {
            return Optional.empty();
        }

        return Optional.of(new HarvestMaterial(
                resourceType, new SourceMaterial(sourceMaterialName, miscGatheringType.getLevel()), tier));
    }
}
