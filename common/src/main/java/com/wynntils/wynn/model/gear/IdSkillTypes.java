/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.gear;

public enum IdSkillTypes implements IdType {
    SKILL_AGILITY(IsVariable.NO, "Agility", null, "rawAgility", "AGILITYPOINTS", "agilityPoints"),
    SKILL_DEFENCE(IsVariable.NO, "Defence", null, "rawDefence", "DEFENSEPOINTS", "defensePoints"),
    SKILL_DEXTERITY(IsVariable.NO, "Dexterity", null, "rawDexterity", "DEXTERITYPOINTS", "dexterityPoints"),
    SKILL_INTELLIGENCE(
            IsVariable.NO, "Intelligence", null, "rawIntelligence", "INTELLIGENCEPOINTS", "intelligencePoints"),
    SKILL_STRENGTH(IsVariable.NO, "Strength", null, "rawStrength", "STRENGTHPOINTS", "strengthPoints");

    private final IsVariable isVariable;
    private final String displayName;
    private final String unit;
    private final String athenaName;
    private final String loreName;
    private final String apiName;

    IdSkillTypes(
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
