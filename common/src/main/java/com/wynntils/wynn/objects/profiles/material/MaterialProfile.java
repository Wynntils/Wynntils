/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.material;

import com.wynntils.wynn.objects.profiles.ingredient.ProfessionType;
import java.util.List;
import java.util.Map;

public class MaterialProfile {
    public static final Map<MaterialType, List<LeveledMaterial>> LEVELED_MATERIALS = Map.of(
            MaterialType.ORE,
            List.of(
                    new LeveledMaterial("Copper", 1),
                    new LeveledMaterial("Granite", 10),
                    new LeveledMaterial("Gold", 20),
                    new LeveledMaterial("Sandstone", 30),
                    new LeveledMaterial("Iron", 40),
                    new LeveledMaterial("Silver", 50),
                    new LeveledMaterial("Cobalt", 60),
                    new LeveledMaterial("Kanderstone", 70),
                    new LeveledMaterial("Diamond", 80),
                    new LeveledMaterial("Molten", 90),
                    new LeveledMaterial("Voidstone", 100),
                    new LeveledMaterial("Dernic", 110)),
            MaterialType.LOG,
            List.of(
                    new LeveledMaterial("Oak", 1),
                    new LeveledMaterial("Birch", 10),
                    new LeveledMaterial("Willow", 20),
                    new LeveledMaterial("Acacia", 30),
                    new LeveledMaterial("Spruce", 40),
                    new LeveledMaterial("Jungle", 50),
                    new LeveledMaterial("Dark", 60),
                    new LeveledMaterial("Light", 70),
                    new LeveledMaterial("Pine", 80),
                    new LeveledMaterial("Avo", 90),
                    new LeveledMaterial("Sky", 100),
                    new LeveledMaterial("Dernic", 110)),
            MaterialType.CROP,
            List.of(
                    new LeveledMaterial("Wheat", 1),
                    new LeveledMaterial("Barley", 10),
                    new LeveledMaterial("Oat", 20),
                    new LeveledMaterial("Malt", 30),
                    new LeveledMaterial("Hops", 40),
                    new LeveledMaterial("Rye", 50),
                    new LeveledMaterial("Millet", 60),
                    new LeveledMaterial("Decay_Roots", 70),
                    new LeveledMaterial("Rice", 80),
                    new LeveledMaterial("Sorghum", 90),
                    new LeveledMaterial("Hemp", 100),
                    new LeveledMaterial("Dernic_Seed", 110)),
            MaterialType.FISH,
            List.of(
                    new LeveledMaterial("Gudgeon", 1),
                    new LeveledMaterial("Trout", 10),
                    new LeveledMaterial("Salmon", 20),
                    new LeveledMaterial("Carp", 30),
                    new LeveledMaterial("Icefish", 40),
                    new LeveledMaterial("Piranha", 50),
                    new LeveledMaterial("Koi", 60),
                    new LeveledMaterial("Gylia_Fish", 70),
                    new LeveledMaterial("Bass", 80),
                    new LeveledMaterial("Molten_Eel", 90),
                    new LeveledMaterial("Starfish", 100),
                    new LeveledMaterial("Dernic_Fish", 110)));

    private final ResourceType resourceType;
    private final LeveledMaterial leveledMaterial;
    private final int tier;

    public MaterialProfile(ResourceType resourceType, LeveledMaterial leveledMaterial, int tier) {
        this.resourceType = resourceType;
        this.leveledMaterial = leveledMaterial;
        this.tier = tier;
    }

    public static MaterialProfile fromCodedString(String name) {
        // FIXME
        // §fWheat String§6 [§e✫§8✫✫§6]
        // §fSalmon Oil§6 [§e✫§8✫✫§6]
        return null;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public LeveledMaterial getLeveledMaterial() {
        return leveledMaterial;
    }

    public int getTier() {
        return tier;
    }

    public enum MaterialType {
        ORE(ProfessionType.MINING),
        LOG(ProfessionType.WOODCUTTING),
        CROP(ProfessionType.FARMING),
        FISH(ProfessionType.FISHING);

        private final ProfessionType professionType;

        MaterialType(ProfessionType professionType) {
            this.professionType = professionType;
        }
    }

    public enum ResourceType {
        INGOT(MaterialType.ORE),
        GEM(MaterialType.ORE),
        WOOD(MaterialType.LOG),
        PAPER(MaterialType.LOG),
        STRING(MaterialType.CROP),
        GRAIN(MaterialType.CROP),
        OIL(MaterialType.FISH),
        MEAT(MaterialType.FISH);

        private final MaterialType materialType;

        ResourceType(MaterialType materialType) {
            this.materialType = materialType;
        }
    }

    public record LeveledMaterial(String name, int level) {}
}
