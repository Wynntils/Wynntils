/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo;

import java.util.List;

public class StatOrder {

    /*
    FIXED:
                // damage stuff
                "damage",
                "fireDamage",
                "waterDamage",
                "airDamage",
                "thunderDamage",
                "earthDamage",
                "attackSpeed",

                // defence stuff
                "health",
                "fireDefense",
                "waterDefense",
                "airDefense",
                "thunderDefense",
                "earthDefense",

                // requirements
                "level",
                "quest",
                "classRequirement",
                "strength",
                "dexterity",
                "intelligence",
                "agility",
                "defense",


                // these are fixed
                "strengthPoints",
                "dexterityPoints",
                "intelligencePoints",
                "agilityPoints",
                "defensePoints",

     */
    public static List<String> getWynncraftOrder() {
        return List.of(


                // dynamic stats
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

                // DAMAGE STARTS HERE

                // ANY ATTTACK

                // no elem.
                "damageBonus", // never used

                // confirmed to be before spellDamageBonus
                "fireDamageBonus",
                "waterDamageBonus",
                "airDamageBonus",
                "thunderDamageBonus",
                "earthDamageBonus",
                "elementalDamageBonus", // only aleph null, confirmed before spell damage
                "neutralDamageBonus", // never used

                "damageBonusRaw", // never used

                "fireDamageBonusRaw", // not used
                "waterDamageBonusRaw", // checked expeditions end, only one, cant really say
                "earthDamageBonusRaw", // not used
                "thunderDamageBonusRaw", // only expeditions end
                "airDamageBonusRaw", // not used
                "elementalDamageBonusRaw", // used once, cannot test
                "neutralDamageBonusRaw", // never used

                // SPELL ATTACK

                "spellDamageBonus",

                "spellFireDamageBonus", // not used
                "spellWaterDamageBonus", //only in "ancient waters", we know only that it is a damage :(
                "spellEarthDamageBonus", // cant confirm, only in "decaying Headdress", can possible get hold of
                "spellThunderDamageBonus", // not used
                "spellAirDamageBonus", // confirmed before spellWaterDamageBonusRaw in Soul Ink
                "spellElementalDamageBonus", // only in violet-shift
                "spellNeutralDamageBonus", // never seend

                "spellDamageBonusRaw", // confirmed before mainAttackDamageBonus

                "spellFireDamageBonusRaw", // cant confirm, only "the nothing"
                "spellWaterDamageBonusRaw", // soul ink, cant say much
                "spellEarthDamageBonusRaw", // only decaying hairdress, cant test
                "spellThunderDamageBonusRaw", // only the nothing, cant test
                "spellAirDamageBonusRaw", // spearmint, ok order
                "spellElementalDamageBonusRaw", // cant confirm,. only in forest aconite
                "spellNeutralDamageBonusRaw",  // only in violet-shift...

                // MAIN ATTACK

                "mainAttackDamageBonus",

                // new possibly wrong
                "mainAttackFireDamageBonus", // not used
                "mainAttackWaterDamageBonus", // not used
                "mainAttackAirDamageBonus", // used once in "wind spine", cannot confirm
                "mainAttackThunderDamageBonus", // used once in "Despondence", cannot test
                "mainAttackEarthDamageBonus", // never used
                "mainAttackElementalDamageBonus", // never used
                "mainAttackNeutralDamageBonus", // never used

                "mainAttackDamageBonusRaw",

                "mainAttackFireDamageBonusRaw", // used once
                "mainAttackWaterDamageBonusRaw", // not used
                "mainAttackEarthDamageBonusRaw", // not used
                "mainAttackThunderDamageBonusRaw", // only in despondence
                "mainAttackAirDamageBonusRaw", // spearmint, ok order
                "mainAttackElementalDamageBonusRaw", // used once, cannot test
                "mainAttackNeutralDamageBonusRaw", // never used

                /// end untested

                // DAMAGE ENDS HERE


                // defence, confirmed to come after damage!
                "bonusFireDefense",
                "bonusWaterDefense",
                "bonusAirDefense",
                "bonusThunderDefense",
                "bonusEarthDefense",

                "sprint",  // order confirmed: sprint, spell cost, jumpheight.
                "sprintRegen",

                "spellCostPct1",
                "spellCostRaw1",
                "spellCostPct2",
                "spellCostRaw2",
                "spellCostPct3",
                "spellCostRaw3",
                "spellCostPct4",
                "spellCostRaw4",

                "jumpHeight",

                // not used except crafted, which does not have an order...
                "gatherXpBonus",
                "gatherSpeed",

                "lootQuality"

                );
    }

}
