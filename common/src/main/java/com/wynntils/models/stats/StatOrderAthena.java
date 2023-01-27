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
                "healthBonus",
                "healthRegenRaw",
                "healthRegen",
                "lifeSteal",
                "manaRegen",
                "manaSteal",
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

    /*
    start new groups at:
    attackSpeedBonus - "attack stuff"
    healthBonus - "health/mana stuff"
    damageBonusRaw - "damage stuff"
    bonusEarthDefense - "defence stuff"
    exploding - "passive damage"
    speed - "movement stuff"
    soulPoints - "XP/Gathering stuff"
    spellCostRaw1 - "spell stuff"
     */
}
