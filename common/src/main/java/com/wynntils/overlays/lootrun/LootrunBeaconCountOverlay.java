/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.lootrun;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.models.lootrun.type.LootrunningState;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class LootrunBeaconCountOverlay extends TextOverlay {
    @Persisted
    private final Config<OrangeDisplayType> orangeDisplayType = new Config<>(OrangeDisplayType.NEXT_EXPIRE);

    private static final String TEMPLATE_NEXT_EXPIRE =
            """
            {concat(
            "§eYellow: ";string(lootrun_beacon_count("YELLOW"));"
            §9Blue: ";string(lootrun_beacon_count("BLUE"));"
            §5Purple: ";string(lootrun_beacon_count("PURPLE"));"
            §7Gray: ";string(lootrun_beacon_count("GRAY"));"/3";"
            §6Orange: ";string(lootrun_beacon_count("ORANGE"));" (+";string(lootrun_orange_beacon_count);") (";string(lootrun_next_orange_expire);")";"
            §bAqua: ";string(lootrun_beacon_count("AQUA"));"
            §8Dark Gray: ";string(lootrun_beacon_count("DARK_GRAY"));"/1";"
            §aGreen: ";string(lootrun_beacon_count("GREEN"));"
            §cRed: ";string(lootrun_beacon_count("RED"));" (";string(lootrun_red_beacon_challenge_count);")";"
            §fWhite: ";string(lootrun_beacon_count("WHITE"));"/1";"
            §#00f010ffCrimson: ";string(lootrun_beacon_count("CRIMSON"));"/2";"
            §#00f000ffRainbow: ";string(lootrun_beacon_count("RAINBOW"));" (";string(lootrun_rainbow_beacon_count);")")}
            """;

    private static final String TEMPLATE_LIST =
            """
            {concat(
            "§eYellow: ";string(lootrun_beacon_count("YELLOW"));"
            §9Blue: ";string(lootrun_beacon_count("BLUE"));"
            §5Purple: ";string(lootrun_beacon_count("PURPLE"));"
            §7Gray: ";string(lootrun_beacon_count("GRAY"));"/3";"
            §6Orange: ";string(lootrun_beacon_count("ORANGE"));" (+";string(lootrun_orange_beacon_count);")";
            if(gt(lootrun_orange_beacon_count;0);concat(
            " (";lootrun_orange_beacon_index(0);
            if(gt(lootrun_orange_beacon_count;1);concat(",";lootrun_orange_beacon_index(1));"");
            if(gt(lootrun_orange_beacon_count;2);concat(",";lootrun_orange_beacon_index(2));"");
            if(gt(lootrun_orange_beacon_count;3);concat(",";lootrun_orange_beacon_index(3));"");
            if(gt(lootrun_orange_beacon_count;4);concat(",";lootrun_orange_beacon_index(4));"");
            if(gt(lootrun_orange_beacon_count;5);concat(",";lootrun_orange_beacon_index(5));"");
            if(gt(lootrun_orange_beacon_count;6);concat(",";lootrun_orange_beacon_index(6));"");
            if(gt(lootrun_orange_beacon_count;7);concat(",";lootrun_orange_beacon_index(7));"");
            if(gt(lootrun_orange_beacon_count;8);concat(",";lootrun_orange_beacon_index(8));"");
            if(gt(lootrun_orange_beacon_count;9);concat(",";lootrun_orange_beacon_index(9));"");
            ")");"");"
            §bAqua: ";string(lootrun_beacon_count("AQUA"));"
            §8Dark Gray: ";string(lootrun_beacon_count("DARK_GRAY"));"/1";"
            §aGreen: ";string(lootrun_beacon_count("GREEN"));"
            §cRed: ";string(lootrun_beacon_count("RED"));" (";string(lootrun_red_beacon_challenge_count);")";"
            §fWhite: ";string(lootrun_beacon_count("WHITE"));"/1";"
            §#00f010ffCrimson: ";string(lootrun_beacon_count("CRIMSON"));"/2";"
            §#00f000ffRainbow: ";string(lootrun_beacon_count("RAINBOW"));" (";string(lootrun_rainbow_beacon_count);")")}
            """;

    private static final String TEMPLATE_SEPARATED =
            """
            {concat(
            "§eYellow: ";string(lootrun_beacon_count("YELLOW"));"
            §9Blue: ";string(lootrun_beacon_count("BLUE"));"
            §5Purple: ";string(lootrun_beacon_count("PURPLE"));"
            §7Gray: ";string(lootrun_beacon_count("GRAY"));"/3";"
            §6Orange: ";string(lootrun_beacon_count("ORANGE"));" (+";string(lootrun_orange_beacon_count);")";
            if(gt(lootrun_orange_beacon_count;0);concat(" (";lootrun_orange_beacon_index(0);")");"");
            if(gt(lootrun_orange_beacon_count;1);concat(" (";lootrun_orange_beacon_index(1);")");"");
            if(gt(lootrun_orange_beacon_count;2);concat(" (";lootrun_orange_beacon_index(2);")");"");
            if(gt(lootrun_orange_beacon_count;3);concat(" (";lootrun_orange_beacon_index(3);")");"");
            if(gt(lootrun_orange_beacon_count;4);concat(" (";lootrun_orange_beacon_index(4);")");"");
            if(gt(lootrun_orange_beacon_count;5);concat(" (";lootrun_orange_beacon_index(5);")");"");
            if(gt(lootrun_orange_beacon_count;6);concat(" (";lootrun_orange_beacon_index(6);")");"");
            if(gt(lootrun_orange_beacon_count;7);concat(" (";lootrun_orange_beacon_index(7);")");"");
            if(gt(lootrun_orange_beacon_count;8);concat(" (";lootrun_orange_beacon_index(8);")");"");
            if(gt(lootrun_orange_beacon_count;9);concat(" (";lootrun_orange_beacon_index(9);")");""
            );"
            §bAqua: ";string(lootrun_beacon_count("AQUA"));"
            §8Dark Gray: ";string(lootrun_beacon_count("DARK_GRAY"));"/1";"
            §aGreen: ";string(lootrun_beacon_count("GREEN"));"
            §cRed: ";string(lootrun_beacon_count("RED"));" (";string(lootrun_red_beacon_challenge_count);")";"
            §fWhite: ";string(lootrun_beacon_count("WHITE"));"/1";"
            §#00f010ffCrimson: ";string(lootrun_beacon_count("CRIMSON"));"/2";"
            §#00f000ffRainbow: ";string(lootrun_beacon_count("RAINBOW"));" (";string(lootrun_rainbow_beacon_count);")")}
            """;

    private static final String PREVIEW_TEMPLATE_NEXT_EXPIRE =
            """
            §eYellow: 0
            §9Blue: 0
            §5Purple: 0
            §7Gray: 0/3
            §6Orange: 0 (+1) (5)
            §bAqua: 0
            §8Dark Gray: 0/1
            §aGreen: 0
            §cRed: 0 (0)
            §fWhite: 0/1
            §#00f010ffCrimson: 0/2
            §#00f000ffRainbow: 0 (10)
            """;

    private static final String PREVIEW_TEMPLATE_LIST =
            """
            §eYellow: 0
            §9Blue: 0
            §5Purple: 0
            §7Gray: 0/3
            §6Orange: 2 (+2) (5,8)
            §bAqua: 0
            §8Dark Gray: 0/1
            §aGreen: 0
            §cRed: 0 (0)
            §fWhite: 0/1
            §#00f010ffCrimson: 0/2
            §#00f000ffRainbow: 0 (10)
            """;

    private static final String PREVIEW_TEMPLATE_SEPARATED =
            """
            §eYellow: 0
            §9Blue: 0
            §5Purple: 0
            §7Gray: 0/3
            §6Orange: 2 (+2) (5) (8)
            §bAqua: 0
            §8Dark Gray: 0/1
            §aGreen: 0
            §cRed: 0 (0)
            §fWhite: 0/1
            §#00f010ffCrimson: 0/2
            §#00f000ffRainbow: 0 (10)
            """;

    public LootrunBeaconCountOverlay() {
        super(
                new OverlayPosition(
                        120,
                        5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.MIDDLE_LEFT),
                250,
                300);
    }

    @Override
    protected String getTemplate() {
        return switch (orangeDisplayType.get()) {
            case NEXT_EXPIRE -> TEMPLATE_NEXT_EXPIRE;
            case LIST -> TEMPLATE_LIST;
            case SEPARATED -> TEMPLATE_SEPARATED;
        };
    }

    @Override
    protected String getPreviewTemplate() {
        return switch (orangeDisplayType.get()) {
            case NEXT_EXPIRE -> PREVIEW_TEMPLATE_NEXT_EXPIRE;
            case LIST -> PREVIEW_TEMPLATE_LIST;
            case SEPARATED -> PREVIEW_TEMPLATE_SEPARATED;
        };
    }

    @Override
    public boolean isVisible() {
        return Models.Lootrun.getState() != LootrunningState.NOT_RUNNING;
    }

    public enum OrangeDisplayType {
        NEXT_EXPIRE,
        LIST,
        SEPARATED
    }
}
