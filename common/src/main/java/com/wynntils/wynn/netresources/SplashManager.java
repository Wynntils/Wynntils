package com.wynntils.wynn.netresources;

import com.wynntils.core.webapi.ApiUrls;
import com.wynntils.core.webapi.account.WynntilsAccount;
import com.wynntils.utils.Utils;
import java.util.List;

public class SplashManager {
    private static String currentSplash = "";

    public static void init() {
        updateCurrentSplash();
    }

    private static void updateCurrentSplash() {
        if (ApiUrls.getApiUrls() == null || ApiUrls.getApiUrls().getList("Splashes") == null) return;

        List<String> splashes = ApiUrls.getApiUrls().getList("Splashes");
        currentSplash = splashes.get(Utils.getRandom().nextInt(splashes.size()));
    }

    public static String getCurrentSplash() {
        return currentSplash;
    }
}
