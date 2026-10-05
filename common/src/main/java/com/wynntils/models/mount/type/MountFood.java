/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

import com.wynntils.core.components.Models;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.profession.type.ResourceType;
import com.wynntils.utils.type.Pair;
import java.util.Optional;

public enum MountFood
{
    COPPER_INGOT(ResourceType.INGOT, 1, Pair.of(MountStat.ENERGY, 4), Pair.of(MountStat.TOUGHNESS, 8)),
    COPPER_GEM(ResourceType.GEM, 1, Pair.of(MountStat.SPEED, 4), Pair.of(MountStat.ENERGY, 2), Pair.of(MountStat.TRAINING, 6)),
    OAK_PLANK(ResourceType.PLANK, 1, Pair.of(MountStat.SPEED, 2), Pair.of(MountStat.ACCELERATION, 6), Pair.of(MountStat.TOUGHNESS, 4)),
    OAK_PAPER(ResourceType.PAPER, 1, Pair.of(MountStat.ALTITUDE, 8), Pair.of(MountStat.BOOST, 4)),
    WHEAT_STRING(ResourceType.STRING, 1, Pair.of(MountStat.ACCELERATION, 2), Pair.of(MountStat.HANDLING, 4), Pair.of(MountStat.BOOST, 6)),
    WHEAT_GRAINS(ResourceType.GRAINS, 1, Pair.of(MountStat.SPEED, 8), Pair.of(MountStat.ALTITUDE, 4)),
    GUDGEON_OIL(ResourceType.OIL, 1, Pair.of(MountStat.ALTITUDE, 2), Pair.of(MountStat.HANDLING, 6), Pair.of(MountStat.TRAINING, 4)),
    GUDGEON_MEAT(ResourceType.MEAT, 1, Pair.of(MountStat.ACCELERATION, 4), Pair.of(MountStat.ENERGY, 8)),

    GRANITE_INGOT(ResourceType.INGOT, 10, Pair.of(MountStat.ENERGY, 5), Pair.of(MountStat.TOUGHNESS, 10)),
    GRANITE_GEM(ResourceType.GEM, 10, Pair.of(MountStat.SPEED, 5), Pair.of(MountStat.ENERGY, 2), Pair.of(MountStat.TRAINING, 8)),
    BIRCH_PLANK(ResourceType.PLANK, 10, Pair.of(MountStat.SPEED, 2), Pair.of(MountStat.ACCELERATION, 8), Pair.of(MountStat.TOUGHNESS, 5)),
    BIRCH_PAPER(ResourceType.PAPER, 10, Pair.of(MountStat.ALTITUDE, 10), Pair.of(MountStat.BOOST, 5)),
    BARLEY_STRING(ResourceType.STRING, 10, Pair.of(MountStat.ACCELERATION, 2), Pair.of(MountStat.HANDLING, 5), Pair.of(MountStat.BOOST, 8)),
    BARLEY_GRAINS(ResourceType.GRAINS, 10, Pair.of(MountStat.SPEED, 10), Pair.of(MountStat.ALTITUDE, 5)),
    TROUT_OIL(ResourceType.OIL, 10, Pair.of(MountStat.ALTITUDE, 2), Pair.of(MountStat.HANDLING, 8), Pair.of(MountStat.TRAINING, 5)),
    TROUT_MEAT(ResourceType.MEAT, 10, Pair.of(MountStat.ACCELERATION, 5), Pair.of(MountStat.ENERGY, 10)),

