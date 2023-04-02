/*
 * Copyright © Wynntils 2018-2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.translation;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.utils.TaskUtils;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TranslationManager extends Manager {
    private TranslationService translator = null;

    public TranslationManager() {
        super(List.of());

        CachingTranslationService.loadTranslationCache();
    }

    @Override
    public void reloadData() {
        CachingTranslationService.loadTranslationCache();
    }

    public void shutdown() {
        CachingTranslationService.saveTranslationCache();
    }

    /**
     * Get a TranslationService.
     *
     * @param service An enum describing which translation service is requested.
     * @return An instance of the selected translation service, or null on failure
     */
    private TranslationService getService(TranslationServices service) {
        try {
            Constructor<? extends TranslationService> ctor = service.serviceClass.getConstructor();
            return ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            WynntilsMod.error("Error when trying to get translation service.", e);
        }

        return null;
    }

    /**
     * Get the default TranslationService for the language specified by the user in the settings.
     *
     * @return An instance of the selected translation service, or null on failure
     */
    public TranslationService getTranslator(TranslationManager.TranslationServices translationService) {
        // These might not have been created yet, or reset by config changing
        if (translator == null) {
            translator = getService(translationService);
        }
        return translator;
    }

    /**
     * Reset the default TranslatorService, e.g. due to config changes.
     */
    public void resetTranslator() {
        translator = null;
    }

    public enum TranslationServices {
        GOOGLEAPI(GoogleApiTranslationService.class),
        PIGLATIN(PigLatinTranslationService.class);

        private final Class<? extends TranslationService> serviceClass;

        TranslationServices(Class<? extends TranslationService> serviceClass) {
            this.serviceClass = serviceClass;
        }
    }

    /**
     * A demo "translation" service that ignores the selected language, and always translates
     * to "pig latin". Use for test purposes, or for hours of enjoyment for the simple-minded. ;-)
     */
    public static class PigLatinTranslationService implements TranslationService {
        @Override
        public void translate(List<String> messageList, String toLanguage, Consumer<List<String>> handleTranslation) {
            List<String> resultList = new ArrayList<>();
            for (String message : messageList) {
                StringBuilder latinString = new StringBuilder();
                if (!message.isEmpty()) {
                    for (String word : message.split("\\s")) {
                        if (word.isEmpty()) continue;
                        if ("AEIOUaeiou".indexOf(word.charAt(0)) != -1) {
                            latinString.append(word).append("ay ");
                        } else {
                            latinString
                                    .append(word.substring(1))
                                    .append(word.charAt(0))
                                    .append("ay ");
                        }
                    }
                }
                resultList.add(latinString.toString());
            }
            TaskUtils.runAsync(() -> handleTranslation.accept(resultList));
        }
    }
}
