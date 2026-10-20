/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import java.util.regex.Pattern;

public class MountFeederContainer extends Container {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFED\uE058");
    private static final int[] mountEnclosureMainSlots = {9, 18, 27, 36, 45};
    private static final int[] mountEnclosureSlots = {
            11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42, 47, 48, 49, 50, 51
    };
    private static final int[] mountEnclosureSBSSlots = {16, 17, 25, 26, 34, 35, 43, 44, 52, 53};

    public MountFeederContainer() {
        super(TITLE_PATTERN);
    }

    public static  int[] getMountEnclosureMainSlots()
    {
        return mountEnclosureMainSlots;
    }

    public static  int[] getMountEnclosureSlots()
    {
        return mountEnclosureSlots;
    }

    public static  int[] getMountEnclosureSBSSlots()
    {
        return mountEnclosureSBSSlots;
    }
}