    GOLD_INGOT(ResourceType.INGOT, 20, Pair.of(MountStat.ENERGY, 5), Pair.of(MountStat.TOUGHNESS, 12)),
    GOLD_GEM(ResourceType.GEM, 20, Pair.of(MountStat.SPEED, 6), Pair.of(MountStat.ENERGY, 3), Pair.of(MountStat.TRAINING, 9)),
    WILLOW_PLANK(ResourceType.PLANK, 20, Pair.of(MountStat.SPEED, 3), Pair.of(MountStat.ACCELERATION, 9), Pair.of(MountStat.TOUGHNESS, 6)),
    WILLOW_PAPER(ResourceType.PAPER, 20, Pair.of(MountStat.ALTITUDE, 12), Pair.of(MountStat.BOOST, 5)),
    OAT_STRING(ResourceType.STRING, 20, Pair.of(MountStat.ACCELERATION, 3), Pair.of(MountStat.HANDLING, 6), Pair.of(MountStat.BOOST, 9)),
    OAT_GRAINS(ResourceType.GRAINS, 20, Pair.of(MountStat.SPEED, 12), Pair.of(MountStat.ALTITUDE, 5)),
    SALMON_OIL(ResourceType.OIL, 20, Pair.of(MountStat.ALTITUDE, 3), Pair.of(MountStat.HANDLING, 9), Pair.of(MountStat.TRAINING, 6)),
    SALMON_MEAT(ResourceType.MEAT, 20, Pair.of(MountStat.ACCELERATION, 5), Pair.of(MountStat.ENERGY, 12)),

    SANDSTONE_INGOT(ResourceType.INGOT, 30, Pair.of(MountStat.ENERGY, 6), Pair.of(MountStat.TOUGHNESS, 14)),
    SANDSTONE_GEM(ResourceType.GEM, 30, Pair.of(MountStat.SPEED, 6), Pair.of(MountStat.ENERGY, 3), Pair.of(MountStat.TRAINING, 11)),
    ACACIA_PLANK(ResourceType.PLANK, 30, Pair.of(MountStat.SPEED, 3), Pair.of(MountStat.ACCELERATION, 11), Pair.of(MountStat.TOUGHNESS, 6)),
    ACACIA_PAPER(ResourceType.PAPER, 30, Pair.of(MountStat.ALTITUDE, 14), Pair.of(MountStat.BOOST, 6)),
    MALT_STRING(ResourceType.STRING, 30, Pair.of(MountStat.ACCELERATION, 3), Pair.of(MountStat.HANDLING, 6), Pair.of(MountStat.BOOST, 11)),
    MALT_GRAINS(ResourceType.GRAINS, 30, Pair.of(MountStat.SPEED, 14), Pair.of(MountStat.ALTITUDE, 6)),
    CARP_OIL(ResourceType.OIL, 30, Pair.of(MountStat.ALTITUDE, 3), Pair.of(MountStat.HANDLING, 11), Pair.of(MountStat.TRAINING, 6)),
    CARP_MEAT(ResourceType.MEAT, 30, Pair.of(MountStat.ACCELERATION, 6), Pair.of(MountStat.ENERGY, 14)),

    IRON_INGOT(ResourceType.INGOT, 40, Pair.of(MountStat.ENERGY, 6), Pair.of(MountStat.TOUGHNESS, 16)),
    IRON_GEM(ResourceType.GEM, 40, Pair.of(MountStat.SPEED, 7), Pair.of(MountStat.ENERGY, 3), Pair.of(MountStat.TRAINING, 12)),
    SPRUCE_PLANK(ResourceType.PLANK, 40, Pair.of(MountStat.SPEED, 3), Pair.of(MountStat.ACCELERATION, 12), Pair.of(MountStat.TOUGHNESS, 7)),
    SPRUCE_PAPER(ResourceType.PAPER, 40, Pair.of(MountStat.ALTITUDE, 16), Pair.of(MountStat.BOOST, 6)),
    HOPS_STRING(ResourceType.STRING, 40, Pair.of(MountStat.ACCELERATION, 3), Pair.of(MountStat.HANDLING, 7), Pair.of(MountStat.BOOST, 12)),
    HOPS_GRAINS(ResourceType.GRAINS, 40, Pair.of(MountStat.SPEED, 16), Pair.of(MountStat.ALTITUDE, 6)),
    ICEFISH_OIL(ResourceType.OIL, 40, Pair.of(MountStat.ALTITUDE, 3), Pair.of(MountStat.HANDLING, 12), Pair.of(MountStat.TRAINING, 7)),
    ICEFISH_MEAT(ResourceType.MEAT, 40, Pair.of(MountStat.ACCELERATION, 6), Pair.of(MountStat.ENERGY, 16)),

