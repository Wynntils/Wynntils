/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.personal;

import com.wynntils.models.containers.type.HighlightableProfessionProperty;
import com.wynntils.models.containers.type.PersonalStorageType;
import java.util.List;
import java.util.regex.Pattern;

public class MiscBucketContainer extends PersonalStorageContainer implements HighlightableProfessionProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFF0\uE00F\uDAFF\uDF68\uF004");

    public MiscBucketContainer() {
        super(TITLE_PATTERN, PersonalStorageType.MISC_BUCKET, 10, List.of(1, 3, 5, 7, 9));
    }
}
