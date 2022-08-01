/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.services.TranslationManager;

public class TranslationFeature extends UserFeature {
    public static TranslationFeature INSTANCE;

    @Config
    public String languageName = "en";

    @Config
    public boolean translateTrackedQuest = true;

    @Config
    public boolean translateNpc = true;

    @Config
    public boolean translateInfo = false;

    @Config
    public boolean translatePlayerChat = false;

    @Config
    public boolean keepOriginal = true;

    @Config
    public boolean removeAccents = false;

    @Config
    public TranslationManager.TranslationServices translationService = TranslationManager.TranslationServices.GOOGLEAPI;
}