    SILVER_INGOT(ResourceType.INGOT, 50, Pair.of(MountStat.ENERGY, 7), Pair.of(MountStat.TOUGHNESS, 18)),
    SILVER_GEM(ResourceType.GEM, 50, Pair.of(MountStat.SPEED, 8), Pair.of(MountStat.ENERGY, 4), Pair.of(MountStat.TRAINING, 14)),
    JUNGLE_PLANK(ResourceType.PLANK, 50, Pair.of(MountStat.SPEED, 4), Pair.of(MountStat.ACCELERATION, 14), Pair.of(MountStat.TOUGHNESS, 8)),
    JUNGLE_PAPER(ResourceType.PAPER, 50, Pair.of(MountStat.ALTITUDE, 18), Pair.of(MountStat.BOOST, 7)),
    RYE_STRING(ResourceType.STRING, 50, Pair.of(MountStat.ACCELERATION, 4), Pair.of(MountStat.HANDLING, 8), Pair.of(MountStat.BOOST, 14)),
    RYE_GRAINS(ResourceType.GRAINS, 50, Pair.of(MountStat.SPEED, 18), Pair.of(MountStat.ALTITUDE, 7)),
    PIRANHA_OIL(ResourceType.OIL, 50, Pair.of(MountStat.ALTITUDE, 4), Pair.of(MountStat.HANDLING, 14), Pair.of(MountStat.TRAINING, 8)),
    PIRANHA_MEAT(ResourceType.MEAT, 50, Pair.of(MountStat.ACCELERATION, 7), Pair.of(MountStat.ENERGY, 18)),

    COBALT_INGOT(ResourceType.INGOT, 60, Pair.of(MountStat.ENERGY, 8), Pair.of(MountStat.TOUGHNESS, 20)),
    COBALT_GEM(ResourceType.GEM, 60, Pair.of(MountStat.SPEED, 9), Pair.of(MountStat.ENERGY, 4), Pair.of(MountStat.TRAINING, 15)),
    DARK_PLANK(ResourceType.PLANK, 60, Pair.of(MountStat.SPEED, 4), Pair.of(MountStat.ACCELERATION, 15), Pair.of(MountStat.TOUGHNESS, 9)),
    DARK_PAPER(ResourceType.PAPER, 60, Pair.of(MountStat.ALTITUDE, 20), Pair.of(MountStat.BOOST, 8)),
    MILLET_STRING(ResourceType.STRING, 60, Pair.of(MountStat.ACCELERATION, 4), Pair.of(MountStat.HANDLING, 9), Pair.of(MountStat.BOOST, 15)),
    MILLET_GRAINS(ResourceType.GRAINS, 60, Pair.of(MountStat.SPEED, 20), Pair.of(MountStat.ALTITUDE, 8)),
    KOI_OIL(ResourceType.OIL, 60, Pair.of(MountStat.ALTITUDE, 4), Pair.of(MountStat.HANDLING, 15), Pair.of(MountStat.TRAINING, 9)),
    KOI_MEAT(ResourceType.MEAT, 60, Pair.of(MountStat.ACCELERATION, 8), Pair.of(MountStat.ENERGY, 20)),

    KANDERSTONE_INGOT(ResourceType.INGOT, 70, Pair.of(MountStat.ENERGY, 8), Pair.of(MountStat.TOUGHNESS, 22)),
    KANDERSTONE_GEM(ResourceType.GEM, 70, Pair.of(MountStat.SPEED, 10), Pair.of(MountStat.ENERGY, 4), Pair.of(MountStat.TRAINING, 17)),
    LIGHT_PLANK(ResourceType.PLANK, 70, Pair.of(MountStat.SPEED, 4), Pair.of(MountStat.ACCELERATION, 17), Pair.of(MountStat.TOUGHNESS, 10)),
    LIGHT_PAPER(ResourceType.PAPER, 70, Pair.of(MountStat.ALTITUDE, 22), Pair.of(MountStat.BOOST, 8)),
    DECAY_STRING(ResourceType.STRING, 70, Pair.of(MountStat.ACCELERATION, 4), Pair.of(MountStat.HANDLING, 10), Pair.of(MountStat.BOOST, 17)),
    DECAY_GRAINS(ResourceType.GRAINS, 70, Pair.of(MountStat.SPEED, 22), Pair.of(MountStat.ALTITUDE, 8)),
    GYLIA_OIL(ResourceType.OIL, 70, Pair.of(MountStat.ALTITUDE, 4), Pair.of(MountStat.HANDLING, 17), Pair.of(MountStat.TRAINING, 10)),
    GYLIA_MEAT(ResourceType.MEAT, 70, Pair.of(MountStat.ACCELERATION, 8), Pair.of(MountStat.ENERGY, 22)),

