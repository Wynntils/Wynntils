/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats;

import java.util.List;

public class LegacyStatListOrder {
    // Legacy order was defined by Athena as a fixed list. This was missing several stat types; I have
    // tried filling them in into "logical" places

    public static final List<String> LEGACY_ORDER = List.of(
            "attackSpeedBonus",
            "mainAttackDamageBonusRaw",
            "mainAttackDamageBonus",
            "mainAttackNeutralDamageBonusRaw",
            "mainAttackNeutralDamageBonus",
            "mainAttackElementalDamageBonusRaw",
            "mainAttackElementalDamageBonus",
            "mainAttackEarthDamageBonusRaw",
            "mainAttackEarthDamageBonus",
            "mainAttackThunderDamageBonusRaw",
            "mainAttackThunderDamageBonus",
            "mainAttackWaterDamageBonusRaw",
            "mainAttackWaterDamageBonus",
            "mainAttackFireDamageBonusRaw",
            "mainAttackFireDamageBonus",
            "mainAttackAirDamageBonusRaw",
            "mainAttackAirDamageBonus",
            "spellDamageBonusRaw",
            "spellDamageBonus",
            "spellNeutralDamageBonusRaw",
            "spellNeutralDamageBonus",
            "spellElementalDamageBonusRaw",
            "spellElementalDamageBonus",
            "spellEarthDamageBonusRaw",
            "spellEarthDamageBonus",
            "spellThunderDamageBonusRaw",
            "spellThunderDamageBonus",
            "spellWaterDamageBonusRaw",
            "spellWaterDamageBonus",
            "spellFireDamageBonusRaw",
            "spellFireDamageBonus",
            "spellAirDamageBonusRaw",
            "spellAirDamageBonus",
            "", // delimiter
            "healthBonus",
            "healthRegenRaw",
            "healthRegen",
            "lifeSteal",
            "manaRegen",
            "manaSteal",
            "", // delimiter
            "damageBonusRaw",
            "damageBonus",
            "neutralDamageBonusRaw",
            "neutralDamageBonus",
            "elementalDamageBonusRaw",
            "elementalDamageBonus",
            "earthDamageBonusRaw",
            "earthDamageBonus",
            "thunderDamageBonusRaw",
            "thunderDamageBonus",
            "waterDamageBonusRaw",
            "waterDamageBonus",
            "fireDamageBonusRaw",
            "fireDamageBonus",
            "airDamageBonusRaw",
            "airDamageBonus",
            "", // delimiter
            "bonusEarthDefense",
            "bonusThunderDefense",
            "bonusWaterDefense",
            "bonusFireDefense",
            "bonusAirDefense",
            "", // delimiter
            "exploding",
            "poison",
            "thorns",
            "reflection",
            "", // delimiter
            "speed",
            "sprint",
            "sprintRegen",
            "jumpHeight",
            "", // delimiter
            "soulPoints",
            "lootBonus",
            "lootQuality",
            "emeraldStealing",
            "xpBonus",
            "gatherXpBonus",
            "gatherSpeed",
            "", // delimiter
            "spellCostRaw1",
            "spellCostPct1",
            "spellCostRaw2",
            "spellCostPct2",
            "spellCostRaw3",
            "spellCostPct3",
            "spellCostRaw4",
            "spellCostPct4");
}
