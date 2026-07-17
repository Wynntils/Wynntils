/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.abilities.label.ShamanPuppetInfo;
import com.wynntils.models.abilities.type.AbilityCooldown;
import com.wynntils.models.abilities.type.PuppetType;
import com.wynntils.models.character.type.VehicleType;
import com.wynntils.models.characterstats.type.PowderSpecialInfo;
import com.wynntils.models.objectives.WynnObjective;
import com.wynntils.services.leaderboard.type.LeaderboardType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.NamedValue;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.player.LocalPlayer;
import com.wynntils.templates.annotations.TemplateFunction;

//Functions are accessed via reflection
@SuppressWarnings("unused")
public class CharacterFunctions {

    @TemplateFunction(name = "ability_cooldown")
    public float abilityCooldownFunction(String name, boolean interpolated) {
        AbilityCooldown cooldown = AbilityCooldown.fromName(name);
        if (cooldown == null || !Models.Ability.getActiveCooldowns().contains(cooldown))
            return -1.0f;
        return interpolated ? Models.Ability.getInterpolatedCooldown(cooldown) : cooldown.getServerRemainingSeconds();
    }

    @TemplateFunction(name = "capped_mana")
    public CappedValue cappedManaFunction() {
        return Models.CharacterStats.getMana().orElse(CappedValue.EMPTY);
    }

    @TemplateFunction(name = "capped_health")
    public CappedValue cappedHealthFunction() {
        return Models.CharacterStats.getHealth().orElse(CappedValue.EMPTY);
    }

    @TemplateFunction(name = "sprint")
    public CappedValue sprintFunction() {
        return Models.CharacterStats.getSprint().orElse(CappedValue.EMPTY);
    }

    @TemplateFunction(name = "bps")
    public double bpsFunction() {
        LocalPlayer player = McUtils.player();
        double dX = player.getX() - player.xOld;
        double dZ = player.getZ() - player.zOld;
        double dY = player.getY() - player.yOld;
        return Math.sqrt((dX * dX) + (dZ * dZ) + (dY * dY)) * 20;
    }

    @TemplateFunction(name = "bps_xz")
    public double bpsXzFunction() {
        LocalPlayer player = McUtils.player();
        double dX = player.getX() - player.xOld;
        double dZ = player.getZ() - player.zOld;
        return Math.sqrt((dX * dX) + (dZ * dZ)) * 20;
    }

    @TemplateFunction(name = "class")
    public String classFunction() {
        return classFunction(false, true);
    }

    @TemplateFunction(name = "class")
    public String classFunction(boolean showReskinnedName, boolean uppercase) {
        String name = showReskinnedName ? Models.Character.getActualName() : Models.Character.getClassType().getActualName(false);
        if (uppercase) {
            return name.toUpperCase(Locale.ROOT);
        }
        return name;
    }

    @TemplateFunction(name = "mana")
    public int manaFunction() {
        return Models.CharacterStats.getMana().orElse(CappedValue.EMPTY).current();
    }

    @TemplateFunction(name = "mana_max")
    public int manaMaxFunction() {
        return Models.CharacterStats.getMana().orElse(CappedValue.EMPTY).max();
    }

    @TemplateFunction(name = "health")
    public int healthFunction() {
        return Models.CharacterStats.getHealth().orElse(CappedValue.EMPTY).current();
    }

    @TemplateFunction(name = "health_max")
    public int healthMaxFunction() {
        return Models.CharacterStats.getHealth().orElse(CappedValue.EMPTY).max();
    }

    @TemplateFunction(name = "health_pct")
    public double healthPctFunction() {
        return Models.CharacterStats.getHealth().orElse(CappedValue.EMPTY).getPercentage();
    }

    @TemplateFunction(name = "mana_pct")
    public double manaPctFunction() {
        return Models.CharacterStats.getMana().orElse(CappedValue.EMPTY).getPercentage();
    }

    @TemplateFunction(name = "id")
    public String idFunction() {
        return Models.Character.getId();
    }

    @TemplateFunction(name = "capped_awakened_progress")
    public CappedValue cappedAwakenedProgressFunction() {
        return Models.Ability.awakenedBar.isActive() ? Models.Ability.awakenedBar.getBarProgress().value() : CappedValue.EMPTY;
    }

    @TemplateFunction(name = "capped_blood_pool")
    public CappedValue cappedBloodPoolFunction() {
        return Models.Ability.bloodPoolBar.isActive() ? Models.Ability.bloodPoolBar.getBarProgress().value() : CappedValue.EMPTY;
    }

    @TemplateFunction(name = "capped_corrupted")
    public CappedValue cappedCorruptedFunction() {
        return Models.Ability.corruptedBar.isActive() ? Models.Ability.corruptedBar.getBarProgress().value() : CappedValue.EMPTY;
    }

