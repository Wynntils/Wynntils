package com.wynntils.models.abilitytree.type;

import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public enum AbilityTreeNodeType {
    // === Base class abilities ===
    ARCHER_ABILITY_LOCKED("abilityTree.archerAbilityLocked", false, ClassType.ARCHER, AbilityTreeNodeState.LOCKED),
    ARCHER_ABILITY_UNLOCKABLE("abilityTree.archerAbilityUnlockable", false, ClassType.ARCHER, AbilityTreeNodeState.UNLOCKABLE),
    ARCHER_ABILITY_UNLOCKED("abilityTree.archerAbilityUnlocked", false, ClassType.ARCHER, AbilityTreeNodeState.UNLOCKED),

    ASSASSIN_ABILITY_LOCKED("abilityTree.assassinAbilityLocked", false, ClassType.ASSASSIN, AbilityTreeNodeState.LOCKED),
    ASSASSIN_ABILITY_UNLOCKABLE("abilityTree.assassinAbilityUnlockable", false, ClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKABLE),
    ASSASSIN_ABILITY_UNLOCKED("abilityTree.assassinAbilityUnlocked", false, ClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKED),

    MAGE_ABILITY_LOCKED("abilityTree.mageAbilityLocked", false, ClassType.MAGE, AbilityTreeNodeState.LOCKED),
    MAGE_ABILITY_UNLOCKABLE("abilityTree.mageAbilityUnlockable", false, ClassType.MAGE, AbilityTreeNodeState.UNLOCKABLE),
    MAGE_ABILITY_UNLOCKED("abilityTree.mageAbilityUnlocked", false, ClassType.MAGE, AbilityTreeNodeState.UNLOCKED),

    SHAMAN_ABILITY_LOCKED("abilityTree.shamanAbilityLocked", false, ClassType.SHAMAN, AbilityTreeNodeState.LOCKED),
    SHAMAN_ABILITY_UNLOCKABLE("abilityTree.shamanAbilityUnlockable", false, ClassType.SHAMAN, AbilityTreeNodeState.UNLOCKABLE),
    SHAMAN_ABILITY_UNLOCKED("abilityTree.shamanAbilityUnlocked", false, ClassType.SHAMAN, AbilityTreeNodeState.UNLOCKED),

    WARRIOR_ABILITY_LOCKED("abilityTree.warriorAbilityLocked", false, ClassType.WARRIOR, AbilityTreeNodeState.LOCKED),
    WARRIOR_ABILITY_UNLOCKABLE("abilityTree.warriorAbilityUnlockable", false, ClassType.WARRIOR, AbilityTreeNodeState.UNLOCKABLE),
    WARRIOR_ABILITY_UNLOCKED("abilityTree.warriorAbilityUnlocked", false, ClassType.WARRIOR, AbilityTreeNodeState.UNLOCKED),

    // === Archer ultimates ===
    BOLTSLINGER_ULTIMATE_LOCKED("abilityTree.boltslingerUltimateLocked", true, ClassType.ARCHER, AbilityTreeNodeState.LOCKED),
    BOLTSLINGER_ULTIMATE_UNLOCKABLE("abilityTree.boltslingerUltimateUnlockable", true, ClassType.ARCHER, AbilityTreeNodeState.UNLOCKABLE),
    BOLTSLINGER_ULTIMATE_BLOCKED("abilityTree.boltslingerUltimateBlocked", true, ClassType.ARCHER, AbilityTreeNodeState.BLOCKED),
    BOLTSLINGER_ULTIMATE_UNLOCKED("abilityTree.boltslingerUltimateUnlocked", true, ClassType.ARCHER, AbilityTreeNodeState.UNLOCKED),

    TRAPPER_ULTIMATE_LOCKED("abilityTree.trapperUltimateLocked", true, ClassType.ARCHER, AbilityTreeNodeState.LOCKED),
    TRAPPER_ULTIMATE_UNLOCKABLE("abilityTree.trapperUltimateUnlockable", true, ClassType.ARCHER, AbilityTreeNodeState.UNLOCKABLE),
    TRAPPER_ULTIMATE_BLOCKED("abilityTree.trapperUltimateBlocked", true, ClassType.ARCHER, AbilityTreeNodeState.BLOCKED),
    TRAPPER_ULTIMATE_UNLOCKED("abilityTree.trapperUltimateUnlocked", true, ClassType.ARCHER, AbilityTreeNodeState.UNLOCKED),

    SHARPSHOOTER_ULTIMATE_LOCKED("abilityTree.sharpshooterUltimateLocked", true, ClassType.ARCHER, AbilityTreeNodeState.LOCKED),
    SHARPSHOOTER_ULTIMATE_UNLOCKABLE("abilityTree.sharpshooterUltimateUnlockable", true, ClassType.ARCHER, AbilityTreeNodeState.UNLOCKABLE),
    SHARPSHOOTER_ULTIMATE_BLOCKED("abilityTree.sharpshooterUltimateBlocked", true, ClassType.ARCHER, AbilityTreeNodeState.BLOCKED),
    SHARPSHOOTER_ULTIMATE_UNLOCKED("abilityTree.sharpshooterUltimateUnlocked", true, ClassType.ARCHER, AbilityTreeNodeState.UNLOCKED),

    // === Assassin ultimates ===
    SHADESTEPPER_ULTIMATE_LOCKED("abilityTree.shadestepperUltimateLocked", true, ClassType.ASSASSIN, AbilityTreeNodeState.LOCKED),
    SHADESTEPPER_ULTIMATE_UNLOCKABLE("abilityTree.shadestepperUltimateUnlockable", true, ClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKABLE),
    SHADESTEPPER_ULTIMATE_BLOCKED("abilityTree.shadestepperUltimateBlocked", true, ClassType.ASSASSIN, AbilityTreeNodeState.BLOCKED),
    SHADESTEPPER_ULTIMATE_UNLOCKED("abilityTree.shadestepperUltimateUnlocked", true, ClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKED),

    TRICKSTER_ULTIMATE_LOCKED("abilityTree.tricksterUltimateLocked", true, ClassType.ASSASSIN, AbilityTreeNodeState.LOCKED),
    TRICKSTER_ULTIMATE_UNLOCKABLE("abilityTree.tricksterUltimateUnlockable", true, ClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKABLE),
    TRICKSTER_ULTIMATE_BLOCKED("abilityTree.tricksterUltimateBlocked", true, ClassType.ASSASSIN, AbilityTreeNodeState.BLOCKED),
    TRICKSTER_ULTIMATE_UNLOCKED("abilityTree.tricksterUltimateUnlocked", true, ClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKED),

    ACROBAT_ULTIMATE_LOCKED("abilityTree.acrobatUltimateLocked", true, ClassType.ASSASSIN, AbilityTreeNodeState.LOCKED),
    ACROBAT_ULTIMATE_UNLOCKABLE("abilityTree.acrobatUltimateUnlockable", true, ClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKABLE),
    ACROBAT_ULTIMATE_BLOCKED("abilityTree.acrobatUltimateBlocked", true, ClassType.ASSASSIN, AbilityTreeNodeState.BLOCKED),
    ACROBAT_ULTIMATE_UNLOCKED("abilityTree.acrobatUltimateUnlocked", true, ClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKED),

    // === Mage ultimates ===
    LIGHTBENDER_ULTIMATE_LOCKED("abilityTree.lightbenderUltimateLocked", true, ClassType.MAGE, AbilityTreeNodeState.LOCKED),
    LIGHTBENDER_ULTIMATE_UNLOCKABLE("abilityTree.lightbenderUltimateUnlockable", true, ClassType.MAGE, AbilityTreeNodeState.UNLOCKABLE),
    LIGHTBENDER_ULTIMATE_BLOCKED("abilityTree.lightbenderUltimateBlocked", true, ClassType.MAGE, AbilityTreeNodeState.BLOCKED),
    LIGHTBENDER_ULTIMATE_UNLOCKED("abilityTree.lightbenderUltimateUnlocked", true, ClassType.MAGE, AbilityTreeNodeState.UNLOCKED),

    RIFTWALKER_ULTIMATE_LOCKED("abilityTree.riftwalkerUltimateLocked", true, ClassType.MAGE, AbilityTreeNodeState.LOCKED),
    RIFTWALKER_ULTIMATE_UNLOCKABLE("abilityTree.riftwalkerUltimateUnlockable", true, ClassType.MAGE, AbilityTreeNodeState.UNLOCKABLE),
    RIFTWALKER_ULTIMATE_BLOCKED("abilityTree.riftwalkerUltimateBlocked", true, ClassType.MAGE, AbilityTreeNodeState.BLOCKED),
    RIFTWALKER_ULTIMATE_UNLOCKED("abilityTree.riftwalkerUltimateUnlocked", true, ClassType.MAGE, AbilityTreeNodeState.UNLOCKED),

    ARCANIST_ULTIMATE_LOCKED("abilityTree.arcanistUltimateLocked", true, ClassType.MAGE, AbilityTreeNodeState.LOCKED),
    ARCANIST_ULTIMATE_UNLOCKABLE("abilityTree.arcanistUltimateUnlockable", true, ClassType.MAGE, AbilityTreeNodeState.UNLOCKABLE),
    ARCANIST_ULTIMATE_BLOCKED("abilityTree.arcanistUltimateBlocked", true, ClassType.MAGE, AbilityTreeNodeState.BLOCKED),
    ARCANIST_ULTIMATE_UNLOCKED("abilityTree.arcanistUltimateUnlocked", true, ClassType.MAGE, AbilityTreeNodeState.UNLOCKED),

    // === Shaman ultimates ===
    SUMMONER_ULTIMATE_LOCKED("abilityTree.summonerUltimateLocked", true, ClassType.SHAMAN, AbilityTreeNodeState.LOCKED),
    SUMMONER_ULTIMATE_UNLOCKABLE("abilityTree.summonerUltimateUnlockable", true, ClassType.SHAMAN, AbilityTreeNodeState.UNLOCKABLE),
    SUMMONER_ULTIMATE_BLOCKED("abilityTree.summonerUltimateBlocked", true, ClassType.SHAMAN, AbilityTreeNodeState.BLOCKED),
    SUMMONER_ULTIMATE_UNLOCKED("abilityTree.summonerUltimateUnlocked", true, ClassType.SHAMAN, AbilityTreeNodeState.UNLOCKED),

    RITUALIST_ULTIMATE_LOCKED("abilityTree.ritualistUltimateLocked", true, ClassType.SHAMAN, AbilityTreeNodeState.LOCKED),
    RITUALIST_ULTIMATE_UNLOCKABLE("abilityTree.ritualistUltimateUnlockable", true, ClassType.SHAMAN, AbilityTreeNodeState.UNLOCKABLE),
    RITUALIST_ULTIMATE_BLOCKED("abilityTree.ritualistUltimateBlocked", true, ClassType.SHAMAN, AbilityTreeNodeState.BLOCKED),
    RITUALIST_ULTIMATE_UNLOCKED("abilityTree.ritualistUltimateUnlocked", true, ClassType.SHAMAN, AbilityTreeNodeState.UNLOCKED),

    ACOLYTE_ULTIMATE_LOCKED("abilityTree.acolyteUltimateLocked", true, ClassType.SHAMAN, AbilityTreeNodeState.LOCKED),
    ACOLYTE_ULTIMATE_UNLOCKABLE("abilityTree.acolyteUltimateUnlockable", true, ClassType.SHAMAN, AbilityTreeNodeState.UNLOCKABLE),
    ACOLYTE_ULTIMATE_BLOCKED("abilityTree.acolyteUltimateBlocked", true, ClassType.SHAMAN, AbilityTreeNodeState.BLOCKED),
    ACOLYTE_ULTIMATE_UNLOCKED("abilityTree.acolyteUltimateUnlocked", true, ClassType.SHAMAN, AbilityTreeNodeState.UNLOCKED),

    // === Warrior ultimates ===
    FALLEN_ULTIMATE_LOCKED("abilityTree.fallenUltimateLocked", true, ClassType.WARRIOR, AbilityTreeNodeState.LOCKED),
    FALLEN_ULTIMATE_UNLOCKABLE("abilityTree.fallenUltimateUnlockable", true, ClassType.WARRIOR, AbilityTreeNodeState.UNLOCKABLE),
    FALLEN_ULTIMATE_BLOCKED("abilityTree.fallenUltimateBlocked", true, ClassType.WARRIOR, AbilityTreeNodeState.BLOCKED),
    FALLEN_ULTIMATE_UNLOCKED("abilityTree.fallenUltimateUnlocked", true, ClassType.WARRIOR, AbilityTreeNodeState.UNLOCKED),

    BATTLEMONK_ULTIMATE_LOCKED("abilityTree.battlemonkUltimateLocked", true, ClassType.WARRIOR, AbilityTreeNodeState.LOCKED),
    BATTLEMONK_ULTIMATE_UNLOCKABLE("abilityTree.battlemonkUltimateUnlockable", true, ClassType.WARRIOR, AbilityTreeNodeState.UNLOCKABLE),
    BATTLEMONK_ULTIMATE_BLOCKED("abilityTree.battlemonkUltimateBlocked", true, ClassType.WARRIOR, AbilityTreeNodeState.BLOCKED),
    BATTLEMONK_ULTIMATE_UNLOCKED("abilityTree.battlemonkUltimateUnlocked", true, ClassType.WARRIOR, AbilityTreeNodeState.UNLOCKED),

    PALADIN_ULTIMATE_LOCKED("abilityTree.paladinUltimateLocked", true, ClassType.WARRIOR, AbilityTreeNodeState.LOCKED),
    PALADIN_ULTIMATE_UNLOCKABLE("abilityTree.paladinUltimateUnlockable", true, ClassType.WARRIOR, AbilityTreeNodeState.UNLOCKABLE),
    PALADIN_ULTIMATE_BLOCKED("abilityTree.paladinUltimateBlocked", true, ClassType.WARRIOR, AbilityTreeNodeState.BLOCKED),
    PALADIN_ULTIMATE_UNLOCKED("abilityTree.paladinUltimateUnlocked", true, ClassType.WARRIOR, AbilityTreeNodeState.UNLOCKED),

    // === Colour abilities ===
    WHITE_ABILITY_LOCKED("abilityTree.whiteAbilityLocked", false, null, AbilityTreeNodeState.LOCKED),
    WHITE_ABILITY_UNLOCKABLE("abilityTree.whiteAbilityUnlockable", false, null, AbilityTreeNodeState.UNLOCKABLE),
    WHITE_ABILITY_BLOCKED("abilityTree.whiteAbilityBlocked", false, null, AbilityTreeNodeState.BLOCKED),
    WHITE_ABILITY_UNLOCKED("abilityTree.whiteAbilityUnlocked", false, null, AbilityTreeNodeState.UNLOCKED),

    YELLOW_ABILITY_LOCKED("abilityTree.yellowAbilityLocked", false, null, AbilityTreeNodeState.LOCKED),
    YELLOW_ABILITY_UNLOCKABLE("abilityTree.yellowAbilityUnlockable", false, null, AbilityTreeNodeState.UNLOCKABLE),
    YELLOW_ABILITY_BLOCKED("abilityTree.yellowAbilityBlocked", false, null, AbilityTreeNodeState.BLOCKED),
    YELLOW_ABILITY_UNLOCKED("abilityTree.yellowAbilityUnlocked", false, null, AbilityTreeNodeState.UNLOCKED),

    PURPLE_ABILITY_LOCKED("abilityTree.purpleAbilityLocked", false, null, AbilityTreeNodeState.LOCKED),
    PURPLE_ABILITY_UNLOCKABLE("abilityTree.purpleAbilityUnlockable", false, null, AbilityTreeNodeState.UNLOCKABLE),
    PURPLE_ABILITY_BLOCKED("abilityTree.purpleAbilityBlocked", false, null, AbilityTreeNodeState.BLOCKED),
    PURPLE_ABILITY_UNLOCKED("abilityTree.purpleAbilityUnlocked", false, null, AbilityTreeNodeState.UNLOCKED),

    BLUE_ABILITY_LOCKED("abilityTree.blueAbilityLocked", false, null, AbilityTreeNodeState.LOCKED),
    BLUE_ABILITY_UNLOCKABLE("abilityTree.blueAbilityUnlockable", false, null, AbilityTreeNodeState.UNLOCKABLE),
    BLUE_ABILITY_BLOCKED("abilityTree.blueAbilityBlocked", false, null, AbilityTreeNodeState.BLOCKED),
    BLUE_ABILITY_UNLOCKED("abilityTree.blueAbilityUnlocked", false, null, AbilityTreeNodeState.UNLOCKED),

    RED_ABILITY_LOCKED("abilityTree.redAbilityLocked", false, null, AbilityTreeNodeState.LOCKED),
    RED_ABILITY_UNLOCKABLE("abilityTree.redAbilityUnlockable", false, null, AbilityTreeNodeState.UNLOCKABLE),
    RED_ABILITY_BLOCKED("abilityTree.redAbilityBlocked", false, null, AbilityTreeNodeState.BLOCKED),
    RED_ABILITY_UNLOCKED("abilityTree.redAbilityUnlocked", false, null, AbilityTreeNodeState.UNLOCKED);

    private final String key;
    private final boolean ultimate;
    private final ClassType classType;
    private final AbilityTreeNodeState state;

    AbilityTreeNodeType(String key, boolean ultimate, ClassType classType, AbilityTreeNodeState state) {
        this.key = key;
        this.ultimate = ultimate;
        this.classType = classType;
        this.state = state;
    }

    public String getKey() {
        return key;
    }

    public boolean isUltimate() {
        return ultimate;
    }

    public ClassType getClassType() {
        return classType;
    }

    public AbilityTreeNodeState getState() {
        return state;
    }

    public Optional<Float> getCustomModelData() {
        return Services.CustomModel.getFloat(key);
    }

    public static AbilityTreeNodeType fromItemStack(ItemStack itemStack) {
        if (!itemStack.has(DataComponents.CUSTOM_MODEL_DATA)) return null;

        List<Float> floats = itemStack.get(DataComponents.CUSTOM_MODEL_DATA).floats();
        if (floats.isEmpty()) return null;

        return fromCustomModelData(floats.getFirst());
    }

    private static AbilityTreeNodeType fromCustomModelData(float customModelData) {
        for (AbilityTreeNodeType type : values()) {
            if (type.matches(customModelData)) {
                return type;
            }
        }
        return null;
    }

    private boolean matches(float customModelData) {
        return Services.CustomModel.getFloat(key)
                .map(value -> value == customModelData)
                .orElse(false);
    }
}