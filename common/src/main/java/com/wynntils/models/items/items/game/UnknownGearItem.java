/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.gear.type.SetInstance;
import com.wynntils.models.items.properties.ClassableItemProperty;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.PowderedItemProperty;
import com.wynntils.models.items.properties.RequirementItemProperty;
import com.wynntils.models.items.properties.RerollableItemProperty;
import com.wynntils.models.items.properties.SetItemProperty;
import com.wynntils.models.items.properties.ShinyItemProperty;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.Optional;

public class UnknownGearItem extends GameItem
        implements GearTierItemProperty,
                GearTypeItemProperty,
                LeveledItemProperty,
                PowderedItemProperty,
                RerollableItemProperty,
                ShinyItemProperty,
                ClassableItemProperty,
                SetItemProperty,
                RequirementItemProperty {
    private final String name;
    private final GearType gearType;
    private final GearTier gearTier;
    private final boolean isUnidentified;
    private final int level;
    private final GearAttackSpeed attackSpeed;
    private final int health;
    private final List<Pair<DamageType, RangedValue>> damages;
    private final List<Pair<Element, Integer>> defences;
    private final GearRequirements requirements;
    private final boolean allRequirementsMet;
    private final List<StatActualValue> identifications;
    private final List<Powder> powders;
    private final int powderSlots;
    private final int rerolls;
    private final SetInstance setInstance;
    private final ShinyStat shinyStat;

    public UnknownGearItem(
            String name,
            GearType gearType,
            GearTier gearTier,
            boolean isUnidentified,
            int level,
            GearAttackSpeed attackSpeed,
            int health,
            List<Pair<DamageType, RangedValue>> damages,
            List<Pair<Element, Integer>> defences,
            GearRequirements requirements,
            boolean allRequirementsMet,
            List<StatActualValue> identifications,
            List<Powder> powders,
            int powderSlots,
            int rerolls,
            SetInstance setInstance,
            ShinyStat shinyStat) {
        this.name = name;
        this.gearType = gearType;
        this.gearTier = gearTier;
        this.isUnidentified = isUnidentified;
        this.level = level;
        this.attackSpeed = attackSpeed;
        this.health = health;
        this.damages = damages;
        this.defences = defences;
        this.requirements = requirements;
        this.allRequirementsMet = allRequirementsMet;
        this.identifications = identifications;
        this.powders = powders;
        this.powderSlots = powderSlots;
        this.rerolls = rerolls;
        this.setInstance = setInstance;
        this.shinyStat = shinyStat;
    }

    public String getName() {
        return name;
    }

    @Override
    public GearType getGearType() {
        return gearType;
    }

    @Override
    public GearTier getGearTier() {
        return gearTier;
    }

    @Override
    public int getLevel() {
        return level;
    }

    public GearAttackSpeed getAttackSpeed() {
        return attackSpeed;
    }

    public int getHealth() {
        return health;
    }

    public List<Pair<DamageType, RangedValue>> getDamages() {
        return damages;
    }

    public List<Pair<Element, Integer>> getDefences() {
        return defences;
    }

    public GearRequirements getRequirements() {
        return requirements;
    }

    public boolean isUnidentified() {
        return isUnidentified;
    }

    public List<StatActualValue> getIdentifications() {
        return identifications;
    }

    @Override
    public ClassType getRequiredClass() {
        return requirements.classType().orElse(null);
    }

    @Override
    public List<Powder> getPowders() {
        return powders;
    }

    @Override
    public int getPowderSlots() {
        return powderSlots;
    }

    @Override
    public boolean meetsActualRequirements() {
        return allRequirementsMet;
    }

    @Override
    public int getRerollCount() {
        return rerolls;
    }

    @Override
    public Optional<SetInfo> getSetInfo() {
        return Optional.ofNullable(setInstance).map(SetInstance::setInfo);
    }

    @Override
    public Optional<SetInstance> getSetInstance() {
        return Optional.ofNullable(setInstance);
    }

    @Override
    public Optional<ShinyStat> getShinyStat() {
        return Optional.ofNullable(shinyStat);
    }

    @Override
    public String toString() {
        return "UnknownGearItem{" + "name='"
                + name + '\'' + ", gearType="
                + gearType + ", gearTier="
                + gearTier + ", level="
                + level + ", attackSpeed="
                + attackSpeed + ", health="
                + health + ", damages="
                + damages + ", defences="
                + defences + ", requirements="
                + requirements + ", allRequirementsMet="
                + allRequirementsMet + ", identifications="
                + identifications + ", powders="
                + powders + ", powderSlots="
                + powderSlots + ", rerolls="
                + rerolls + ", setInstance="
                + setInstance + ", shinyStat="
                + shinyStat + '}';
    }
}
