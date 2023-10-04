/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wrappedscreens.activities;

import com.wynntils.handlers.wrappedscreen.WrappedScreenHolder;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.regex.Pattern;

public class WynntilsQuestBookHolder extends WrappedScreenHolder<WynntilsQuestBookScreen> {
    // Same as ActivityModel, but we can't reference it because it would change the class loading order
    private static final Pattern CONTENT_BOOK_TITLE_PATTERN = Pattern.compile("^§f\uE000\uE072$");

    @Override
    protected Pattern getReplacedScreenTitlePattern() {
        return CONTENT_BOOK_TITLE_PATTERN;
    }

    @Override
    protected WynntilsQuestBookScreen createWrappedScreen(WrappedScreenInfo wrappedScreenInfo) {
        return new WynntilsQuestBookScreen(wrappedScreenInfo, this);
    }

    @Override
    protected void setWrappedScreen(WynntilsQuestBookScreen wrappedScreen) {}

    @Override
    protected void reset() {}

    public static void openScreen() {
        ContainerUtils.openInventory(InventoryUtils.CONTENT_BOOK_SLOT_NUM);
    }
}
