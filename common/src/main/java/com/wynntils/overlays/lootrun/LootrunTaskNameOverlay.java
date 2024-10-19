/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.lootrun;

import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class LootrunTaskNameOverlay extends TextOverlay {
    @Persisted
    public final Config<Boolean> forceCoordinates = new Config<>(false);

    private static final String TEMPLATE =
            "{IF_STRING(STRING_EQUALS(lootrun_state; \"NOT_RUNNING\"); \"\"; CONCAT(IF_STRING(STRING_EQUALS(lootrun_task_name(\"YELLOW\"); \"\");\"\";CONCAT(\"§eYellow: \"; lootrun_task_type(\"YELLOW\");\" at \";lootrun_task_name(\"YELLOW\");\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"BLUE\"); \"\");\"\";CONCAT(\"§9Blue: \"; lootrun_task_type(\"BLUE\");\" at \";lootrun_task_name(\"BLUE\");\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"PURPLE\"); \"\");\"\";CONCAT(\"§5Purple: \"; lootrun_task_type(\"PURPLE\");\" at \";lootrun_task_name(\"PURPLE\");\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"GRAY\"); \"\");\"\";CONCAT(\"§7Gray: \"; lootrun_task_type(\"GRAY\");\" at \";lootrun_task_name(\"GRAY\");\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"ORANGE\"); \"\");\"\";CONCAT(\"§6Orange: \"; lootrun_task_type(\"ORANGE\");\" at \";lootrun_task_name(\"ORANGE\");\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"AQUA\"); \"\");\"\";CONCAT(\"§bAqua: \"; lootrun_task_type(\"AQUA\");\" at \";lootrun_task_name(\"AQUA\");\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"DARK_GRAY\"); \"\");\"\";CONCAT(\"§8Dark Gray: \"; lootrun_task_type(\"DARK_GRAY\");\" at \";lootrun_task_name(\"DARK_GRAY\");\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"GREEN\"); \"\");\"\";CONCAT(\"§aGreen: \"; lootrun_task_type(\"GREEN\");\" at \";lootrun_task_name(\"GREEN\");\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"RED\"); \"\");\"\";CONCAT(\"§cRed: \"; lootrun_task_type(\"RED\");\" at \";lootrun_task_name(\"RED\");\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"WHITE\"); \"\");\"\";CONCAT(\"§fWhite: \"; lootrun_task_type(\"WHITE\");\" at \";lootrun_task_name(\"WHITE\");\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"RAINBOW\"); \"\");\"\";CONCAT(\"§4R§ca§6i§en§ab§2o§bw§9: §f\"; lootrun_task_type(\"RAINBOW\");\" at \";lootrun_task_name(\"RAINBOW\")))))}";

    private static final String TEMPLATE_FORCE_COORDINATES =
            "{IF_STRING(STRING_EQUALS(lootrun_state; \"NOT_RUNNING\"); \"\"; CONCAT(IF_STRING(STRING_EQUALS(lootrun_task_name(\"YELLOW\"); \"\");\"\";CONCAT(\"§eYellow: \"; lootrun_task_type(\"YELLOW\");\" at \";\"[\"; str(x(lootrun_task_location(\"YELLOW\"))); \", \"; str(y(lootrun_task_location(\"YELLOW\"))); \", \"; str(z(lootrun_task_location(\"YELLOW\"))); \"]\";\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"BLUE\"); \"\");\"\";CONCAT(\"§9Blue: \"; lootrun_task_type(\"BLUE\");\" at \";\"[\"; str(x(lootrun_task_location(\"BLUE\"))); \", \"; str(y(lootrun_task_location(\"BLUE\"))); \", \"; str(z(lootrun_task_location(\"BLUE\"))); \"]\";\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"PURPLE\"); \"\");\"\";CONCAT(\"§5Purple: \"; lootrun_task_type(\"PURPLE\");\" at \";\"[\"; str(x(lootrun_task_location(\"PURPLE\"))); \", \"; str(y(lootrun_task_location(\"PURPLE\"))); \", \"; str(z(lootrun_task_location(\"PURPLE\"))); \"]\";\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"GRAY\"); \"\");\"\";CONCAT(\"§7Gray: \"; lootrun_task_type(\"GRAY\");\" at \";\"[\"; str(x(lootrun_task_location(\"GRAY\"))); \", \"; str(y(lootrun_task_location(\"GRAY\"))); \", \"; str(z(lootrun_task_location(\"GRAY\"))); \"]\";\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"ORANGE\"); \"\");\"\";CONCAT(\"§6Orange: \"; lootrun_task_type(\"ORANGE\");\" at \";\"[\"; str(x(lootrun_task_location(\"ORANGE\"))); \", \"; str(y(lootrun_task_location(\"ORANGE\"))); \", \"; str(z(lootrun_task_location(\"ORANGE\"))); \"]\";\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"AQUA\"); \"\");\"\";CONCAT(\"§bAqua: \"; lootrun_task_type(\"AQUA\");\" at \";\"[\"; str(x(lootrun_task_location(\"AQUA\"))); \", \"; str(y(lootrun_task_location(\"AQUA\"))); \", \"; str(z(lootrun_task_location(\"AQUA\"))); \"]\";\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"DARK_GRAY\"); \"\");\"\";CONCAT(\"§8Dark Gray: \"; lootrun_task_type(\"DARK_GRAY\");\" at \";\"[\"; str(x(lootrun_task_location(\"DARK_GRAY\"))); \", \"; str(y(lootrun_task_location(\"DARK_GRAY\"))); \", \"; str(z(lootrun_task_location(\"DARK_GRAY\"))); \"]\";\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"GREEN\"); \"\");\"\";CONCAT(\"§aGreen: \"; lootrun_task_type(\"GREEN\");\" at \";\"[\"; str(x(lootrun_task_location(\"GREEN\"))); \", \"; str(y(lootrun_task_location(\"GREEN\"))); \", \"; str(z(lootrun_task_location(\"GREEN\"))); \"]\";\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"RED\"); \"\");\"\";CONCAT(\"§cRed: \"; lootrun_task_type(\"RED\");\" at \";\"[\"; str(x(lootrun_task_location(\"RED\"))); \", \"; str(y(lootrun_task_location(\"RED\"))); \", \"; str(z(lootrun_task_location(\"RED\"))); \"]\";\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"WHITE\"); \"\");\"\";CONCAT(\"§fWhite: \"; lootrun_task_type(\"WHITE\");\" at \";\"[\"; str(x(lootrun_task_location(\"WHITE\"))); \", \"; str(y(lootrun_task_location(\"WHITE\"))); \", \"; str(z(lootrun_task_location(\"WHITE\"))); \"]\";\"\\n\"));IF_STRING(STRING_EQUALS(lootrun_task_name(\"RAINBOW\"); \"\");\"\";CONCAT(\"§4R§ca§6i§en§ab§2o§bw§9: §f\"; lootrun_task_type(\"RAINBOW\");\" at \";\"[\"; str(x(lootrun_task_location(\"RAINBOW\"))); \", \"; str(y(lootrun_task_location(\"RAINBOW\"))); \", \"; str(z(lootrun_task_location(\"RAINBOW\"))); \"]\"))))}";

    private static final String PREVIEW_TEMPLATE =
            """
            §eYellow: Slay at Yellow Task name
            §9Blue: Defend at Blue Task name
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
        return forceCoordinates.get() ? TEMPLATE_FORCE_COORDINATES : TEMPLATE;
    }

    @Override
    protected String getPreviewTemplate() {
        return PREVIEW_TEMPLATE;
    }
}
