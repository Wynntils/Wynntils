/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearInstanceRequirements;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.models.items.properties.RequirementItemProperty;
import com.wynntils.models.items.properties.RerollableItemProperty;
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.CharmInstance;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CharmItem extends GameItem
        implements GearTierItemProperty,
                GearTypeItemProperty,
                RerollableItemProperty,
                LeveledItemProperty,
                RequirementItemProperty,
                IdentifiableItemProperty<CharmInfo, CharmInstance>,
                PagedItemProperty {
    private int currentPage;
    private final Set<Integer> parsedPages = new HashSet<>();

    private final CharmInfo charmInfo;
    private final CharmInstance charmInstance;
    private GearInstanceRequirements gearInstanceRequirements;

    public CharmItem(CharmInfo charmInfo, boolean isUnidentified) {
        this.charmInfo = charmInfo;
        this.charmInstance = isUnidentified ? null : new CharmInstance();
    }

    public CharmItem(CharmInfo charmInfo, CharmInstance charmInstance, int currentPage) {
        this.currentPage = currentPage;
        parsedPages.add(currentPage);
        this.charmInfo = charmInfo;
        this.charmInstance = charmInstance;
    }

    public CharmItem(CharmInfo charmInfo, CharmInstance charmInstance) {
        this(charmInfo, charmInstance, 0);
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
    public StyledText getLore() {
        return charmInfo.metaInfo().lore().orElse(StyledText.EMPTY);
    }

    @Override
    public void setLore(StyledText lore) {
        // Unused, we get the lore from the CharmInfo
    }

    @Override
    public GearType getGearType() {
        return GearType.CHARM;
    }

    @Override
    public CharmInfo getItemInfo() {
        return charmInfo;
    }

    @Override
    public Optional<CharmInstance> getItemInstance() {
        return Optional.ofNullable(charmInstance);
    }

    @Override
    public boolean isUnidentified() {
        return charmInstance == null;
    }

    @Override
    public int getRerollCount() {
        return charmInstance != null ? charmInstance.rerolls() : 0;
    }

    @Override
    public GearTier getGearTier() {
        return charmInfo.tier();
    }

    @Override
    public String getName() {
        return charmInfo.name();
    }

    @Override
    public ClassType getRequiredClass() {
        // Charms are not class-specific
        return ClassType.NONE;
    }

    @Override
    public List<StatType> getVariableStats() {
        return charmInfo.variableStats().stream().map(Pair::a).toList();
    }

    @Override
    public int getLevel() {
        return charmInfo.requirements().level();
    }

    @Override
    public GearInstanceRequirements getGearInstanceRequirements() {
        return gearInstanceRequirements;
    }

    @Override
    public boolean meetsActualRequirements() {
        return gearInstanceRequirements != null && gearInstanceRequirements.meetsAllRequirements();
    }

    @Override
    public List<StatActualValue> getIdentifications() {
        if (charmInstance == null) return List.of();

        return charmInstance.identifications();
    }

    @Override
    public List<StatPossibleValues> getPossibleValues() {
        return charmInfo.variableStats().stream().map(Pair::b).toList();
    }

    @Override
    public RangedValue getIdentificationLevelRange() {
        return RangedValue.of(getLevel(), getLevel() + 20);
    }

    @Override
    public boolean hasOverallValue() {
        return charmInstance != null && charmInstance.hasOverallValue();
    }

    @Override
    public boolean isPerfect() {
        return charmInstance != null && charmInstance.isPerfect() || hasOverallValue() && getOverallPercentage() == 100;
    }

    @Override
    public boolean isDefective() {
        return charmInstance != null && charmInstance.isDefective() || hasOverallValue() && getOverallPercentage() == 0;
    }

    @Override
    public float getOverallPercentage() {
        return charmInstance != null ? charmInstance.getOverallPercentage() : 0.0f;
    }

    public void updateStats(
            GearInstanceRequirements gearInstanceRequirements, int rerolls, List<StatActualValue> identifications) {
        this.gearInstanceRequirements = gearInstanceRequirements;
        if (charmInstance != null) {
            charmInstance.updateStats(rerolls, charmInfo, identifications);
        }
    }

    @Override
    public String toString() {
        return "CharmItem{" + "charmInfo=" + charmInfo + ", charmInstance=" + charmInstance + '}';
    }
}