    DIAMOND_INGOT(ResourceType.INGOT, 80, Pair.of(MountStat.ENERGY, 9), Pair.of(MountStat.TOUGHNESS, 24)),
    DIAMOND_GEM(ResourceType.GEM, 80, Pair.of(MountStat.SPEED, 10), Pair.of(MountStat.ENERGY, 4), Pair.of(MountStat.TRAINING, 18)),
    PINE_PLANK(ResourceType.PLANK, 80, Pair.of(MountStat.SPEED, 4), Pair.of(MountStat.ACCELERATION, 18), Pair.of(MountStat.TOUGHNESS, 10)),
    PINE_PAPER(ResourceType.PAPER, 80, Pair.of(MountStat.ALTITUDE, 24), Pair.of(MountStat.BOOST, 9)),
    RICE_STRING(ResourceType.STRING, 80, Pair.of(MountStat.ACCELERATION, 4), Pair.of(MountStat.HANDLING, 10), Pair.of(MountStat.BOOST, 18)),
    RICE_GRAINS(ResourceType.GRAINS, 80, Pair.of(MountStat.SPEED, 24), Pair.of(MountStat.ALTITUDE, 9)),
    BASS_OIL(ResourceType.OIL, 80, Pair.of(MountStat.ALTITUDE, 4), Pair.of(MountStat.HANDLING, 18), Pair.of(MountStat.TRAINING, 10)),
    BASS_MEAT(ResourceType.MEAT, 80, Pair.of(MountStat.ACCELERATION, 9), Pair.of(MountStat.ENERGY, 24)),

    MOLTEN_INGOT(ResourceType.INGOT, 90, Pair.of(MountStat.ENERGY, 9), Pair.of(MountStat.TOUGHNESS, 26)),
    MOLTEN_GEM(ResourceType.GEM, 90, Pair.of(MountStat.SPEED, 11), Pair.of(MountStat.ENERGY, 5), Pair.of(MountStat.TRAINING, 20)),
    AVO_PLANK(ResourceType.PLANK, 90, Pair.of(MountStat.SPEED, 5), Pair.of(MountStat.ACCELERATION, 20), Pair.of(MountStat.TOUGHNESS, 11)),
    AVO_PAPER(ResourceType.PAPER, 90, Pair.of(MountStat.ALTITUDE, 26), Pair.of(MountStat.BOOST, 9)),
    SORGHUM_STRING(ResourceType.STRING, 90, Pair.of(MountStat.ACCELERATION, 5), Pair.of(MountStat.HANDLING, 11), Pair.of(MountStat.BOOST, 20)),
    SORGHUM_GRAINS(ResourceType.GRAINS, 90, Pair.of(MountStat.SPEED, 26), Pair.of(MountStat.ALTITUDE, 9)),
    MOLTEN_OIL(ResourceType.OIL, 90, Pair.of(MountStat.ALTITUDE, 5), Pair.of(MountStat.HANDLING, 20), Pair.of(MountStat.TRAINING, 11)),
    MOLTEN_MEAT(ResourceType.MEAT, 90, Pair.of(MountStat.ACCELERATION, 9), Pair.of(MountStat.ENERGY, 26)),

