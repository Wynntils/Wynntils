/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.event.ActionBarRenderEvent;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.event.TickEvent;
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
import com.wynntils.models.characterstats.actionbar.matchers.UltimateMeterBarSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.matchers.UltimateMeterTransitionFromNormalSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.matchers.UltimateMeterTransitionToBarSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.matchers.UltimateReadyActivateSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.matchers.UltimateReadyTransitionSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.segments.CombatExperienceSegment;
import com.wynntils.models.characterstats.actionbar.segments.HealthBarSegment;
import com.wynntils.models.characterstats.actionbar.segments.HealthTextSegment;
import com.wynntils.models.characterstats.actionbar.segments.LevelSegment;
import com.wynntils.models.characterstats.actionbar.segments.ManaBarSegment;
import com.wynntils.models.characterstats.actionbar.segments.ManaTextSegment;
import com.wynntils.models.characterstats.actionbar.segments.MeterBarSegment;
import com.wynntils.models.characterstats.actionbar.segments.PowderSpecialSegment;
import com.wynntils.models.characterstats.actionbar.segments.ProfessionExperienceSegment;
import com.wynntils.models.characterstats.parser.CharacterStatsParser;
import com.wynntils.models.characterstats.type.MeterBarInfo;
import com.wynntils.models.characterstats.type.PlayerStat;
import com.wynntils.models.characterstats.type.PowderSpecialInfo;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.DeltaTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.neoforged.bus.api.SubscribeEvent;

public final class CharacterStatsModel extends Model {
    public static final int TICKS_BETWEEN_STAT_LOOKUP = 20 * 20; // 20 seconds
    private static final CharacterStatsParser CHARACTER_STATS_PARSER = new CharacterStatsParser();
    private final Set<Class<? extends ActionBarSegment>> hiddenSegments = new HashSet<>();

    private int level = 0;
    private CappedValue health = CappedValue.EMPTY;
    private CappedValue mana = CappedValue.EMPTY;
    private CappedValue sprint = CappedValue.EMPTY;
    private PowderSpecialInfo powderSpecialInfo = PowderSpecialInfo.EMPTY;
    private final HashMap<StatType, PlayerStat> playerStats = new HashMap<>();

    private boolean isProfessionExperience = false;

    public CharacterStatsModel() {
        super(List.of());

        // Register relevant segment matchers
        Handlers.ActionBar.registerSegment(new HotbarSegmentMatcher());
        Handlers.ActionBar.registerSegment(new MeterBarSegmentMatcher());
        Handlers.ActionBar.registerSegment(new MeterEdgeAnimationSegmentMatcher());
        Handlers.ActionBar.registerSegment(new MeterTransitionSegmentMatcher());
        Handlers.ActionBar.registerSegment(new UltimateMeterBarSegmentMatcher());
        Handlers.ActionBar.registerSegment(new UltimateMeterTransitionFromNormalSegmentMatcher());
        Handlers.ActionBar.registerSegment(new UltimateReadyTransitionSegmentMatcher());
        Handlers.ActionBar.registerSegment(new UltimateMeterTransitionToBarSegmentMatcher());
        Handlers.ActionBar.registerSegment(new UltimateReadyActivateSegmentMatcher());
        Handlers.ActionBar.registerSegment(new LevelSegmentMatcher());
        Handlers.ActionBar.registerSegment(new ManaBarSegmentMatcher());
        Handlers.ActionBar.registerSegment(new HealthBarSegmentMatcher());
        Handlers.ActionBar.registerSegment(new ManaTextSegmentMatcher());
        Handlers.ActionBar.registerSegment(new HealthTextSegmentMatcher());
        Handlers.ActionBar.registerSegment(new PowderSpecialSegmentMatcher());
        Handlers.ActionBar.registerSegment(new ProfessionExperienceSegmentMatcher());
    }

    @SubscribeEvent
    public void onOpenScreen(ScreenOpenedEvent.Pre event) {
        CHARACTER_STATS_PARSER.postponeQuery();
    }

    private int ticksUntilNextLookup = 0;

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (ticksUntilNextLookup > 0) {
            ticksUntilNextLookup--;
            return;
        }

        CHARACTER_STATS_PARSER.queryPlayerStats(stat -> playerStats.put(stat.statType(), stat));
        ticksUntilNextLookup = TICKS_BETWEEN_STAT_LOOKUP;
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

    public float getItemCooldown(DeltaTracker deltaTracker) {
        ItemCooldowns.CooldownInstance cooldown = getActiveCooldown();
        if (cooldown == null) return 0.0F;

        ItemCooldowns cooldowns = McUtils.player().getCooldowns();

        float total = cooldown.endTime() - cooldown.startTime();
        float remaining = cooldown.endTime() - (cooldowns.tickCount + deltaTracker.getGameTimeDeltaPartialTick(true));

        return Mth.clamp(remaining / total, 0.0F, 1.0F);
    }

    public CappedValue getItemCooldownTicks() {
        ItemCooldowns.CooldownInstance cooldown = getActiveCooldown();
        if (cooldown == null) return CappedValue.EMPTY;

        ItemCooldowns cooldowns = McUtils.player().getCooldowns();

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
        player.equipment.items.values().forEach(itemStack -> {
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

    public HashMap<StatType, PlayerStat> getPlayerStats() {
        return playerStats;
    }

    public Optional<PlayerStat> getStatByApiName(String statName) {
        StatType type = Models.Stat.fromApiName(statName);
        if (type == null) return Optional.empty();
        return Optional.of(this.playerStats.getOrDefault(type, null));
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

    // Prior to 2.2 we would check the main hand item for the cooldown but for some reason
    // the main hand item does not always have a cooldown anymore so we just use the first
    // one in the cooldown map
    private ItemCooldowns.CooldownInstance getActiveCooldown() {
        if (InventoryUtils.getItemInHand().isEmpty()) return null;

        ItemCooldowns cooldowns = McUtils.player().getCooldowns();
        if (cooldowns.cooldowns.isEmpty()) return null;

        ItemCooldowns.CooldownInstance cooldown =
                cooldowns.cooldowns.values().iterator().next();
        return cooldown.startTime() < cooldown.endTime() ? cooldown : null;
    }
}
