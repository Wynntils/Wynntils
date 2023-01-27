/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats;

import java.util.List;

public class StatOrderDefault {

    public static final List<String> WYNNCRAFT_ORDER = List.of(
            // DEFENCE
            "bonusFireDefense",
            "bonusWaterDefense",
            "bonusAirDefense",
            "bonusThunderDefense",
            "bonusEarthDefense",


            // misc1, confirmed
            "healthRegen",
            "manaRegen",
            "lifeSteal",
            "manaSteal",
            "xpBonus",
            "lootBonus",
            "reflection",
            "thorns",
            "exploding",
            "speed",
            "attackSpeedBonus",
            "poison",
            "healthBonus",
            "soulPoints",
            "emeraldStealing",
            "healthRegenRaw",

            // defence ends, misc2
            "sprint",
            "sprintRegen",
            // misc2 ends,
            // misc3
            "jumpHeight",

            // not used except crafted, which does not have an order...
            "gatherXpBonus",
            "gatherSpeed",
            "lootQuality",


            // DAMAGE STARTS HERE
            // ORDER IS:
            // First Attack Type (any, spell, main)
            // Then for each attach type, DamageType (any, <all elements>, rainbow, neutral)
            // then: pecerent, RAW

            "damageBonus",
            "fireDamageBonus",
            "waterDamageBonus",
            "airDamageBonus",
            "thunderDamageBonus",
            "earthDamageBonus",
            "elementalDamageBonus",
            "neutralDamageBonus",
            "damageBonusRaw",
            "fireDamageBonusRaw",
            "waterDamageBonusRaw",
            "earthDamageBonusRaw",
            "thunderDamageBonusRaw",
            "airDamageBonusRaw",
            "elementalDamageBonusRaw",
            "neutralDamageBonusRaw",
            "spellDamageBonus",
            "spellFireDamageBonus",
            "spellWaterDamageBonus",
            "spellEarthDamageBonus",
            "spellThunderDamageBonus",
            "spellAirDamageBonus",
            "spellElementalDamageBonus",
            "spellNeutralDamageBonus",
            "spellDamageBonusRaw",
            "spellFireDamageBonusRaw",
            "spellWaterDamageBonusRaw",
            "spellEarthDamageBonusRaw",
            "spellThunderDamageBonusRaw",
            "spellAirDamageBonusRaw",
            "spellElementalDamageBonusRaw",
            "spellNeutralDamageBonusRaw",
            "mainAttackDamageBonus",
            "mainAttackFireDamageBonus",
            "mainAttackWaterDamageBonus",
            "mainAttackAirDamageBonus",
            "mainAttackThunderDamageBonus",
            "mainAttackEarthDamageBonus",
            "mainAttackElementalDamageBonus",
            "mainAttackNeutralDamageBonus",
            "mainAttackDamageBonusRaw",
            "mainAttackFireDamageBonusRaw",
            "mainAttackWaterDamageBonusRaw",
            "mainAttackEarthDamageBonusRaw",
            "mainAttackThunderDamageBonusRaw",
            "mainAttackAirDamageBonusRaw",
            "mainAttackElementalDamageBonusRaw",
            "mainAttackNeutralDamageBonusRaw",
            // DAMAGE ENDS HERE

            // spell costs
            "spellCostPct1",
            "spellCostRaw1",
            "spellCostPct2",
            "spellCostRaw2",
            "spellCostPct3",
            "spellCostRaw3",
            "spellCostPct4",
            "spellCostRaw4"
            // spell cost ends,

            );

    public static List<String> getWynncraftOrder() {
        return WYNNCRAFT_ORDER;
    }
}
