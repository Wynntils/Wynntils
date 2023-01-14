/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

public enum GearDefenceStat implements GearStat {
    // FIXME: check discrepancy between lore and api
    DEFENCE_AIR(IsVariable.YES, "Air Defence", "%", "airDefence", "AIRDEFENSE", "bonusAirDefense"),
    DEFENCE_EARTH(IsVariable.YES, "Earth Defence", "%", "earthDefence", "EARTHDEFENSE", "bonusEarthDefense"),
    DEFENCE_FIRE(IsVariable.YES, "Fire Defence", "%", "fireDefence", "FIREDEFENSE", "bonusFireDefense"),
    DEFENCE_THUNDER(IsVariable.YES, "Thunder Defence", "%", "thunderDefence", "THUNDERDEFENSE", "bonusThunderDefense"),
    DEFENCE_WATER(IsVariable.YES, "Water Defence", "%", "waterDefence", "WATERDEFENSE", "bonusWaterDefense");

    private final IsVariable isVariable;
    private final String displayName;
    private final String unit;
    private final String athenaName;
    private final String loreName;
    private final String apiName;

    GearDefenceStat(
            IsVariable isVariable,
            String displayName,
            String unit,
            String athenaName,
            String loreName,
            String apiName) {
        this.isVariable = isVariable;
        this.displayName = displayName;
        this.unit = unit;
        this.athenaName = athenaName;
        this.loreName = loreName;
        this.apiName = apiName;
    }

    @Override
    public String getKey() {
        return this.name();
    }

    public IsVariable getIsVariable() {
        return isVariable;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    @Override
    public String getAthenaName() {
        return athenaName;
    }

    @Override
    public String getLoreName() {
        return loreName;
    }

    @Override
    public String getApiName() {
        return apiName;
    }
}
