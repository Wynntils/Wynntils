/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.war.type.WarBattleInfo;
import com.wynntils.utils.type.RangedValue;
import com.wynntils.utils.type.Time;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import com.wynntils.templates.annotations.TemplateFunction;

//Functions are accessed via reflection
@SuppressWarnings("unused")
public class WarFunctions {

    // These are static, and only change per war
    // Initial tower state functions
    // Current tower state functions
    @TemplateFunction(name = "aura_timer")
    public double auraTimerFunction() {
        return Models.GuildWarTower.getRemainingTimeUntilAura() / 1000d;
    }

    @TemplateFunction(name = "volley_timer")
    public double volleyTimerFunction() {
        return Models.GuildWarTower.getRemainingTimeUntilVolley() / 1000d;
    }

    @TemplateFunction(name = "tower_owner")
    public String towerOwnerFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return "-";
        return warBattleInfoOpt.get().getOwnerGuild();
    }

    @TemplateFunction(name = "tower_territory")
    public String towerTerritoryFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return "-";
        return warBattleInfoOpt.get().getTerritory();
    }

    @TemplateFunction(name = "initial_tower_health")
    public long initialTowerHealthFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return -1L;
        return warBattleInfoOpt.get().getInitialState().health();
    }

    @TemplateFunction(name = "initial_tower_defense")
    public double initialTowerDefenseFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return -1d;
        return warBattleInfoOpt.get().getInitialState().defense();
    }

    @TemplateFunction(name = "initial_tower_damage")
    public RangedValue initialTowerDamageFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return RangedValue.NONE;
        return warBattleInfoOpt.get().getInitialState().damage();
    }

    @TemplateFunction(name = "initial_tower_attack_speed")
    public double initialTowerAttackSpeedFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return -1d;
        return warBattleInfoOpt.get().getInitialState().attackSpeed();
    }

    @TemplateFunction(name = "current_tower_health")
    public long currentTowerHealthFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return -1L;
        return warBattleInfoOpt.get().getCurrentState().health();
    }

    @TemplateFunction(name = "current_tower_defense")
    public double currentTowerDefenseFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return -1d;
        return warBattleInfoOpt.get().getCurrentState().defense();
    }

    @TemplateFunction(name = "current_tower_damage")
    public RangedValue currentTowerDamageFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return RangedValue.NONE;
        return warBattleInfoOpt.get().getCurrentState().damage();
    }

    @TemplateFunction(name = "current_tower_attack_speed")
    public double currentTowerAttackSpeedFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return -1d;
        return warBattleInfoOpt.get().getCurrentState().attackSpeed();
    }

    @TemplateFunction(name = "war_start")
    public Time warStartFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return Time.NONE;
        return Time.of(warBattleInfoOpt.get().getInitialState().timestamp());
    }

    @TemplateFunction(name = "time_in_war")
    public long timeInWarFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return -1L;
        return warBattleInfoOpt.get().getTotalLengthSeconds();
    }

    @TemplateFunction(name = "tower_effective_hp")
    public long towerEffectiveHpFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return -1L;
        return warBattleInfoOpt.get().getTowerEffectiveHp();
    }

    @TemplateFunction(name = "tower_dps")
    public RangedValue towerDpsFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return RangedValue.NONE;
        return warBattleInfoOpt.get().getTowerDps();
    }

    @TemplateFunction(name = "team_dps")
    public long teamDpsFunction(long seconds) {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return -1L;
        return warBattleInfoOpt.get().getDps(seconds);
    }

    @TemplateFunction(name = "estimated_war_end")
    public Time estimatedWarEndFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return Time.NONE;
        int timeRemaining = (int) warBattleInfoOpt.get().getEstimatedTimeRemaining();
        return Time.now().offset(timeRemaining);
    }

    @TemplateFunction(name = "estimated_time_to_finish_war")
    public long estimatedTimeToFinishWarFunction() {
        Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
        if (warBattleInfoOpt.isEmpty())
            return -1L;
        return warBattleInfoOpt.get().getEstimatedTimeRemaining();
    }

    @TemplateFunction(name = "is_territory_queued", aliases = { "is_queued" })
    public boolean isTerritoryQueuedFunction(String territoryName) {
        return Models.GuildAttackTimer.getAttackTimerForTerritory(territoryName).isPresent();
    }

    @TemplateFunction(name = "wars_since")
    public long warsSinceFunction(int sinceDays) {
        return Models.War.historicWars.get().stream().filter(historicWarInfo -> historicWarInfo.endedTimestamp() >= System.currentTimeMillis() - TimeUnit.DAYS.toMillis(sinceDays)).count();
    }
}