    @TemplateFunction(name = "capped_focus")
    public CappedValue cappedFocusFunction() {
        return Models.Ability.focusBar.isActive() ? Models.Ability.focusBar.getBarProgress().value() : CappedValue.EMPTY;
    }

    @TemplateFunction(name = "capped_mana_bank")
    public CappedValue cappedManaBankFunction() {
        return Models.Ability.manaBankBar.isActive() ? Models.Ability.manaBankBar.getBarProgress().value() : CappedValue.EMPTY;
    }

    @TemplateFunction(name = "capped_ophanim")
    public CappedValue cappedOphanimFunction() {
        return Models.Ability.ophanimBar.isActive() ? Models.Ability.ophanimBar.getBarProgress().value() : CappedValue.EMPTY;
    }

    @TemplateFunction(name = "ophanim_orb")
    public int ophanimOrb(int orbNumber) {
        return orbNumber < Models.Ability.ophanimBar.getOrbs().size() && orbNumber >= 0 ? Models.Ability.ophanimBar.getOrbs().get(orbNumber).getHealthState() : -1;
    }

    @TemplateFunction(name = "ophanim_active")
    public boolean ophanimActive() {
        return Models.Ability.ophanimBar.isActive();
    }

    @TemplateFunction(name = "capped_holy_power", aliases = { "capped_sacred_surge" })
    public CappedValue cappedHolyPowerFunction() {
        return Models.Ability.holyPowerBar.isActive() ? Models.Ability.holyPowerBar.getBarProgress().value() : CappedValue.EMPTY;
    }

    @TemplateFunction(name = "commander_duration")
    public int commanderDurationFunction() {
        return Models.Ability.commanderBar.isActive() ? Models.Ability.commanderBar.getDuration() : 0;
    }

    @TemplateFunction(name = "commander_activated")
    public boolean commanderActivatedFunction() {
        return Models.Ability.commanderBar.isActive() && Models.Ability.commanderBar.isActivated();
    }

    @TemplateFunction(name = "momentum_percent", aliases = { "momentum_pct" })
    public CappedValue momentumPercentFunction() {
        return Models.Ability.momentumBar.isActive() ? Models.Ability.momentumBar.getBarProgress().value() : CappedValue.EMPTY;
    }

    @TemplateFunction(name = "momentum")
    public int momentumFunction() {
        return Models.Ability.momentumBar.getMomentum();
    }

    @TemplateFunction(name = "is_riding_horse")
    public boolean isRidingHorseFunction() {
        return Models.Character.getVehicle() == VehicleType.HORSE;
    }

    @TemplateFunction(name = "has_no_gui")
    public boolean hasNoGuiFunction() {
        return Models.Cutscene.isCutsceneActive();
    }

    @TemplateFunction(name = "hummingbirds_state")
    public boolean hummingbirdsStateFunction() {
        return Models.ShamanSummon.hummingBirdsState;
    }

    @TemplateFunction(name = "ophanim_healing_percent")
    public int ophanimHealingPercentFunction() {
        return Models.Ability.ophanimBar.isActive() ? Models.Ability.ophanimBar.getHealed() : -1;
    }

    @TemplateFunction(name = "guild_objective_score")
    public CappedValue guildObjectiveScoreFunction() {
        WynnObjective weekly = Models.Objectives.getGuildObjective();
        if (weekly == null)
            return CappedValue.EMPTY;
        return weekly.getScore();
    }

    @TemplateFunction(name = "guild_objective_goal")
    public String guildObjectiveGoalFunction() {
        WynnObjective weekly = Models.Objectives.getGuildObjective();
        if (weekly == null)
            return "";
        return weekly.getGoal();
    }

    @TemplateFunction(name = "guild_objective_event_bonus")
    public boolean guildObjectiveEventBonusFunction() {
        WynnObjective weekly = Models.Objectives.getGuildObjective();
        if (weekly == null)
            return false;
        return weekly.hasEventBonus();
    }

    @TemplateFunction(name = "personal_objective_score")
    public CappedValue personalObjectiveScoreFunction() {
        return personalObjectiveScoreFunction(0);
    }

    @TemplateFunction(name = "personal_objective_score")
    public CappedValue personalObjectiveScoreFunction(int index) {
        List<WynnObjective> daily = Models.Objectives.getPersonalObjectives();
        return !daily.isEmpty() && index >= 0 && daily.size() > index ? daily.get(index).getScore() : CappedValue.EMPTY;
    }

    @TemplateFunction(name = "personal_objective_goal")
    public String personalObjectiveGoalFunction() {
        return personalObjectiveGoalFunction(0);
    }

    @TemplateFunction(name = "personal_objective_goal")
    public String personalObjectiveGoalFunction(int index) {
        List<WynnObjective> daily = Models.Objectives.getPersonalObjectives();
        return !daily.isEmpty() && index >= 0 && daily.size() > index ? daily.get(index).getGoal() : "";
    }

