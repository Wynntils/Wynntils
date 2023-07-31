/*
 * Copyright © Wynntils 2018-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.translation;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.services.translation.type.TranslationProvider;
import com.wynntils.utils.TaskUtils;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TranslationService extends Service {
    private TranslationProvider translator = null;

    public TranslationService() {
        super(List.of());

        CachingTranslationProvider.loadTranslationCache();
    }

    @Override
    public void reloadData() {
        CachingTranslationProvider.loadTranslationCache();
    }

    public void shutdown() {
        CachingTranslationProvider.saveTranslationCache();
    }

    /**
     * Get a TranslationService.
     *
     * @param service An enum describing which translation service is requested.
     * @return An instance of the selected translation service, or null on failure
     */
    private TranslationProvider getService(TranslationServices service) {
        try {
            Constructor<? extends TranslationProvider> ctor = service.serviceClass.getConstructor();
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
    public TranslationProvider getTranslator(TranslationService.TranslationServices translationService) {
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
        GOOGLEAPI(GoogleApiTranslationProvider.class),
        PIGLATIN(PigLatinTranslationProvider.class);

        private final Class<? extends TranslationProvider> serviceClass;

        TranslationServices(Class<? extends TranslationProvider> serviceClass) {
            this.serviceClass = serviceClass;
        }
    }

    /**
     * A demo "translation" service that ignores the selected language, and always translates
     * to "pig latin". Use for test purposes, or for hours of enjoyment for the simple-minded. ;-)
     */
    public static class PigLatinTranslationProvider implements TranslationProvider {
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
