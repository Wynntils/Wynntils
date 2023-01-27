/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats;

import java.util.List;

public class StatOrderAthena {

    public static List<String> getAthenaOrder() {
        return List.of(
                "strengthPoints",
                "dexterityPoints",
                "intelligencePoints",
                "defensePoints",
                "agilityPoints",
                "attackSpeedBonus",
                "damageBonusRaw",
                "damageBonus",
                "spellDamageRaw",
                "spellDamage",
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
                "bonusEarthDamage",
                "bonusThunderDamage",
                "bonusWaterDamage",
                "bonusFireDamage",
                "bonusAirDamage",
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
    START: skill bonuses -- "skill stuff"
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
