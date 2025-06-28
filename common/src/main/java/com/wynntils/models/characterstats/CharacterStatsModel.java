/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.event.ActionBarRenderEvent;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.models.characterstats.actionbar.matchers.HealthBarSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.matchers.HealthTextSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.matchers.HotbarSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.matchers.LevelSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.matchers.ManaBarSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.matchers.ManaTextSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.matchers.MeterBarSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.matchers.MeterEdgeAnimationSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.matchers.MeterTransitionSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.matchers.PowderSpecialSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.matchers.ProfessionExperienceSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.segments.CombatExperienceSegment;
import com.wynntils.models.characterstats.actionbar.segments.HealthBarSegment;
import com.wynntils.models.characterstats.actionbar.segments.HealthTextSegment;
import com.wynntils.models.characterstats.actionbar.segments.LevelSegment;
import com.wynntils.models.characterstats.actionbar.segments.ManaBarSegment;
import com.wynntils.models.characterstats.actionbar.segments.ManaTextSegment;
import com.wynntils.models.characterstats.actionbar.segments.MeterBarSegment;
import com.wynntils.models.characterstats.actionbar.segments.PowderSpecialSegment;
import com.wynntils.models.characterstats.actionbar.segments.ProfessionExperienceSegment;
import com.wynntils.models.characterstats.type.MeterBarInfo;
import com.wynntils.models.characterstats.type.PowderSpecialInfo;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public final class CharacterStatsModel extends Model {
    private final Set<Class<? extends ActionBarSegment>> hiddenSegments = new HashSet<>();

    private int level = 0;
    private CappedValue health = CappedValue.EMPTY;
    private CappedValue mana = CappedValue.EMPTY;
    private CappedValue sprint = CappedValue.EMPTY;
    private PowderSpecialInfo powderSpecialInfo = PowderSpecialInfo.EMPTY;

    private boolean isProfessionExperience = false;

    public CharacterStatsModel() {
        super(List.of());

        // Register relevant segment matchers
        Handlers.ActionBar.registerSegment(new HotbarSegmentMatcher());
        Handlers.ActionBar.registerSegment(new MeterBarSegmentMatcher());
        Handlers.ActionBar.registerSegment(new MeterEdgeAnimationSegmentMatcher());
        Handlers.ActionBar.registerSegment(new MeterTransitionSegmentMatcher());
        Handlers.ActionBar.registerSegment(new LevelSegmentMatcher());
        Handlers.ActionBar.registerSegment(new ManaBarSegmentMatcher());
        Handlers.ActionBar.registerSegment(new HealthBarSegmentMatcher());
        Handlers.ActionBar.registerSegment(new ManaTextSegmentMatcher());
        Handlers.ActionBar.registerSegment(new HealthTextSegmentMatcher());
        Handlers.ActionBar.registerSegment(new PowderSpecialSegmentMatcher());
        Handlers.ActionBar.registerSegment(new ProfessionExperienceSegmentMatcher());
    }

    @SubscribeEvent
    public void onActionBarRender(ActionBarRenderEvent event) {
        hiddenSegments.forEach(segment -> event.setSegmentEnabled(segment, false));
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresent(HealthTextSegment.class, this::updateHealth);
        event.runIfPresent(ManaTextSegment.class, this::updateMana);
        event.runIfPresent(MeterBarSegment.class, this::updateSprint);
        event.runIfPresent(PowderSpecialSegment.class, this::updatePowderSpecial);
        event.runIfPresent(CombatExperienceSegment.class, this::updateCombatExperience);
        event.runIfPresent(ProfessionExperienceSegment.class, this::updateProfessionExperience);

        // This segment must be updated last, as it updates the level based on the
        // the current experience segment, which are updated above.
        event.runIfPresent(LevelSegment.class, this::updateLevel);
    }

    @SubscribeEvent
    public void onHeldItemChanged(ChangeCarriedItemEvent event) {
        // powders are always reset when held item is changed on Wynn, this ensures consistent behavior
        powderSpecialInfo = PowderSpecialInfo.EMPTY;
    }

    public double getBlocksAboveGround() {
        // iteratively find the first non-air block below the player
        double endY = (int) Math.ceil(McUtils.player().position().y) - 1;
        while (McUtils.mc()
                .level
                .getBlockState(new BlockPos(
                        McUtils.player().blockPosition().getX(),
                        (int) endY,
                        McUtils.player().blockPosition().getZ()))
                .isAir()) {
            endY--;

            // stop checking beyond the minimum y as there will never be any blocks below it
            if (endY < McUtils.mc().level.getMinY()) return -1;
        }

        // add the floor height to the result to account for half-blocks
        endY += McUtils.mc()
                .level
                .getBlockFloorHeight(new BlockPos(
                        McUtils.player().blockPosition().getX(),
                        (int) endY,
                        McUtils.player().blockPosition().getZ()));
        return McUtils.player().position().y - endY;
    }

    public CappedValue getItemCooldownTicks(ItemStack itemStack) {
        ItemCooldowns cooldowns = McUtils.player().getCooldowns();
        ResourceLocation resourceLocation = cooldowns.getCooldownGroup(itemStack);
        ItemCooldowns.CooldownInstance cooldown = cooldowns.cooldowns.get(resourceLocation);
        if (cooldown == null || cooldown.startTime >= cooldown.endTime) return CappedValue.EMPTY; // Sanity check

        int remaining = cooldown.endTime - cooldowns.tickCount;
        if (remaining <= 0) return CappedValue.EMPTY;

        return new CappedValue(remaining, cooldown.endTime - cooldown.startTime);
    }

    public List<GearInfo> getWornGear() {
        Player player = McUtils.player();

        // Check if main hand has valid weapon. We can hold weapons we can't yield, so we need
        // to check that it is indeed a valid and usable weapon
        List<GearInfo> wornGear = new ArrayList<>();
        Optional<GearItem> mainHandGearItem = Models.Item.asWynnItem(player.getMainHandItem(), GearItem.class);
        if (mainHandGearItem.isPresent()) {
            GearInfo gearInfo = mainHandGearItem.get().getItemInfo();
            if (gearInfo.type().isValidWeapon(Models.Character.getClassType())
                    && Models.CombatXp.getCombatLevel().current()
                            >= gearInfo.requirements().level()) {
                wornGear.add(gearInfo);
            }
        }

        // We trust that Wynncraft do not let us wear invalid gear, so no further validation checks are needed

        // Check armor slots
        player.getArmorSlots().forEach(itemStack -> {
            Optional<GearItem> armorGearItem = Models.Item.asWynnItem(itemStack, GearItem.class);
            if (armorGearItem.isPresent()) {
                GearInfo gearInfo = armorGearItem.get().getItemInfo();
                wornGear.add(gearInfo);
            }
        });

        // Check accessory slots
        InventoryUtils.getAccessories(player).forEach(itemStack -> {
            Optional<GearItem> accessoryGearItem = Models.Item.asWynnItem(itemStack, GearItem.class);
            if (accessoryGearItem.isPresent()) {
                GearInfo gearInfo = accessoryGearItem.get().getItemInfo();
                wornGear.add(gearInfo);
            }
        });

        return wornGear;
    }

    public int getLevel() {
        return level;
    }

    public Optional<CappedValue> getHealth() {
        if (health == CappedValue.EMPTY) return Optional.empty();
        return Optional.of(health);
    }

    public Optional<CappedValue> getMana() {
        if (mana == CappedValue.EMPTY) return Optional.empty();
        return Optional.of(mana);
    }

    public Optional<CappedValue> getSprint() {
        if (sprint == CappedValue.EMPTY) return Optional.empty();
        return Optional.of(sprint);
    }

    public Optional<PowderSpecialInfo> getPowderSpecialInfo() {
        if (powderSpecialInfo == PowderSpecialInfo.EMPTY) return Optional.empty();
        return Optional.of(powderSpecialInfo);
    }

    public void setHideHealth(boolean shouldHide) {
        if (shouldHide) {
            hiddenSegments.add(HealthTextSegment.class);
            hiddenSegments.add(HealthBarSegment.class);
        } else {
            hiddenSegments.remove(HealthTextSegment.class);
            hiddenSegments.remove(HealthBarSegment.class);
        }
    }

    public void setHideMana(boolean shouldHide) {
        if (shouldHide) {
            hiddenSegments.add(ManaTextSegment.class);
            hiddenSegments.add(ManaBarSegment.class);
        } else {
            hiddenSegments.remove(ManaTextSegment.class);
            hiddenSegments.remove(ManaBarSegment.class);
        }
    }

    public void setHidePowder(boolean shouldHide) {
        if (shouldHide) {
            hiddenSegments.add(PowderSpecialSegment.class);
        } else {
            hiddenSegments.remove(PowderSpecialSegment.class);
        }
    }

    private void updateLevel(LevelSegment segment) {
        // If the profession experience segment is present, the displayed level is relative to the profession,
        // which we do not want to update here.
        if (isProfessionExperience) return;
        level = segment.getLevel();
    }

    private void updateHealth(HealthTextSegment segment) {
        health = segment.getHealth();
    }

    private void updateMana(ManaTextSegment segment) {
        mana = segment.getMana();
    }

    private void updateSprint(MeterBarSegment segment) {
        MeterBarInfo meterBarInfo = segment.getMeterBarInfo();
        if (meterBarInfo.type() != MeterBarInfo.MeterActionType.SPRINT
                && meterBarInfo.type() != MeterBarInfo.MeterActionType.BOTH) {
            return;
        }
        sprint = meterBarInfo.value();
    }

    private void updatePowderSpecial(PowderSpecialSegment segment) {
        powderSpecialInfo = segment.getPowderSpecialInfo();
    }

    private void updateCombatExperience(CombatExperienceSegment combatExperienceSegment) {
        // Combat experience segments are handled in CombatXpModel, so we do not need to do anything here.
        isProfessionExperience = false;
    }

    private void updateProfessionExperience(ProfessionExperienceSegment professionExperienceSegment) {
        // Profession experience segments are handled in ProfessionModel, from harvesting, as those values are far
        // more precise than the action bar segments.
        isProfessionExperience = true;
    }
}
