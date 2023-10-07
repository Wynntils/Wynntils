/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.models.profession.type.MaterialProfile;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.Pair;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;

public class GatheringNodeLabelParser implements LabelParser {
    // Note: At the moment, only Dernic appends to the end of the label, but not consistently..
    private static final Pattern GATHERING_NODE_LABEL = Pattern.compile("^§(.)(.+?)(:?\\s(Fish|Seed|Ore|Wood))?$");

    @Override
    public LabelInfo getInfo(StyledText label, Location location) {
        Matcher matcher = label.getMatcher(GATHERING_NODE_LABEL);
        if (matcher.matches()) {
            Optional<Pair<MaterialProfile.MaterialType, MaterialProfile.SourceMaterial>> materialLookup =
                    MaterialProfile.findByMaterialName(
                            matcher.group(2),
                            ChatFormatting.getByCode(matcher.group(1).charAt(0)));

            return materialLookup
                    .map(materialTypeSourceMaterialPair -> new ProfessionGatheringNodeLabelInfo(
                            label,
                            matcher.group(2) + " Node",
                            location,
                            materialTypeSourceMaterialPair.value(),
                            materialTypeSourceMaterialPair.key()))
                    .orElse(null);
        }

        return null;
    }
}
