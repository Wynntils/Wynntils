/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.profession.type.HarvestInfo;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Optional;
import com.wynntils.templates.annotations.TemplateFunction;

//Functions are accessed via reflection
@SuppressWarnings("unused")
public class ProfessionFunctions {

    @TemplateFunction(name = "profession_xp", aliases = { "prof_xp" })
    public static CappedValue professionXpFunction(String profession) {
        ProfessionType professionType = ProfessionType.fromString(profession);
        if (professionType == null)
            return CappedValue.EMPTY;
        return Models.Profession.getXP(professionType);
    }

    @TemplateFunction(name = "profession_level", aliases = { "prof_lvl" })
    public static int professionLevelFunction(String profession) {
        ProfessionType professionType = ProfessionType.fromString(profession);
        if (professionType == null)
            return -1;
        return Models.Profession.getLevel(professionType);
    }

    @TemplateFunction(name = "profession_percentage", aliases = { "prof_pct" })
    public static double professionPercentageFunction(String profession) {
        ProfessionType professionType = ProfessionType.fromString(profession);
        if (professionType == null)
            return -1.0;
        return Models.Profession.getProgress(professionType);
    }

    @TemplateFunction(name = "profession_xp_per_minute_raw", aliases = { "prof_xpm_raw" })
    public static int professionXpPerMinuteRawFunction(String profession) {
        ProfessionType professionType = ProfessionType.fromString(profession);
        if (professionType == null)
            return -1;
        return (int) Models.Profession.getRawXpGainInLastMinute().get(professionType).stream().mapToDouble(Float::doubleValue).sum();
    }

    @TemplateFunction(name = "profession_xp_per_minute", aliases = { "prof_xpm" })
    public static String professionXpPerMinuteFunction(String profession) {
        ProfessionType professionType = ProfessionType.fromString(profession);
        if (professionType == null)
            return "Invalid profession";
        return StringUtils.integerToShortString((int) Models.Profession.getRawXpGainInLastMinute().get(professionType).stream().mapToDouble(Float::doubleValue).sum());
    }

    @TemplateFunction(name = "last_harvest_resource_type")
    public static String lastHarvestResourceTypeFunction() {
        Optional<HarvestInfo> lastHarvest = Models.Profession.getLastHarvest();
        if (lastHarvest.isEmpty())
            return "";
        return lastHarvest.get().harvestMaterial().resourceType().name();
    }

    @TemplateFunction(name = "last_harvest_material_type")
    public static String lastHarvestMaterialTypeFunction() {
        Optional<HarvestInfo> lastHarvest = Models.Profession.getLastHarvest();
        if (lastHarvest.isEmpty())
            return "";
        return lastHarvest.get().harvestMaterial().resourceType().getMaterialType().name();
    }

    @TemplateFunction(name = "last_harvest_material_name")
    public static String lastHarvestMaterialNameFunction() {
        Optional<HarvestInfo> lastHarvest = Models.Profession.getLastHarvest();
        if (lastHarvest.isEmpty())
            return "";
        return lastHarvest.get().harvestMaterial().sourceMaterial().name();
    }

    @TemplateFunction(name = "last_harvest_material_level")
    public static int lastHarvestMaterialLevelFunction() {
        Optional<HarvestInfo> lastHarvest = Models.Profession.getLastHarvest();
        if (lastHarvest.isEmpty())
            return -1;
        return lastHarvest.get().harvestMaterial().sourceMaterial().level();
    }

    @TemplateFunction(name = "last_harvest_material_tier")
    public static int lastHarvestMaterialTierFunction() {
        Optional<HarvestInfo> lastHarvest = Models.Profession.getLastHarvest();
        if (lastHarvest.isEmpty())
            return -1;
        return lastHarvest.get().harvestMaterial().tier();
    }

    @TemplateFunction(name = "last_harvest_xp_gain")
    public static float lastHarvestXpGainFunction() {
        Optional<HarvestInfo> lastHarvest = Models.Profession.getLastHarvest();
        if (lastHarvest.isEmpty())
            return -1f;
        return lastHarvest.get().xpGain();
    }

    @TemplateFunction(name = "material_dry_streak", aliases = { "mat_dry" })
    public static int materialDryStreak() {
        return Models.Profession.getProfessionDryStreak();
    }

    @TemplateFunction(name = "last_profession_xp_gain")
    public static String lastProfessionXpGainFunction() {
        Optional<ProfessionType> profession = Models.Profession.getLastProfessionXpGain();
        if (profession.isEmpty())
            return "";
        return profession.get().getDisplayName();
    }
}
