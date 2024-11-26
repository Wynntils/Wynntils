/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.models.profession.type.MaterialProfile;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.mc.type.Location;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class GatheringNodeHarvestLabelParser implements LabelParser<GatheringNodeHarvestLabelInfo> {
    // §7x1 [+3952§f Ⓒ§7 Woodcutting XP] §6[14.64%]
    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile(
            "(§.x[\\d\\.]+ )?(§.)?\\[\\+(§d)?(?<gain>\\d+)§f [ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ]§7 (?<name>.+) XP\\] §6\\[(?<current>[\\d.]+)%\\]");

    // §a+1§2 Dernic Wood§6 [§e✫§8✫✫§6]
    private static final Pattern HARVEST_PATTERN =
            Pattern.compile("§a\\+\\d+§2 (?<type>.+) (?<material>.+)§6 \\[(?<tier>§e✫(?:§8)?✫(?:§8)?✫)§6\\]");

    @Override
    public GatheringNodeHarvestLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        StyledText[] lines = label.split("\n");

        Matcher experienceMatcher = lines[0].getMatcher(EXPERIENCE_PATTERN);

        if (experienceMatcher.matches()) {
            ProfessionType profession = ProfessionType.fromString(experienceMatcher.group("name"));
            float gain = Float.parseFloat(experienceMatcher.group("gain"));
            float current = Float.parseFloat(experienceMatcher.group("current"));

            Optional<MaterialProfile> gatheredMaterial = Optional.empty();

            if (lines.length == 2) {
                Matcher materialMatcher = lines[1].getMatcher(HARVEST_PATTERN);

                if (materialMatcher.matches()) {
                    String type = materialMatcher.group("type");
                    String material = materialMatcher.group("material");
                    String tierGroup = materialMatcher.group("tier");
                    int tier =
                            switch (tierGroup) {
                                case "§e✫✫§8✫" -> 2;
                                case "§e✫✫✫" -> 3;
                                default -> 1;
                            };

                    gatheredMaterial = Optional.ofNullable(MaterialProfile.lookup(type, material, tier));
                }
            }

            return new GatheringNodeHarvestLabelInfo(
                    label, location, entity, profession, gain, current, gatheredMaterial);
        }

        return null;
    }
}
