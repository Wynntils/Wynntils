/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatValue;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.Optional;

public class ActualStatProvider extends ItemStatProvider<StatValue> {
    private final StatType statType;

    public ActualStatProvider(StatType statType) {
        this.statType = statType;
    }

    @Override
    public String getName() {
        return statType.getApiName();
    }

    @Override
    public String getDisplayName() {
        // A few overrides for clearer names
        // and to show units for non raw stats
        if (statType.getKey().equals("MISC_LEVELED_XP_BONUS")) {
            return "XP From Lv. Content";
        } else if (statType.getKey().equals("MISC_LEVELED_LOOT_BONUS")) {
            return "Loot From Lv. Content";
        } else {
            return switch (statType.getUnit()) {
                case PERCENT, PER_3_S, PER_5_S ->
                    statType.getDisplayName() + "(" + statType.getUnit().getDisplayName() + ")";
                default -> statType.getDisplayName();
            };
        }
    }

    @Override
    public String getDescription() {
        return getTranslation("description", statType.getDisplayName());
    }

    @Override
    public Optional<String> getGuideCategory() {
        String key = statType.getKey();

        if (key.startsWith("SKILL_")) {
            return Optional.of("Fixed Stats");
        }

        if (key.equals("MISC_HEALTH")
                || key.startsWith("MISC_HEALTH_REGEN")
                || key.equals("MISC_HEALING_EFFICIENCY")
                || key.equals("MISC_LIFE_STEAL")
                || key.equals("MISC_MANA_REGEN")
                || key.equals("MISC_MANA_STEAL")
                || key.equals("MISC_MAX_MANA_RAW")) {
            return Optional.of("Health & Mana");
        }

        if (key.equals("MISC_WALK_SPEED")
                || key.equals("MISC_SPRINT")
                || key.equals("MISC_SPRINT_REGEN")
                || key.equals("MISC_JUMP_HEIGHT")) {
            return Optional.of("Movement");
        }

        if (key.equals("MISC_ATTACK_SPEED")
                || key.equals("MISC_MAIN_ATTACK_RANGE")
                || key.equals("MISC_REFLECTION")
                || key.equals("MISC_THORNS")
                || key.equals("MISC_EXPLODING")
                || key.equals("MISC_POISON")
                || key.equals("MISC_KNOCKBACK")
                || key.equals("MISC_SLOW_ENEMY")
                || key.equals("MISC_WEAKEN_ENEMY")) {
            return Optional.of("Combat");
        }

        if (key.equals("MISC_STEALING")
                || key.equals("MISC_COMBAT_EXPERIENCE")
                || key.equals("MISC_LOOT")
                || key.equals("MISC_LOOT_QUALITY")
                || key.equals("MISC_GATHERING_EXPERIENCE")
                || key.equals("MISC_GATHERING_SPEED")
                || key.equals("MISC_SLAYING_XP")
                || key.equals("MISC_GATHERING_XP")
                || key.equals("MISC_DUNGEON_XP")
                || key.equals("MISC_LEVELED_XP_BONUS")
                || key.equals("MISC_LEVELED_LOOT_BONUS")) {
            return Optional.of("Bonuses");
        }

        if (key.equals("DEFENCE_EARTH")
                || key.equals("DEFENCE_THUNDER")
                || key.equals("DEFENCE_WATER")
                || key.equals("DEFENCE_FIRE")
                || key.equals("DEFENCE_AIR")
                || key.equals("DEFENCE_ELEMENTAL")) {
            return Optional.of("Defence");
        }

        if (key.startsWith("SPELL_")) {
            return Optional.of("Spell Costs");
        }

        if ((key.startsWith("DAMAGE_") && !key.equals("DAMAGE_FROM_MOBS"))
                || key.equals("CRITICAL_DAMAGE_BONUS")
                || key.equals("DAMAGE_TO_MOBS")) {
            return Optional.of("Damage");
        }

        return Optional.empty();
    }

    @Override
    public Optional<StatValue> getValue(WynnItem wynnItem) {
        if (wynnItem instanceof GearItem gearItem) {
            return handleGearItem(gearItem);
        }

        if (wynnItem instanceof IngredientItem ingredientItem) {
            return handleIngredientItem(ingredientItem);
        }

        return Optional.empty();
    }

