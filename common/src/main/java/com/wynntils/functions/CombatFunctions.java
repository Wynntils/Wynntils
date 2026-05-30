/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.combat.label.DebuffType;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Time;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class CombatFunctions {
    @TemplateFunction(name = "area_damage_per_second", aliases = "adps")
    public static long areaDamagePerSecondFunction() {
        return Models.Combat.getAreaDamagePerSecond();
    }

    @TemplateFunction(name = "area_damage_average", aliases = "adavg")
    public static double areaDamageAverageFunction(int seconds) {
        return Models.Combat.getAverageAreaDamagePerSecond(seconds);
    }

    @TemplateFunction(
            name = "total_area_damage",
            aliases = {"total_dmg", "tdmg"})
    public static double totalAreaDamageFunction(int seconds) {
        return Models.Combat.getTotalAreaDamageOverSeconds(seconds);
    }

    @TemplateFunction(
            name = "blocks_above_ground",
            aliases = {"agl", "above_ground_level"})
    public static double blocksAboveGroundFunction() {
        return Models.CharacterStats.getBlocksAboveGround();
    }

    @TemplateFunction(name = "kills_per_minute", aliases = "kpm")
    public static int killsPerMinuteFunction(boolean includeShared) {
        return Models.Combat.getKillsPerMinute(includeShared);
    }

    @TemplateFunction(
            name = "last_spell_name",
            aliases = {"recast_name"})
    public static String lastSpellNameFunction(boolean burst) {
        return burst ? Models.Spell.getLastBurstSpellName() : Models.Spell.getLastSpellName();
    }

    @TemplateFunction(
            name = "last_spell_repeat_count",
            aliases = {"recast_count"})
    public static int lastSpellRepeatCountFunction(boolean burst) {
        return burst ? Models.Spell.getRepeatedBurstSpellCount() : Models.Spell.getRepeatedSpellCount();
    }

    @TemplateFunction(
            name = "ticks_since_last_spell",
            aliases = {"recast_ticks"})
    public static int ticksSinceLastSpellFunction(boolean burst) {
        return burst ? Models.Spell.getTicksSinceCastBurst() : Models.Spell.getTicksSinceCast();
    }

    @TemplateFunction(name = "focused_mob_name", aliases = "foc_mob")
    public static String focusedMobNameFunction() {
        return Models.Combat.getFocusedMobName();
    }

    @TemplateFunction(name = "focused_mob_health", aliases = "foc_mob_hp")
    public static long focusedMobHealthFunction() {
        return Models.Combat.getFocusedMobHealth();
    }

    @TemplateFunction(name = "focused_mob_health_percent", aliases = "foc_mob_hp_pct")
    public static CappedValue focusedMobHealthPercentFunction() {
        return Models.Combat.getFocusedMobHealthPercent();
    }

    @TemplateFunction(name = "last_damage_dealt", aliases = "last_dam")
    public static Time lastDamageDealtFunction() {
        return Time.of(Models.Combat.getLastDamageDealtTimestamp());
    }

    @TemplateFunction(name = "time_since_last_damage_dealt", aliases = "last_dam_ms")
    public static long timeSinceLastDamageDealthFunction() {
        return System.currentTimeMillis() - Models.Combat.getLastDamageDealtTimestamp();
    }

    @TemplateFunction(name = "last_kill")
    public static Time lastKillFunction(boolean includeShared) {
        return Time.of(Models.Combat.getLastKillTimestamp(includeShared));
    }

    @TemplateFunction(name = "time_since_last_kill", aliases = "last_kill_ms")
    public static long timeSinceLastKillFunction(boolean includeShared) {
        return System.currentTimeMillis() - Models.Combat.getLastKillTimestamp(includeShared);
    }

    @TemplateFunction(name = "targeted_mob_debuff_count")
    public static int targetedMobDebuffValueFunction(
            String debuffName, double range, double horizontalFovDegrees, double verticalFovDegrees) {
        DebuffType debuffType = DebuffType.fromName(debuffName);
        if (debuffType == null) return 0;

        return Models.Combat.getTargetedDebuffCount(range, horizontalFovDegrees, verticalFovDegrees, debuffType);
    }

    @TemplateFunction(name = "debuffs_in_radius")
    public static int debuffsInRadiusValueFunction(String debuffName, double radius) {
        DebuffType debuffType = DebuffType.fromName(debuffName);
        if (debuffType == null) return 0;

        return Models.Combat.getDebuffCountInRadius(radius, debuffType);
    }

    @TemplateFunction(name = "ticks_since_specific_spell")
    public static int ticksSinceSpecificSpellFunction(String spellName) {
        return Models.Spell.getTicksSinceCast(spellName);
    }

    @TemplateFunction(name = "spell_name_from_direction")
    public static String spellNameFromDirectionFunction(String spellDirection, String className) {
        ClassType classType = ClassType.fromName(className);
        SpellType spellType = SpellType.fromSpellString(classType, spellDirection);
        return spellType == null ? "" : spellType.getName();
    }

    @TemplateFunction(name = "spell_name_from_number")
    public static String spellNameFromNumberFunction(int spellNumber, String className) {
        SpellType spellType = SpellType.forClass(ClassType.fromName(className), spellNumber);
        return spellType == null ? "" : spellType.getName();
    }
}
