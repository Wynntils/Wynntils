/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderSpecialTierInfo;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearInstanceRequirements;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.properties.ClassableItemProperty;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.models.items.properties.PowderedItemProperty;
import com.wynntils.models.items.properties.RequirementItemProperty;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CraftedGearItem extends GameItem
        implements GearTierItemProperty,
                GearTypeItemProperty,
                DurableItemProperty,
                LeveledItemProperty,
                PowderedItemProperty,
                CraftedItemProperty,
                ClassableItemProperty,
                RequirementItemProperty,
                PagedItemProperty {
    private final String name;
    private GearType gearType;
    private int dps;
    private int health;
    private CappedValue durability;
    private GearAttackSpeed attackSpeed;
    private List<Pair<DamageType, RangedValue>> damages;
    private List<Pair<Element, Integer>> defences;
    private GearRequirements requirements;
    private GearInstanceRequirements gearInstanceRequirements;
    private List<StatActualValue> identifications;
    private int powderSlots;
    private Optional<PowderSpecialTierInfo> powderSpecial = Optional.empty();
    private List<Pair<Powder, Integer>> powders;
    private StyledText lore = StyledText.EMPTY;
    private String creator = "";

    private int currentPage;
    private final Set<Integer> parsedPages = new HashSet<>();

    public CraftedGearItem(String name, GearType gearType) {
        this.name = name;
        this.gearType = gearType;
        this.durability = CappedValue.EMPTY;
        this.damages = List.of();
        this.defences = List.of();
        this.requirements = new GearRequirements(0, Optional.empty(), List.of(), Optional.empty());
        this.gearInstanceRequirements = null;
        this.identifications = List.of();
        this.powders = List.of();
    }

    public CraftedGearItem(
            String name,
            GearType gearType,
            int dps,
            int health,
            CappedValue durability,
            GearAttackSpeed attackSpeed,
            List<Pair<DamageType, RangedValue>> damages,
            List<Pair<Element, Integer>> defences,
            GearRequirements requirements,
            GearInstanceRequirements gearInstanceRequirements,
            List<StatActualValue> identifications,
            int currentPage) {
        this(
                name,
                gearType,
                dps,
                health,
                durability,
                attackSpeed,
                damages,
                defences,
                requirements,
                gearInstanceRequirements,
                identifications,
                0,
                List.of(),
                currentPage);
    }

    public CraftedGearItem(
            String name,
            GearType gearType,
            int dps,
            int health,
            CappedValue durability,
            GearAttackSpeed attackSpeed,
            List<Pair<DamageType, RangedValue>> damages,
            List<Pair<Element, Integer>> defences,
            GearRequirements requirements,
            GearInstanceRequirements gearInstanceRequirements,
            List<StatActualValue> identifications,
            int powderSlots,
            List<Pair<Powder, Integer>> powders,
            int currentPage) {
        this.currentPage = currentPage;
        parsedPages.add(currentPage);
        this.name = name;
        this.gearType = gearType;
        this.dps = dps;
        this.health = health;
        this.durability = durability;
        this.attackSpeed = attackSpeed;
        this.damages = damages;
        this.defences = defences;
        this.requirements = requirements;
        this.gearInstanceRequirements = gearInstanceRequirements;
        this.identifications = identifications;
        this.powderSlots = powderSlots;
        this.powders = List.copyOf(powders);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public GearTier getGearTier() {
        return GearTier.CRAFTED;
    }

    @Override
    public GearType getGearType() {
        return gearType;
    }

    public int getDps() {
        return dps;
    }

    public int getHealth() {
        return health;
    }

    public Optional<GearAttackSpeed> getAttackSpeed() {
        return Optional.ofNullable(attackSpeed);
    }

    public List<Pair<DamageType, RangedValue>> getDamages() {
        return damages;
    }

    public List<Pair<Element, Integer>> getDefences() {
        return defences;
    }

    @Override
    public int getLevel() {
        return requirements.level();
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
        return gearInstanceRequirements != null && gearInstanceRequirements.meetsAllRequirements();
    }

    @Override
    public List<StatActualValue> getIdentifications() {
        return identifications;
    }

    @Override
    public List<StatType> getStatTypes() {
        return identifications.stream().map(StatActualValue::statType).toList();
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
    public String getCreator() {
        return creator;
    }

    @Override
    public CappedValue getDurability() {
        return durability;
    }

    public void updateStats(
            int dps,
            int health,
            CappedValue durability,
            GearAttackSpeed attackSpeed,
            List<Pair<DamageType, RangedValue>> damages,
            List<Pair<Element, Integer>> defences,
            GearRequirements requirements,
            GearInstanceRequirements gearInstanceRequirements,
            List<StatActualValue> identifications) {
        this.dps = dps;
        this.health = health;
        this.durability = durability;
        this.attackSpeed = attackSpeed;
        this.damages = damages;
        this.defences = defences;
        this.requirements = requirements;
        this.gearInstanceRequirements = gearInstanceRequirements;
        this.identifications = identifications;
        if (gearType == GearType.WEAPON) {
            gearType = requirements.classType().map(GearType::fromClassType).orElse(gearType);
        }
    }

    public void setDps(int dps) {
        this.dps = dps;
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

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setDurability(CappedValue durability) {
        this.durability = durability;
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
        return "CraftedGearItem{" + "name='"
                + name + '\'' + ", gearType="
                + gearType + ", dps="
                + dps + ", health="
                + health + ", durability="
                + durability + ", attackSpeed="
                + attackSpeed + ", damages="
                + damages + ", defences="
                + defences + ", requirements="
                + requirements + ", gearInstanceRequirements="
                + gearInstanceRequirements + ", identifications="
                + identifications + ", powderSlots="
                + powderSlots + ", powderSpecial="
                + powderSpecial + ", powders="
                + powders + ", lore="
                + lore + ", creator='"
                + creator + '\'' + ", currentPage="
                + currentPage + ", parsedPages="
                + parsedPages + '}';
    }
}