    @Override
    public Optional<RangedValue> getExpectedRange() {
        return switch (statType.getKey()) {
            // Skills
            case "SKILL_STRENGTH", "SKILL_DEXTERITY", "SKILL_INTELLIGENCE", "SKILL_DEFENCE", "SKILL_AGILITY" ->
                Optional.of(RangedValue.of(-1000, 1000));

            // Health
            case "MISC_HEALTH" -> Optional.of(RangedValue.of(-10000, 10000));
            case "MISC_HEALTH_REGEN_RAW" -> Optional.of(RangedValue.of(-10000, 2000));
            case "MISC_HEALTH_REGEN_PERCENT" -> Optional.of(RangedValue.of(-500, 500));
            case "MISC_HEALING_EFFICIENCY" -> Optional.of(RangedValue.of(-1000, 100));

            // Mana
            case "MISC_MANA_REGEN", "MISC_MANA_STEAL" -> Optional.of(RangedValue.of(-250, 250));
            case "MISC_MAX_MANA_RAW" -> Optional.of(RangedValue.of(-250, 500));

            // Movement
            case "MISC_WALK_SPEED" -> Optional.of(RangedValue.of(-500, 500));
            case "MISC_SPRINT", "MISC_SPRINT_REGEN" -> Optional.of(RangedValue.of(-250, 500));
            case "MISC_JUMP_HEIGHT" -> Optional.of(RangedValue.of(-10, 10));

            // Combat misc
            case "MISC_ATTACK_SPEED" -> Optional.of(RangedValue.of(-100, 100));
            case "MISC_MAIN_ATTACK_RANGE" -> Optional.of(RangedValue.of(-500, 250));
            case "MISC_REFLECTION", "MISC_THORNS", "MISC_EXPLODING" -> Optional.of(RangedValue.of(-1000, 1000));
            case "MISC_POISON" -> Optional.of(RangedValue.of(-500000, 100000));
            case "MISC_LIFE_STEAL" -> Optional.of(RangedValue.of(-5000, 5000));
            case "MISC_KNOCKBACK" -> Optional.of(RangedValue.of(-500, 250));
            case "MISC_SLOW_ENEMY", "MISC_WEAKEN_ENEMY" -> Optional.of(RangedValue.of(0, 100));
            case "MISC_STEALING" -> Optional.of(RangedValue.of(-25, 50));
            case "MISC_COMBAT_EXPERIENCE",
                    "MISC_LOOT",
                    "MISC_LOOT_QUALITY",
                    "MISC_GATHERING_EXPERIENCE",
                    "MISC_GATHERING_SPEED",
                    "MISC_SLAYING_XP",
                    "MISC_GATHERING_XP",
                    "MISC_DUNGEON_XP",
                    "MISC_LEVELED_XP_BONUS",
                    "MISC_LEVELED_LOOT_BONUS" -> Optional.of(RangedValue.of(-100, 500));

            // Defences
            case "DEFENCE_EARTH",
                    "DEFENCE_THUNDER",
                    "DEFENCE_WATER",
                    "DEFENCE_FIRE",
                    "DEFENCE_AIR",
                    "DEFENCE_ELEMENTAL" -> Optional.of(RangedValue.of(-1000, 500));
            case "DEFENCE_TO_MOBS", "DAMAGE_FROM_MOBS" -> Optional.of(RangedValue.of(-500, 500));

            // Generic % damage
            case "DAMAGE_ANY_ALL_PERCENT",
                    "DAMAGE_ANY_NEUTRAL_PERCENT",
                    "DAMAGE_ANY_FIRE_PERCENT",
                    "DAMAGE_ANY_WATER_PERCENT",
                    "DAMAGE_ANY_AIR_PERCENT",
                    "DAMAGE_ANY_THUNDER_PERCENT",
                    "DAMAGE_ANY_EARTH_PERCENT",
                    "DAMAGE_ANY_RAINBOW_PERCENT",
                    "DAMAGE_SPELL_ALL_PERCENT",
                    "DAMAGE_SPELL_NEUTRAL_PERCENT",
                    "DAMAGE_SPELL_FIRE_PERCENT",
                    "DAMAGE_SPELL_WATER_PERCENT",
                    "DAMAGE_SPELL_AIR_PERCENT",
                    "DAMAGE_SPELL_THUNDER_PERCENT",
                    "DAMAGE_SPELL_EARTH_PERCENT",
                    "DAMAGE_SPELL_RAINBOW_PERCENT",
                    "DAMAGE_MAIN_ATTACK_ALL_PERCENT",
                    "DAMAGE_MAIN_ATTACK_NEUTRAL_PERCENT",
                    "DAMAGE_MAIN_ATTACK_FIRE_PERCENT",
                    "DAMAGE_MAIN_ATTACK_WATER_PERCENT",
                    "DAMAGE_MAIN_ATTACK_AIR_PERCENT",
                    "DAMAGE_MAIN_ATTACK_THUNDER_PERCENT",
                    "DAMAGE_MAIN_ATTACK_EARTH_PERCENT",
                    "DAMAGE_MAIN_ATTACK_RAINBOW_PERCENT" -> Optional.of(RangedValue.of(-1000, 1500));

            // Raw damage
            case "DAMAGE_ANY_ALL_RAW" -> Optional.of(RangedValue.of(-100000, 5000));
            case "DAMAGE_ANY_NEUTRAL_RAW",
                    "DAMAGE_ANY_FIRE_RAW",
                    "DAMAGE_ANY_WATER_RAW",
                    "DAMAGE_ANY_AIR_RAW",
                    "DAMAGE_ANY_THUNDER_RAW",
                    "DAMAGE_ANY_EARTH_RAW",
                    "DAMAGE_ANY_RAINBOW_RAW" -> Optional.of(RangedValue.of(-5000, 5000));
            case "DAMAGE_SPELL_ALL_RAW" -> Optional.of(RangedValue.of(-100000, 5000));
            case "DAMAGE_SPELL_NEUTRAL_RAW",
                    "DAMAGE_SPELL_FIRE_RAW",
                    "DAMAGE_SPELL_WATER_RAW",
                    "DAMAGE_SPELL_AIR_RAW",
                    "DAMAGE_SPELL_THUNDER_RAW",
                    "DAMAGE_SPELL_EARTH_RAW",
                    "DAMAGE_SPELL_RAINBOW_RAW" -> Optional.of(RangedValue.of(-5000, 5000));
            case "DAMAGE_MAIN_ATTACK_ALL_RAW" -> Optional.of(RangedValue.of(-100000, 100000));
            case "DAMAGE_MAIN_ATTACK_NEUTRAL_RAW",
                    "DAMAGE_MAIN_ATTACK_FIRE_RAW",
                    "DAMAGE_MAIN_ATTACK_WATER_RAW",
                    "DAMAGE_MAIN_ATTACK_AIR_RAW",
                    "DAMAGE_MAIN_ATTACK_THUNDER_RAW",
                    "DAMAGE_MAIN_ATTACK_EARTH_RAW",
                    "DAMAGE_MAIN_ATTACK_RAINBOW_RAW" -> Optional.of(RangedValue.of(-5000, 10000));
            case "CRITICAL_DAMAGE_BONUS" -> Optional.of(RangedValue.of(-500, 500));
            case "DAMAGE_TO_MOBS" -> Optional.of(RangedValue.of(-500, 500));

            // Spell costs
            case "SPELL_FIRST_SPELL_COST_PERCENT",
                    "SPELL_SECOND_SPELL_COST_PERCENT",
                    "SPELL_THIRD_SPELL_COST_PERCENT",
                    "SPELL_FOURTH_SPELL_COST_PERCENT" -> Optional.of(RangedValue.of(-200, 100));
            case "SPELL_FIRST_SPELL_COST_RAW",
                    "SPELL_SECOND_SPELL_COST_RAW",
                    "SPELL_THIRD_SPELL_COST_RAW",
                    "SPELL_FOURTH_SPELL_COST_RAW" -> Optional.of(RangedValue.of(-100, 100));
            default -> Optional.of(RangedValue.of(-1000, 1000));
        };
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GEAR, ItemProviderType.GEAR_INSTANCE, ItemProviderType.INGREDIENT);
    }

    private Optional<StatValue> handleIngredientItem(IngredientItem ingredientItem) {
        IngredientInfo ingredientInfo = ingredientItem.getIngredientInfo();

        if (ingredientInfo == null) {
            return Optional.empty();
        }

        List<Pair<StatType, RangedValue>> stats = ingredientInfo.variableStats();

        return stats.stream()
                .filter(pair -> pair.key().equals(statType))
                .map(pair -> new StatValue(new StatPossibleValues(pair.key(), pair.value(), 0, false), null))
                .findFirst();
    }

    private Optional<StatValue> handleGearItem(GearItem gearItem) {
        GearInfo gearInfo = gearItem.getItemInfo();
        StatPossibleValues possibleValues = gearInfo.getPossibleValues(statType);

        if (possibleValues == null) {
            return Optional.empty();
        }

        Optional<GearInstance> gearInstanceOpt = gearItem.getItemInstance();

        if (gearInstanceOpt.isEmpty()) {
            // Item guide item
            return Optional.of(new StatValue(possibleValues, null));
        }

        StatActualValue actualValue = gearInstanceOpt.get().getActualValue(statType);

        // The item is revealed, no actual stats yet
        if (actualValue == null) {
            return Optional.of(new StatValue(possibleValues, null));
        }

        return Optional.of(new StatValue(possibleValues, actualValue));
    }

    @Override
    public List<String> getAliases() {
        // Add "defense" as an alias for all defence stat types
        if (statType.getApiName().contains("defence")) {
            return List.of(statType.getApiName().replace("defence", "defense"));
        } else if (statType.getApiName().contains("Defence")) {
            return List.of(statType.getApiName().replace("Defence", "Defense"));
        }

        return super.getAliases();
    }
}
