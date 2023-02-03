package com.wynntils.models.gear.ingredients;

import com.wynntils.models.gear.type.GearMetaInfo;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.stats.FixedStats;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import java.util.List;

/*
 json fields:
      "name"
      "tier"
      "skills"
      "level"

      "sprite"
      "displayName"

      "itemOnlyIDs"
      "consumableOnlyIDs"
      "identifications"
      "ingredientPositionModifiers"
 */
public record IngredientInfo(String name,
                             int tier,
                             GearType type,
                             int powderSlots,
                             GearMetaInfo metaInfo,
                             GearRequirements requirements,
                             FixedStats fixedStats,
                             List<Pair<StatType, StatPossibleValues>> variableStats) {
}
