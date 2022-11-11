/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.google.gson.reflect.TypeToken;
import com.wynntils.core.chat.tabs.ChatTab;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.TypeOverride;
import com.wynntils.core.features.UserFeature;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChatTabsFeature extends UserFeature {
    public static ChatTabsFeature INSTANCE;

    @Config(visible = false)
    public List<ChatTab> chatTabs = new ArrayList<>();

    @TypeOverride
    public Type chatTabsType = new TypeToken<List<ChatTab>>() {}.getType();
}