    VOIDSTONE_INGOT(ResourceType.INGOT, 100, Pair.of(MountStat.ENERGY, 10), Pair.of(MountStat.TOUGHNESS, 28)),
    VOIDSTONE_GEM(ResourceType.GEM, 100, Pair.of(MountStat.SPEED, 12), Pair.of(MountStat.ENERGY, 5), Pair.of(MountStat.TRAINING, 21)),
    SKY_PLANK(ResourceType.PLANK, 100, Pair.of(MountStat.SPEED, 5), Pair.of(MountStat.ACCELERATION, 21), Pair.of(MountStat.TOUGHNESS, 12)),
    SKY_PAPER(ResourceType.PAPER, 100, Pair.of(MountStat.ALTITUDE, 28), Pair.of(MountStat.BOOST, 10)),
    HEMP_STRING(ResourceType.STRING, 100, Pair.of(MountStat.ACCELERATION, 5), Pair.of(MountStat.HANDLING, 12), Pair.of(MountStat.BOOST, 21)),
    HEMP_GRAINS(ResourceType.GRAINS, 100, Pair.of(MountStat.SPEED, 28), Pair.of(MountStat.ALTITUDE, 10)),
    STARFISH_OIL(ResourceType.OIL, 100, Pair.of(MountStat.ALTITUDE, 5), Pair.of(MountStat.HANDLING, 21), Pair.of(MountStat.TRAINING, 12)),
    STARFISH_MEAT(ResourceType.MEAT, 100, Pair.of(MountStat.ACCELERATION, 10), Pair.of(MountStat.ENERGY, 28)),

    DERNIC_INGOT(ResourceType.INGOT, 105, Pair.of(MountStat.ENERGY, 10), Pair.of(MountStat.TOUGHNESS, 29)),
    DERNIC_GEM(ResourceType.GEM, 105, Pair.of(MountStat.SPEED, 12), Pair.of(MountStat.ENERGY, 5), Pair.of(MountStat.TRAINING, 22)),
    DERNIC_PLANK(ResourceType.PLANK, 105, Pair.of(MountStat.SPEED, 5), Pair.of(MountStat.ACCELERATION, 22), Pair.of(MountStat.TOUGHNESS, 12)),
    DERNIC_PAPER(ResourceType.PAPER, 105, Pair.of(MountStat.ALTITUDE, 29), Pair.of(MountStat.BOOST, 10)),
    DERNIC_STRING(ResourceType.STRING, 105, Pair.of(MountStat.ACCELERATION, 5), Pair.of(MountStat.HANDLING, 12), Pair.of(MountStat.BOOST, 22)),
    DERNIC_GRAINS(ResourceType.GRAINS, 105, Pair.of(MountStat.SPEED, 29), Pair.of(MountStat.ALTITUDE, 10)),
    DERNIC_OIL(ResourceType.OIL, 105, Pair.of(MountStat.ALTITUDE, 5), Pair.of(MountStat.HANDLING, 22), Pair.of(MountStat.TRAINING, 12)),
    DERNIC_MEAT(ResourceType.MEAT, 105, Pair.of(MountStat.ACCELERATION, 10), Pair.of(MountStat.ENERGY, 29)),

    TITANIUM_INGOT(ResourceType.INGOT, 110, Pair.of(MountStat.ENERGY, 11), Pair.of(MountStat.TOUGHNESS, 30)),
    TITANIUM_GEM(ResourceType.GEM, 110, Pair.of(MountStat.SPEED, 13), Pair.of(MountStat.ENERGY, 5), Pair.of(MountStat.TRAINING, 23)),
    MAPLE_PLANK(ResourceType.PLANK, 110, Pair.of(MountStat.SPEED, 5), Pair.of(MountStat.ACCELERATION, 23), Pair.of(MountStat.TOUGHNESS, 13)),
    MAPLE_PAPER(ResourceType.PAPER, 110, Pair.of(MountStat.ALTITUDE, 30), Pair.of(MountStat.BOOST, 11)),
    JUTE_STRING(ResourceType.STRING, 110, Pair.of(MountStat.ACCELERATION, 5), Pair.of(MountStat.HANDLING, 13), Pair.of(MountStat.BOOST, 23)),
    JUTE_GRAINS(ResourceType.GRAINS, 110, Pair.of(MountStat.SPEED, 30), Pair.of(MountStat.ALTITUDE, 11)),
    STURGEON_OIL(ResourceType.OIL, 110, Pair.of(MountStat.ALTITUDE, 5), Pair.of(MountStat.HANDLING, 23), Pair.of(MountStat.TRAINING, 13)),
    STURGEON_MEAT(ResourceType.MEAT, 110, Pair.of(MountStat.ACCELERATION, 11), Pair.of(MountStat.ENERGY, 30)),

