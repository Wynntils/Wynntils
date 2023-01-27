/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats;

import java.util.List;

public class StatOrderAthena {
    public static List<String> getAthenaOrder() {
        return List.of(
                "attackSpeedBonus",
                "mainAttackDamageBonusRaw",
                "mainAttackDamageBonus",
                "spellDamageBonusRaw",
                "spellDamageBonus",
                "spellThunderDamageBonusRaw",
                "spellFireDamageBonusRaw",
                "spellAirDamageBonusRaw",
                "spellEarthDamageBonusRaw",
                "spellWaterDamageBonusRaw",
                "healthBonus",
                "healthRegenRaw",
                "healthRegen",
                "lifeSteal",
                "manaRegen",
                "manaSteal",
                "earthDamageBonus",
                "thunderDamageBonus",
                "waterDamageBonus",
                "fireDamageBonus",
                "airDamageBonus",
                "bonusEarthDefense",
                "bonusThunderDefense",
                "bonusWaterDefense",
                "bonusFireDefense",
                "bonusAirDefense",
                "exploding",
                "poison",
                "thorns",
                "reflection",
                "speed",
                "sprint",
                "sprintRegen",
                "jumpHeight",
                "soulPoints",
                "lootBonus",
                "lootQuality",
                "emeraldStealing",
                "xpBonus",
                "gatherXpBonus",
                "gatherSpeed",
                "spellCostRaw1",
                "spellCostPct1",
                "spellCostRaw2",
                "spellCostPct2",
                "spellCostRaw3",
                "spellCostPct3",
                "spellCostRaw4",
                "spellCostPct4");
    }

    /**
     STATS THAT ARE MISSED BY ATHENA:
     "airDamageBonusRaw",
     "damageBonus",
     "damageBonusRaw",
     "earthDamageBonusRaw",
     "elementalDamageBonus",
     "elementalDamageBonusRaw",
     "fireDamageBonusRaw",
     "mainAttackAirDamageBonus",
     "mainAttackAirDamageBonusRaw",
     "mainAttackEarthDamageBonus",
     "mainAttackEarthDamageBonusRaw",
     "mainAttackElementalDamageBonus",
     "mainAttackElementalDamageBonusRaw",
     "mainAttackFireDamageBonus",
     "mainAttackFireDamageBonusRaw",
     "mainAttackNeutralDamageBonus",
     "mainAttackNeutralDamageBonusRaw",
     "mainAttackThunderDamageBonus",
     "mainAttackThunderDamageBonusRaw",
     "mainAttackWaterDamageBonus",
     "mainAttackWaterDamageBonusRaw",
     "neutralDamageBonus",
     "neutralDamageBonusRaw",
     "spellAirDamageBonus",
     "spellEarthDamageBonus",
     "spellElementalDamageBonus",
     "spellElementalDamageBonusRaw",
     "spellFireDamageBonus",
     "spellNeutralDamageBonus",
     "spellNeutralDamageBonusRaw",
     "spellThunderDamageBonus",
     "spellWaterDamageBonus",
     "thunderDamageBonusRaw",
     "waterDamageBonusRaw",

     */

    /*
    start new groups at:
    attackSpeedBonus - "attack stuff"
    healthBonus - "health/mana stuff"
    bonusEarthDamage - "damage stuff"
    bonusEarthDefense - "defence stuff"
    exploding - "passive damage"
    speed - "movement stuff"
    soulPoints - "XP/Gathering stuff"
    spellCostRaw1 - "spell stuff"
     */
}