    @TemplateFunction(name = "personal_objective_event_bonus")
    public boolean personalObjectiveEventBonusFunction() {
        return personalObjectiveEventBonusFunction(0);
    }

    @TemplateFunction(name = "personal_objective_event_bonus")
    public boolean personalObjectiveEventBonusFunction(int index) {
        List<WynnObjective> daily = Models.Objectives.getPersonalObjectives();
        return !daily.isEmpty() && index >= 0 && daily.size() > index && daily.get(index).hasEventBonus();
    }

    @TemplateFunction(name = "equipped_aspect")
    public NamedValue equippedAspectFunction(int index) {
        int aspectIndex = index;
        Optional<String> equippedAspectOpt = Models.Aspect.getEquippedAspect(aspectIndex);
        if (equippedAspectOpt.isEmpty())
            return NamedValue.EMPTY;
        Optional<Integer> aspectTierOpt = Models.Aspect.getAspectTierByName(equippedAspectOpt.get());
        return aspectTierOpt.map(s -> new NamedValue(equippedAspectOpt.get(), aspectTierOpt.get())).orElse(NamedValue.EMPTY);
    }

    @TemplateFunction(name = "is_aspect_equipped")
    public boolean isAspectEquippedFunction(String aspectName) {
        return Models.Aspect.getEquippedAspectByName(aspectName).isPresent();
    }

    @TemplateFunction(name = "aspect_tier")
    public int aspectTierFunction(String aspectName) {
        return Models.Aspect.getAspectTierByName(aspectName).orElse(0);
    }

    @TemplateFunction(name = "leaderboard_position")
    public int leaderboardPositionFunction(String leaderboardKey) {
        LeaderboardType leaderboardType = LeaderboardType.fromKey(leaderboardKey);
        if (leaderboardType == null)
            return 0;
        return Models.Account.getPlayerInfo().leaderboardPlacements().getOrDefault(leaderboardType, 0);
    }

    @TemplateFunction(name = "powder_special_charge")
    public CappedValue powderSpecialChargeFunction() {
        Optional<PowderSpecialInfo> powderSpecialInfoOpt = Models.CharacterStats.getPowderSpecialInfo();
        if (powderSpecialInfoOpt.isEmpty())
            return CappedValue.EMPTY;
        return CappedValue.fromProgress(powderSpecialInfoOpt.get().charge(), 100);
    }

    @TemplateFunction(name = "current_distortion")
    public int currentDistortionFunction() {
        return Models.Ability.distortionBar.getCurrent();
    }

    @TemplateFunction(name = "mirror_image_clone")
    public int mirrorImageCloneFunction(int cloneNumber) {
        return cloneNumber < Models.Ability.mirrorImageBar.getClones().size() && cloneNumber >= 0 ? Models.Ability.mirrorImageBar.getClones().get(cloneNumber).getActiveState() : -1;
    }

    @TemplateFunction(name = "mirror_image_duration")
    public int mirrorImageDurationFunction() {
        return Models.Ability.mirrorImageBar.isActive() ? Models.Ability.mirrorImageBar.getDuration() : 0;
    }

    @TemplateFunction(name = "puppet_count")
    public int puppetCountFunction() {
        return Math.toIntExact(Models.ShamanSummon.getActivePuppetsByType(PuppetType.PUPPET).count());
    }

    @TemplateFunction(name = "remnant_count")
    public int remnantCountFunction() {
        return Math.toIntExact(Models.ShamanSummon.getActivePuppetsByType(PuppetType.REMNANT).count());
    }

    @TemplateFunction(name = "patchwork_abomination_duration")
    public int patchworkAbominationDurationFunction() {
        return Models.ShamanSummon.getActivePuppetsByType(PuppetType.PATCHWORK_ABOMINATION).findFirst().map(ShamanPuppetInfo::getSecondsLeft).orElse(-1);
    }

    @TemplateFunction(name = "puppets_in_time_range")
    public int puppetsInTimeRangeFunction(int max, int min) {
        if (min > max) {
            int tempMax = max;
            max = min;
            min = tempMax;
        }
        int finalMin = min;
        int finalMax = max;
        return Math.toIntExact(Models.ShamanSummon.getActivePuppetsLabels().map(ShamanPuppetInfo::getSecondsLeft).filter(s -> s >= finalMin && finalMax >= s).count());
    }

    @TemplateFunction(name = "crow_count")
    public int crowCountFunction() {
        return Models.ArcherBeast.getActiveCrowCount();
    }

    @TemplateFunction(name = "hounds_time_left")
    public int houndsTimeLeftFunction() {
        return Models.ArcherBeast.getHoundsTimeLeft();
    }

    @TemplateFunction(name = "snake_count")
    public int snakeCountFunction() {
        return Models.ArcherBeast.getActiveSnakeCount();
    }
}
