/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.HighlightableProfessionProperty;
import com.wynntils.models.profession.type.ProfessionType;
import java.util.List;
import java.util.regex.Pattern;

public class CraftingStationContainer extends Container implements HighlightableProfessionProperty {
    private static final String CRAFTING_STATION_TITLE_PREFIX = "\uDAFF\uDFF8\uE053\uDAFF\uDF80";
    public static final int MATERIAL_SLOT_ONE = 0;
    public static final int MATERIAL_SLOT_TWO = 9;
    public static final int CRAFT_SLOT = 13;
    public static final List<Integer> INGREDIENT_SLOTS = List.of(2, 3, 11, 12, 20, 21);

    private final ProfessionType professionType;

    public CraftingStationContainer(ProfessionType professionType) {
        super(Pattern.compile(CRAFTING_STATION_TITLE_PREFIX + getContainerTitleChar(professionType)));

        this.professionType = professionType;
    }

    private static char getContainerTitleChar(ProfessionType professionType) {
        return switch (professionType) {
            case ALCHEMISM -> '\uF041';
            case ARMOURING -> '\uF042';
            case COOKING -> '\uF043';
            case JEWELING -> '\uF044';
            case SCRIBING -> '\uF045';
            case TAILORING -> '\uF046';
            case WEAPONSMITHING -> '\uF047';
            case WOODWORKING -> '\uF048';
            case null, default -> ' ';
        };
    }

    @Override
    public ContainerBounds getBounds() {
        // Includes both the crafting station and the player inventory
        return new ContainerBounds(0, 0, 6, 8);
    }

    public ProfessionType getProfessionType() {
        return professionType;
    }

    @Override
    public String getContainerName() {
        return professionType.getDisplayName() + "Station";
    }
}
