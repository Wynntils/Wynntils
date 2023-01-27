/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats;

import java.util.List;

public class StatOrderAthena {

    public static List<String> getAthenaOrder() {
        return List.of();
    }

    /*
      LEGACY / ATHENA:

    "identificationOrder": {
      "order": {
        "rawStrength": 1,
        "rawDexterity": 2,
        "rawIntelligence": 3,
        "rawDefence": 4,
        "rawAgility": 5,
        "attackSpeed": 6,
        "rawMainAttackDamage": 7,
        "mainAttackDamage": 8,
        "rawSpellDamage": 10,
        "spellDamage": 11,
        "rawThunderSpellDamage": 12,
        "rawFireSpellDamage": 13,
        "rawAirSpellDamage": 14,
        "rawEarthSpellDamage": 15,
        "rawWaterSpellDamage": 16,
        "rawHealth": 17,
        "rawHealthRegen": 18,
        "healthRegen": 19,
        "lifeSteal": 20,
        "manaRegen": 21,
        "manaSteal": 22,
        "earthDamage": 23,
        "thunderDamage": 24,
        "waterDamage": 25,
        "fireDamage": 26,
        "airDamage": 27,
        "earthDefence": 28,
        "thunderDefence": 29,
        "waterDefence": 30,
        "fireDefence": 31,
        "airDefence": 32,
        "exploding": 33,
        "poison": 34,
        "thorns": 35,
        "reflection": 36,
        "walkSpeed": 37,
        "sprint": 38,
        "sprintRegen": 39,
        "rawJumpHeight": 40,
        "soulPointRegen": 41,
        "lootBonus": 42,
        "lootQuality": 43,
        "stealing": 44,
        "xpBonus": 45,
        "gatherXpBonus": 46,
        "gatherSpeed": 47,
        "raw1stSpellCost": 48,
        "1stSpellCost": 49,
        "raw2ndSpellCost": 50,
        "2ndSpellCost": 51,
        "raw3rdSpellCost": 52,
        "3rdSpellCost": 53,
        "raw4thSpellCost": 54,
        "4thSpellCost": 55
      },

       */
    /*
         Wynntils legacy, copied from Athena:

            $result['order'] = collect([
                'rawStrength',
                'rawDexterity',
                'rawIntelligence',
                'rawDefence',
                'rawAgility',

                //second group {attack stuff}
                'attackSpeed',
                'rawMainAttackDamage',
                'mainAttackDamage',
                'rawSpellDamage',
                'rawSpellDamage',
                'spellDamage',
                'elementalSpellDamage',
                'rawElementalSpellDamage',
                'rawNeutralSpellDamage',
                'rawThunderSpellDamage',
                'rawFireSpellDamage',
                'rawAirSpellDamage',
                'rawEarthSpellDamage',
                'rawWaterSpellDamage',

                //third group {health/mana stuff}
                'rawHealth',
                'rawHealthRegen',
                'healthRegen',
                'lifeSteal',
                'manaRegen',
                'manaSteal',

                //fourth group {damage stuff}
                'earthDamage',
                'thunderDamage',
                'waterDamage',
                'fireDamage',
                'airDamage',

                //fifth group {defence stuff}
                'earthDefence',
                'thunderDefence',
                'waterDefence',
                'fireDefence',
                'airDefence',

                //sixth group {passive damage}
                'exploding',
                'poison',
                'thorns',
                'reflection',

                //seventh group {movement stuff}
                'walkSpeed',
                'sprint',
                'sprintRegen',
                'rawJumpHeight',

                //eigth group {XP/Gathering stuff}
                'soulPointRegen',
                'lootBonus',
                'lootQuality',
                'stealing',
                'xpBonus',
                'gatherXpBonus',
                'gatherSpeed',

                //ninth group {spell stuff}
                'raw1stSpellCost',
                '1stSpellCost',
                'raw2ndSpellCost',
                '2ndSpellCost',
                'raw3rdSpellCost',
                '3rdSpellCost',
                'raw4thSpellCost',
                '4thSpellCost',
            ])->mapWithKeys(
                fn($value, $key) => [$value => $key + 1]
            )->toArray();

            $groups = &$result['groups'];

            $groups[] = '1-5';
            $groups[] = '6-11';
            $groups[] = '12-17';
            $groups[] = '18-22';
            $groups[] = '23-27';
            $groups[] = '28-31';
            $groups[] = '32-35';
            $groups[] = '36-42';
            $groups[] = '43-50';

    "rawStrength" -> {Integer@17006} 1
    "rawDexterity" -> {Integer@17008} 2
    "rawIntelligence" -> {Integer@17010} 3
    "rawDefence" -> {Integer@17012} 4
    "rawAgility" -> {Integer@17014} 5
    "attackSpeed" -> {Integer@17016} 6
    "rawMainAttackDamage" -> {Integer@17018} 7
    "mainAttackDamage" -> {Integer@17020} 8
    "rawSpellDamage" -> {Integer@17022} 10
    "spellDamage" -> {Integer@17024} 11
    "elementalSpellDamage" -> {Integer@17026} 12
    "rawElementalSpellDamage" -> {Integer@17028} 13
    "rawNeutralSpellDamage" -> {Integer@17030} 14
    "rawThunderSpellDamage" -> {Integer@17032} 15
    "rawFireSpellDamage" -> {Integer@17034} 16
    "rawAirSpellDamage" -> {Integer@17036} 17
    "rawEarthSpellDamage" -> {Integer@17038} 18
    "rawWaterSpellDamage" -> {Integer@17040} 19
    "rawHealth" -> {Integer@17042} 20
    "rawHealthRegen" -> {Integer@17044} 21
    "healthRegen" -> {Integer@17046} 22
    "lifeSteal" -> {Integer@17048} 23
    "manaRegen" -> {Integer@17050} 24
    "manaSteal" -> {Integer@17052} 25
    "earthDamage" -> {Integer@17054} 26
    "thunderDamage" -> {Integer@17056} 27
    "waterDamage" -> {Integer@17058} 28
    "fireDamage" -> {Integer@17060} 29
    "airDamage" -> {Integer@17062} 30
    "earthDefence" -> {Integer@17064} 31
    "thunderDefence" -> {Integer@17066} 32
    "waterDefence" -> {Integer@17068} 33
    "fireDefence" -> {Integer@17070} 34
    "airDefence" -> {Integer@17072} 35
    "exploding" -> {Integer@17074} 36
    "poison" -> {Integer@17076} 37
    "thorns" -> {Integer@17078} 38
    "reflection" -> {Integer@17080} 39
    "walkSpeed" -> {Integer@17082} 40
    "sprint" -> {Integer@17084} 41
    "sprintRegen" -> {Integer@17086} 42
    "rawJumpHeight" -> {Integer@17088} 43
    "soulPointRegen" -> {Integer@17090} 44
    "lootBonus" -> {Integer@17092} 45
    "lootQuality" -> {Integer@17094} 46
    "stealing" -> {Integer@17096} 47
    "xpBonus" -> {Integer@17098} 48
    "gatherXpBonus" -> {Integer@17100} 49
    "gatherSpeed" -> {Integer@17102} 50
    "raw1stSpellCost" -> {Integer@17104} 51
    "1stSpellCost" -> {Integer@17106} 52
    "raw2ndSpellCost" -> {Integer@17108} 53
    "2ndSpellCost" -> {Integer@17110} 54
    "raw3rdSpellCost" -> {Integer@17112} 55
    "3rdSpellCost" -> {Integer@17114} 56
    "raw4thSpellCost" -> {Integer@17116} 57
    "4thSpellCost" -> {Integer@17118} 58

         */

}
