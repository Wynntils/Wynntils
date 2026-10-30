/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderSpecialTierInfo;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearInstanceRequirements;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.gear.type.SetInstance;
import com.wynntils.models.items.properties.ClassableItemProperty;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.models.items.properties.PowderedItemProperty;
import com.wynntils.models.items.properties.RequirementItemProperty;
import com.wynntils.models.items.properties.RerollableItemProperty;
import com.wynntils.models.items.properties.SetItemProperty;
import com.wynntils.models.items.properties.ShinyItemProperty;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.chat.Component;

public class UnknownGearItem extends GameItem
        implements IdentifiableItemProperty<UnknownGearItem, UnknownGearItem>,
                GearTierItemProperty,
                GearTypeItemProperty,
                LeveledItemProperty,
                PowderedItemProperty,
                RerollableItemProperty,
                ShinyItemProperty,
                ClassableItemProperty,
                SetItemProperty,
                RequirementItemProperty,
                PagedItemProperty {
    private final String name;
    private final GearTier gearTier;
    private GearType gearType;
    private int health;
    private GearAttackSpeed attackSpeed;
    private List<Pair<DamageType, RangedValue>> damages;
    private List<Pair<Element, Integer>> defences;
    private int level;
    private GearRequirements requirements;
    private GearInstanceRequirements gearInstanceRequirements;
    private ShinyStat shinyStat;
    private int rerolls;
    private List<StatActualValue> identifications;
    private List<Component> majorIdLines = List.of();
    private GearRestrictions restrictions = GearRestrictions.NONE;
    private Optional<SetInstance> setInstance = Optional.empty();
    private int powderSlots;
    private Optional<PowderSpecialTierInfo> powderSpecial = Optional.empty();
    private List<Pair<Powder, Integer>> powders = List.of();
    private StyledText lore = StyledText.EMPTY;

    private int currentPage;
    private final Set<Integer> parsedPages = new HashSet<>();
    private final boolean isUnidentified;

    public UnknownGearItem(String name, GearTier gearTier, GearType gearType, boolean isUnidentified) {
        this.name = name;
        this.gearTier = gearTier;
        this.gearType = gearType;
        this.isUnidentified = isUnidentified;
        this.damages = List.of();
        this.defences = List.of();
        this.requirements = new GearRequirements(0, Optional.empty(), List.of(), Optional.empty());
        this.gearInstanceRequirements = null;
        this.identifications = List.of();
    }

    public String getName() {
        return name;
    }

    @Override
    public GearTier getGearTier() {
        return gearTier;
    }

    @Override
    public GearType getGearType() {
        return gearType;
    }

    public int getHealth() {
        return health;
    }

    public GearAttackSpeed getAttackSpeed() {
        return attackSpeed;
    }

    public List<Pair<DamageType, RangedValue>> getDamages() {
        return damages;
    }

    public List<Pair<Element, Integer>> getDefences() {
        return defences;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public ClassType getRequiredClass() {
        return requirements.classType().orElse(null);
    }

    public GearRequirements getRequirements() {
        return requirements;
    }

    @Override
    public GearInstanceRequirements getGearInstanceRequirements() {
        return gearInstanceRequirements;
    }

    @Override
    public boolean meetsActualRequirements() {
        return gearInstanceRequirements.meetsAllRequirements();
    }

    @Override
    public Optional<ShinyStat> getShinyStat() {
        return Optional.ofNullable(shinyStat);
    }

    @Override
    public int getRerollCount() {
        return rerolls;
    }

    @Override
    public List<StatActualValue> getIdentifications() {
        return identifications;
    }

    @Override
    public List<StatType> getVariableStats() {
        return identifications.stream().map(StatActualValue::statType).toList();
    }

    @Override
    public List<StatPossibleValues> getPossibleValues() {
        return List.of();
    }

    @Override
    public RangedValue getIdentificationLevelRange() {
        if (gearType == GearType.CHARM) {
            return RangedValue.of(getLevel(), getLevel() + 20);
        }

        return RangedValue.NONE;
    }

    @Override
    public boolean hasOverallValue() {
        return !isUnidentified && identifications.stream().anyMatch(stat -> stat.vanillaMeter() != CappedValue.EMPTY);
    }

    @Override
    public boolean isPerfect() {
        return getOverallPercentage() == 100;
    }

    @Override
    public boolean isDefective() {
        return getOverallPercentage() == 0;
    }

    @Override
    public float getOverallPercentage() {
        return StatCalculator.calculateOverallQualityFromVanillaMeters(identifications);
    }

    public List<Component> getMajorIdLines() {
        return majorIdLines;
    }

    public GearRestrictions getRestrictions() {
        return restrictions;
    }

    @Override
    public Optional<SetInstance> getSetInstance() {
        return setInstance;
    }

    @Override
    public Optional<SetInfo> getSetInfo() {
        return Optional.ofNullable(Models.Set.getSetInfoForItem(name));
    }

    @Override
    public int getPowderSlots() {
        return powderSlots;
    }

    @Override
    public Optional<PowderSpecialTierInfo> getPowderSpecial() {
        return powderSpecial;
    }

    @Override
    public List<Pair<Powder, Integer>> getPowders() {
        return powders;
    }

    @Override
    public StyledText getLore() {
        return lore;
    }

    @Override
    public UnknownGearItem getItemInfo() {
        return this;
    }

    @Override
    public Optional<UnknownGearItem> getItemInstance() {
        return isUnidentified || !hasParsedPage(0) ? Optional.empty() : Optional.of(this);
    }

    @Override
    public boolean isUnidentified() {
        return isUnidentified;
    }

    public void updateStats(
            GearRestrictions restrictions,
            int health,
            GearAttackSpeed attackSpeed,
            List<Pair<DamageType, RangedValue>> damages,
            List<Pair<Element, Integer>> defences,
            int level,
            GearRequirements requirements,
            GearInstanceRequirements gearInstanceRequirements,
            Optional<ShinyStat> shinyStat,
            int rerolls,
            List<StatActualValue> identifications,
            List<Component> majorIdLines) {
        this.restrictions = restrictions;
        this.health = health;
        this.attackSpeed = attackSpeed;
        this.damages = damages;
        this.defences = defences;
        this.level = level;
        this.requirements = requirements;
        this.gearInstanceRequirements = gearInstanceRequirements;
        this.shinyStat = shinyStat.orElse(null);
        this.rerolls = rerolls;
        this.identifications = identifications;
        this.majorIdLines = majorIdLines;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setAttackSpeed(GearAttackSpeed attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    public void setDamages(List<Pair<DamageType, RangedValue>> damages) {
        this.damages = damages;
    }

    public void setDefences(List<Pair<Element, Integer>> defences) {
        this.defences = defences;
    }

    public void setRestrictions(GearRestrictions restrictions) {
        this.restrictions = restrictions;
    }

    public void setSetInstance(Optional<SetInstance> setInstance) {
        this.setInstance = setInstance;
    }

    public void setPowderSlots(int powderSlots) {
        this.powderSlots = powderSlots;
    }

    public void setPowderSpecial(Optional<PowderSpecialTierInfo> powderSpecial) {
        this.powderSpecial = powderSpecial;
    }

    public void setPowders(List<Pair<Powder, Integer>> powders) {
        this.powders = List.copyOf(powders);
    }

    @Override
    public void setLore(StyledText lore) {
        this.lore = lore;
    }

    @Override
    public int currentPage() {
        return currentPage;
    }

    @Override
    public void setCurrentPage(int page) {
        currentPage = page;
    }

    @Override
    public boolean hasParsedPage(int page) {
        return parsedPages.contains(page);
    }

    @Override
    public void markParsedPage(int page) {
        parsedPages.add(page);
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
                + requirements + ", gearInstanceRequirements="
                + gearInstanceRequirements + ", identifications="
                + identifications + ", rerolls="
                + rerolls + ", shinyStat="
                + shinyStat + '}';
    }
}