    CINNABAR_INGOT(ResourceType.INGOT, 115, Pair.of(MountStat.ENERGY, 11), Pair.of(MountStat.TOUGHNESS, 31)),
    CINNABAR_GEM(ResourceType.GEM, 115, Pair.of(MountStat.SPEED, 13), Pair.of(MountStat.ENERGY, 5), Pair.of(MountStat.TRAINING, 23)),
    REDWOOD_PLANK(ResourceType.PLANK, 115, Pair.of(MountStat.SPEED, 5), Pair.of(MountStat.ACCELERATION, 23), Pair.of(MountStat.TOUGHNESS, 13)),
    REDWOOD_PAPER(ResourceType.PAPER, 115, Pair.of(MountStat.ALTITUDE, 31), Pair.of(MountStat.BOOST, 11)),
    HEATHER_STRING(ResourceType.STRING, 115, Pair.of(MountStat.ACCELERATION, 5), Pair.of(MountStat.HANDLING, 13), Pair.of(MountStat.BOOST, 23)),
    HEATHER_GRAINS(ResourceType.GRAINS, 115, Pair.of(MountStat.SPEED, 31), Pair.of(MountStat.ALTITUDE, 11)),
    MAHSEER_OIL(ResourceType.OIL, 115, Pair.of(MountStat.ALTITUDE, 5), Pair.of(MountStat.HANDLING, 23), Pair.of(MountStat.TRAINING, 13)),
    MAHSEER_MEAT(ResourceType.MEAT, 115, Pair.of(MountStat.ACCELERATION, 11), Pair.of(MountStat.ENERGY, 31));

    private final ResourceType resourceType;
    private final int level;
    private final Pair<MountStat, Integer> firstMountStat;
    private final Pair<MountStat, Integer> secondMountStat;
    private final Pair<MountStat, Integer> thirdMountStat;

    MountFood(ResourceType resourceType, int level, Pair<MountStat, Integer> firstMountStat, Pair<MountStat, Integer> secondMountStat)
    {
        this(resourceType, level, firstMountStat, secondMountStat, null);
    }

    MountFood(ResourceType resourceType, int level, Pair<MountStat, Integer> firstMountStat, Pair<MountStat, Integer> secondMountStat, Pair<MountStat, Integer> thirdMountStat)
    {
        this.resourceType = resourceType;
        this.level = level;
        this.firstMountStat = firstMountStat;
        this.secondMountStat = secondMountStat;
        this.thirdMountStat = thirdMountStat;
    }

    public ResourceType getResourceType()
    {
        return resourceType;
    }

    public int getLevel()
    {
        return level;
    }

    public Pair<MountStat, Integer> getFirstMountStat()
    {
        return firstMountStat;
    }

    public Pair<MountStat, Integer> getSecondMountStat()
    {
        return secondMountStat;
    }

    public Pair<MountStat, Integer> getThirdMountStat()
    {
        return thirdMountStat;
    }

    public static Optional<MountFood> fromMaterialItem(MaterialItem materialItem)
    {
        Optional<ResourceType> resourceTypeOpt = Models.Profession.getResourceTypeFromMaterialInfo(materialItem.getMaterialInfo());
        if (resourceTypeOpt.isPresent())
        {
            for (MountFood mountFood : values())
            {
                if (mountFood.resourceType == resourceTypeOpt.get() && mountFood.level == materialItem.getLevel())
                    return Optional.of(mountFood);
            }
        }
        return Optional.empty();
    }
}