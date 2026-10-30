/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderSpecialTierInfo;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearInstanceRequirements;
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
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GearItem extends GameItem
        implements GearTierItemProperty,
                GearTypeItemProperty,
                LeveledItemProperty,
                PowderedItemProperty,
                RerollableItemProperty,
                ShinyItemProperty,
                IdentifiableItemProperty<GearInfo, GearInstance>,
                ClassableItemProperty,
                SetItemProperty,
                RequirementItemProperty,
                PagedItemProperty {
    private GearInfo gearInfo;
    private final GearInstance gearInstance;
    private Optional<ShinyStat> shinyStat = Optional.empty();

    private int currentPage;
    private final Set<Integer> parsedPages = new HashSet<>();

    public GearItem(GearInfo gearInfo, boolean isUnidentified) {
        this.gearInfo = gearInfo;
        this.gearInstance = isUnidentified ? null : new GearInstance();
    }

    public GearItem(GearInfo gearInfo, GearInstance gearInstance, Optional<ShinyStat> shinyStat, int currentPage) {
        this.currentPage = currentPage;
        parsedPages.add(currentPage);
        this.gearInfo = gearInfo;
        this.gearInstance = gearInstance;
        this.shinyStat = shinyStat != null ? shinyStat : Optional.empty();
    }

    public GearItem(GearInfo gearInfo, GearInstance gearInstance, int currentPage) {
        this(gearInfo, gearInstance, Optional.empty(), currentPage);
    }

    public GearItem(GearInfo gearInfo, GearInstance gearInstance) {
        this(gearInfo, gearInstance, 0);
    }

    @Override
    public String getName() {
        return gearInfo.name();
    }

    @Override
    public GearTier getGearTier() {
        return gearInfo.tier();
    }

    @Override
    public GearType getGearType() {
        return gearInfo.type();
    }

    @Override
    public int getLevel() {
        return gearInfo.requirements().level();
    }

    @Override
    public ClassType getRequiredClass() {
        return gearInfo.requirements().classType().orElse(null);
    }

    @Override
    public GearInstanceRequirements getGearInstanceRequirements() {
        return gearInstance == null ? null : gearInstance.gearInstanceRequirements();
    }

    @Override
    public boolean meetsActualRequirements() {
        return gearInstance != null
                && gearInstance.gearInstanceRequirements() != null
                && gearInstance.gearInstanceRequirements().meetsAllRequirements();
    }

    @Override
    public Optional<ShinyStat> getShinyStat() {
        return shinyStat;
    }

    @Override
    public int getRerollCount() {
        return gearInstance != null ? gearInstance.rerolls() : 0;
    }

    @Override
    public List<StatActualValue> getIdentifications() {
        if (gearInstance == null) return List.of();

        return gearInstance.identifications();
    }

    @Override
    public List<StatType> getVariableStats() {
        return gearInfo.variableStats().stream().map(Pair::a).toList();
    }

    @Override
    public List<StatPossibleValues> getPossibleValues() {
        return gearInfo.variableStats().stream().map(Pair::b).toList();
    }

    @Override
    public RangedValue getIdentificationLevelRange() {
        // Gears do not have level-specific stats
        return RangedValue.NONE;
    }

    @Override
    public boolean hasOverallValue() {
        return gearInstance != null && gearInstance.hasOverallValue();
    }

    @Override
    public boolean isPerfect() {
        return (gearInstance != null && gearInstance.isPerfect()) || getOverallPercentage() == 100;
    }

    @Override
    public boolean isDefective() {
        return (gearInstance != null && gearInstance.isDefective()) || getOverallPercentage() == 0;
    }

    @Override
    public float getOverallPercentage() {
        return gearInstance != null ? gearInstance.getOverallPercentage() : 0.0f;
    }

    @Override
    public Optional<SetInstance> getSetInstance() {
        return gearInstance == null ? Optional.empty() : gearInstance.getSetInstance();
    }

    @Override
    public Optional<SetInfo> getSetInfo() {
        return gearInfo.setInfo();
    }

    @Override
    public int getPowderSlots() {
        return gearInfo.powderSlots();
    }

    @Override
    public Optional<PowderSpecialTierInfo> getPowderSpecial() {
        return gearInstance == null ? Optional.empty() : gearInstance.getPowderSpecial();
    }

    @Override
    public List<Pair<Powder, Integer>> getPowders() {
        return gearInstance == null ? List.of() : gearInstance.powders();
    }

    @Override
    public StyledText getLore() {
        return gearInfo.metaInfo().lore().orElse(StyledText.EMPTY);
    }

    @Override
    public GearInfo getItemInfo() {
        return gearInfo;
    }

    @Override
    public Optional<GearInstance> getItemInstance() {
        return Optional.ofNullable(gearInstance);
    }

    @Override
    public boolean isUnidentified() {
        return gearInstance == null;
    }

    public void updateStats(
            GearInstanceRequirements gearInstanceRequirements,
            Optional<ShinyStat> shinyStat,
            int rerolls,
            List<StatActualValue> identifications) {
        this.shinyStat = shinyStat;
        if (gearInstance != null) {
            gearInstance.updateStats(gearInfo, identifications, rerolls, gearInstanceRequirements);
        }
    }

    public void setSetInstance(Optional<SetInstance> setInstance) {
        if (gearInstance != null) gearInstance.setSetInstance(setInstance);
    }

    public void setPowderSpecial(Optional<PowderSpecialTierInfo> powderSpecial) {
        if (gearInstance != null) gearInstance.setPowderSpecial(powderSpecial);
    }

    public void setPowders(List<Pair<Powder, Integer>> powders) {
        if (gearInstance != null) gearInstance.setPowders(powders);
    }

    @Override
    public void setLore(StyledText lore) {
        // Unused, we get the lore from the GearInfo
    }

    public void setItemInfo(GearInfo gearInfo) {
        this.gearInfo = gearInfo;
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
        return "GearItem{"
                + "gearInfo="
                + gearInfo
                + ", gearInstance="
                + gearInstance
                + ", shinyStat="
                + shinyStat
                + '}';
    }
}
