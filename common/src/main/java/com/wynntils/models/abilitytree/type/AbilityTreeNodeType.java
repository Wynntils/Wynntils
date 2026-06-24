package com.wynntils.models.abilitytree.type;

import com.wynntils.core.components.Services;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public enum AbilityTreeNodeType {
    // === Base class abilities ===
    ARCHER_ABILITY_LOCKED("abilityTree.archerAbilityLocked", false, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.LOCKED),
    ARCHER_ABILITY_UNLOCKABLE("abilityTree.archerAbilityUnlockable", false, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.UNLOCKABLE),
    ARCHER_ABILITY_UNLOCKED("abilityTree.archerAbilityUnlocked", false, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.UNLOCKED),

    ASSASSIN_ABILITY_LOCKED("abilityTree.assassinAbilityLocked", false, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.LOCKED),
    ASSASSIN_ABILITY_UNLOCKABLE("abilityTree.assassinAbilityUnlockable", false, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKABLE),
    ASSASSIN_ABILITY_UNLOCKED("abilityTree.assassinAbilityUnlocked", false, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKED),

    MAGE_ABILITY_LOCKED("abilityTree.mageAbilityLocked", false, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.LOCKED),
    MAGE_ABILITY_UNLOCKABLE("abilityTree.mageAbilityUnlockable", false, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.UNLOCKABLE),
    MAGE_ABILITY_UNLOCKED("abilityTree.mageAbilityUnlocked", false, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.UNLOCKED),

    SHAMAN_ABILITY_LOCKED("abilityTree.shamanAbilityLocked", false, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.LOCKED),
    SHAMAN_ABILITY_UNLOCKABLE("abilityTree.shamanAbilityUnlockable", false, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.UNLOCKABLE),
    SHAMAN_ABILITY_UNLOCKED("abilityTree.shamanAbilityUnlocked", false, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.UNLOCKED),

    WARRIOR_ABILITY_LOCKED("abilityTree.warriorAbilityLocked", false, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.LOCKED),
    WARRIOR_ABILITY_UNLOCKABLE("abilityTree.warriorAbilityUnlockable", false, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.UNLOCKABLE),
    WARRIOR_ABILITY_UNLOCKED("abilityTree.warriorAbilityUnlocked", false, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.UNLOCKED),

    // === Archer ultimates ===
    BOLTSLINGER_ULTIMATE_LOCKED("abilityTree.boltslingerUltimateLocked", true, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.LOCKED),
    BOLTSLINGER_ULTIMATE_UNLOCKABLE("abilityTree.boltslingerUltimateUnlockable", true, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.UNLOCKABLE),
    BOLTSLINGER_ULTIMATE_BLOCKED("abilityTree.boltslingerUltimateBlocked", true, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.BLOCKED),
    BOLTSLINGER_ULTIMATE_UNLOCKED("abilityTree.boltslingerUltimateUnlocked", true, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.UNLOCKED),

    TRAPPER_ULTIMATE_LOCKED("abilityTree.trapperUltimateLocked", true, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.LOCKED),
    TRAPPER_ULTIMATE_UNLOCKABLE("abilityTree.trapperUltimateUnlockable", true, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.UNLOCKABLE),
    TRAPPER_ULTIMATE_BLOCKED("abilityTree.trapperUltimateBlocked", true, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.BLOCKED),
    TRAPPER_ULTIMATE_UNLOCKED("abilityTree.trapperUltimateUnlocked", true, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.UNLOCKED),

    SHARPSHOOTER_ULTIMATE_LOCKED("abilityTree.sharpshooterUltimateLocked", true, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.LOCKED),
    SHARPSHOOTER_ULTIMATE_UNLOCKABLE("abilityTree.sharpshooterUltimateUnlockable", true, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.UNLOCKABLE),
    SHARPSHOOTER_ULTIMATE_BLOCKED("abilityTree.sharpshooterUltimateBlocked", true, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.BLOCKED),
    SHARPSHOOTER_ULTIMATE_UNLOCKED("abilityTree.sharpshooterUltimateUnlocked", true, AbilityTreeNodeClassType.ARCHER, AbilityTreeNodeState.UNLOCKED),

    // === Assassin ultimates ===
    SHADESTEPPER_ULTIMATE_LOCKED("abilityTree.shadestepperUltimateLocked", true, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.LOCKED),
    SHADESTEPPER_ULTIMATE_UNLOCKABLE("abilityTree.shadestepperUltimateUnlockable", true, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKABLE),
    SHADESTEPPER_ULTIMATE_BLOCKED("abilityTree.shadestepperUltimateBlocked", true, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.BLOCKED),
    SHADESTEPPER_ULTIMATE_UNLOCKED("abilityTree.shadestepperUltimateUnlocked", true, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKED),

    TRICKSTER_ULTIMATE_LOCKED("abilityTree.tricksterUltimateLocked", true, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.LOCKED),
    TRICKSTER_ULTIMATE_UNLOCKABLE("abilityTree.tricksterUltimateUnlockable", true, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKABLE),
    TRICKSTER_ULTIMATE_BLOCKED("abilityTree.tricksterUltimateBlocked", true, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.BLOCKED),
    TRICKSTER_ULTIMATE_UNLOCKED("abilityTree.tricksterUltimateUnlocked", true, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKED),

    ACROBAT_ULTIMATE_LOCKED("abilityTree.acrobatUltimateLocked", true, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.LOCKED),
    ACROBAT_ULTIMATE_UNLOCKABLE("abilityTree.acrobatUltimateUnlockable", true, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKABLE),
    ACROBAT_ULTIMATE_BLOCKED("abilityTree.acrobatUltimateBlocked", true, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.BLOCKED),
    ACROBAT_ULTIMATE_UNLOCKED("abilityTree.acrobatUltimateUnlocked", true, AbilityTreeNodeClassType.ASSASSIN, AbilityTreeNodeState.UNLOCKED),

    // === Mage ultimates ===
    LIGHTBENDER_ULTIMATE_LOCKED("abilityTree.lightbenderUltimateLocked", true, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.LOCKED),
    LIGHTBENDER_ULTIMATE_UNLOCKABLE("abilityTree.lightbenderUltimateUnlockable", true, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.UNLOCKABLE),
    LIGHTBENDER_ULTIMATE_BLOCKED("abilityTree.lightbenderUltimateBlocked", true, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.BLOCKED),
    LIGHTBENDER_ULTIMATE_UNLOCKED("abilityTree.lightbenderUltimateUnlocked", true, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.UNLOCKED),

    RIFTWALKER_ULTIMATE_LOCKED("abilityTree.riftwalkerUltimateLocked", true, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.LOCKED),
    RIFTWALKER_ULTIMATE_UNLOCKABLE("abilityTree.riftwalkerUltimateUnlockable", true, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.UNLOCKABLE),
    RIFTWALKER_ULTIMATE_BLOCKED("abilityTree.riftwalkerUltimateBlocked", true, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.BLOCKED),
    RIFTWALKER_ULTIMATE_UNLOCKED("abilityTree.riftwalkerUltimateUnlocked", true, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.UNLOCKED),

    ARCANIST_ULTIMATE_LOCKED("abilityTree.arcanistUltimateLocked", true, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.LOCKED),
    ARCANIST_ULTIMATE_UNLOCKABLE("abilityTree.arcanistUltimateUnlockable", true, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.UNLOCKABLE),
    ARCANIST_ULTIMATE_BLOCKED("abilityTree.arcanistUltimateBlocked", true, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.BLOCKED),
    ARCANIST_ULTIMATE_UNLOCKED("abilityTree.arcanistUltimateUnlocked", true, AbilityTreeNodeClassType.MAGE, AbilityTreeNodeState.UNLOCKED),

    // === Shaman ultimates ===
    SUMMONER_ULTIMATE_LOCKED("abilityTree.summonerUltimateLocked", true, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.LOCKED),
    SUMMONER_ULTIMATE_UNLOCKABLE("abilityTree.summonerUltimateUnlockable", true, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.UNLOCKABLE),
    SUMMONER_ULTIMATE_BLOCKED("abilityTree.summonerUltimateBlocked", true, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.BLOCKED),
    SUMMONER_ULTIMATE_UNLOCKED("abilityTree.summonerUltimateUnlocked", true, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.UNLOCKED),

    RITUALIST_ULTIMATE_LOCKED("abilityTree.ritualistUltimateLocked", true, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.LOCKED),
    RITUALIST_ULTIMATE_UNLOCKABLE("abilityTree.ritualistUltimateUnlockable", true, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.UNLOCKABLE),
    RITUALIST_ULTIMATE_BLOCKED("abilityTree.ritualistUltimateBlocked", true, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.BLOCKED),
    RITUALIST_ULTIMATE_UNLOCKED("abilityTree.ritualistUltimateUnlocked", true, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.UNLOCKED),

    ACOLYTE_ULTIMATE_LOCKED("abilityTree.acolyteUltimateLocked", true, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.LOCKED),
    ACOLYTE_ULTIMATE_UNLOCKABLE("abilityTree.acolyteUltimateUnlockable", true, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.UNLOCKABLE),
    ACOLYTE_ULTIMATE_BLOCKED("abilityTree.acolyteUltimateBlocked", true, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.BLOCKED),
    ACOLYTE_ULTIMATE_UNLOCKED("abilityTree.acolyteUltimateUnlocked", true, AbilityTreeNodeClassType.SHAMAN, AbilityTreeNodeState.UNLOCKED),

    // === Warrior ultimates ===
    FALLEN_ULTIMATE_LOCKED("abilityTree.fallenUltimateLocked", true, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.LOCKED),
    FALLEN_ULTIMATE_UNLOCKABLE("abilityTree.fallenUltimateUnlockable", true, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.UNLOCKABLE),
    FALLEN_ULTIMATE_BLOCKED("abilityTree.fallenUltimateBlocked", true, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.BLOCKED),
    FALLEN_ULTIMATE_UNLOCKED("abilityTree.fallenUltimateUnlocked", true, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.UNLOCKED),

    BATTLEMONK_ULTIMATE_LOCKED("abilityTree.battlemonkUltimateLocked", true, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.LOCKED),
    BATTLEMONK_ULTIMATE_UNLOCKABLE("abilityTree.battlemonkUltimateUnlockable", true, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.UNLOCKABLE),
    BATTLEMONK_ULTIMATE_BLOCKED("abilityTree.battlemonkUltimateBlocked", true, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.BLOCKED),
    BATTLEMONK_ULTIMATE_UNLOCKED("abilityTree.battlemonkUltimateUnlocked", true, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.UNLOCKED),

    PALADIN_ULTIMATE_LOCKED("abilityTree.paladinUltimateLocked", true, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.LOCKED),
    PALADIN_ULTIMATE_UNLOCKABLE("abilityTree.paladinUltimateUnlockable", true, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.UNLOCKABLE),
    PALADIN_ULTIMATE_BLOCKED("abilityTree.paladinUltimateBlocked", true, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.BLOCKED),
    PALADIN_ULTIMATE_UNLOCKED("abilityTree.paladinUltimateUnlocked", true, AbilityTreeNodeClassType.WARRIOR, AbilityTreeNodeState.UNLOCKED),

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
    private final AbilityTreeNodeClassType classType;
    private final AbilityTreeNodeState state;

    AbilityTreeNodeType(String key, boolean ultimate, AbilityTreeNodeClassType classType, AbilityTreeNodeState state) {
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

    public AbilityTreeNodeClassType getClassType() {
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