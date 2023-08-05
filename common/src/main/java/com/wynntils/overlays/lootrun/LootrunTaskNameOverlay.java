/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.lootrun;

import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class LootrunTaskNameOverlay extends TextOverlay {
    private static final String TEMPLATE =
            "{IF_STRING(STRING_EQUALS(lootrun_state; \"NOT_RUNNING\"); \"\"; CONCAT(IF_STRING(STRING_EQUALS(lootrun_task_name(\"YELLOW\"); \"\");\"\";CONCAT(\"§eYellow: \"; CONCAT(lootrun_task_name(\"YELLOW\");\"\\n\")));IF_STRING(STRING_EQUALS(lootrun_task_name(\"BLUE\"); \"\");\"\";CONCAT(\"§9Blue: \"; CONCAT(lootrun_task_name(\"BLUE\");\"\\n\")));IF_STRING(STRING_EQUALS(lootrun_task_name(\"PURPLE\"); \"\");\"\";CONCAT(\"§5Purple: \"; CONCAT(lootrun_task_name(\"PURPLE\");\"\\n\")));IF_STRING(STRING_EQUALS(lootrun_task_name(\"GRAY\"); \"\");\"\";CONCAT(\"§7Gray: \"; CONCAT(lootrun_task_name(\"GRAY\");\"\\n\")));IF_STRING(STRING_EQUALS(lootrun_task_name(\"ORANGE\"); \"\");\"\";CONCAT(\"§6Orange: \"; CONCAT(lootrun_task_name(\"ORANGE\");\"\\n\")));IF_STRING(STRING_EQUALS(lootrun_task_name(\"AQUA\"); \"\");\"\";CONCAT(\"§bAqua: \"; CONCAT(lootrun_task_name(\"AQUA\");\"\\n\")));IF_STRING(STRING_EQUALS(lootrun_task_name(\"DARK_GRAY\"); \"\");\"\";CONCAT(\"§8Dark Gray: \"; CONCAT(lootrun_task_name(\"DARK_GRAY\");\"\\n\")));IF_STRING(STRING_EQUALS(lootrun_task_name(\"GREEN\"); \"\");\"\";CONCAT(\"§aGreen: \"; CONCAT(lootrun_task_name(\"GREEN\");\"\\n\")));IF_STRING(STRING_EQUALS(lootrun_task_name(\"RED\"); \"\");\"\";CONCAT(\"§cRed: \"; CONCAT(lootrun_task_name(\"RED\");\"\\n\")));IF_STRING(STRING_EQUALS(lootrun_task_name(\"WHITE\"); \"\");\"\";CONCAT(\"§fWhite: \"; CONCAT(lootrun_task_name(\"WHITE\");\"\\n\")));IF_STRING(STRING_EQUALS(lootrun_task_name(\"RAINBOW\"); \"\");\"\";CONCAT(\"§4R§ca§6i§en§ab§2o§bw§9: §f\"; lootrun_task_name(\"RAINBOW\")))))}";

    private static final String PREVIEW_TEMPLATE =
            """
            §eYellow: §fYellow Task name
            §9Blue: §fBlue Task name
            """;

    public LootrunTaskNameOverlay() {
        super(
                new OverlayPosition(
                        5, 0, VerticalAlignment.TOP, HorizontalAlignment.LEFT, OverlayPosition.AnchorSection.TOP_RIGHT),
                200,
                100);
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected String getPreviewTemplate() {
        return PREVIEW_TEMPLATE;
    }
}
