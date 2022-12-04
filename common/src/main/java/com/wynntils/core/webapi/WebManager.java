/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.google.gson.Gson;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.webapi.account.WynntilsAccount;
import com.wynntils.wynn.netresources.ItemProfilesManager;
import com.wynntils.wynn.netresources.SplashManager;

/** Provides and loads web content on demand */
public final class WebManager extends CoreManager {
    public static void init() {
        ApiUrls.tryReloadApiUrls();
        WynntilsAccount.setupUserAccount();

        SplashManager.init();

        ItemProfilesManager.loadCommonObjects();
    }

    public static void reset() {
        ApiUrls.reset();

        ItemProfilesManager.reset();
    }
}
