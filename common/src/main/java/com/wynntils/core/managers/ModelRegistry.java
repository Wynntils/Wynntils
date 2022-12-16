/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.ModelDependant;
import com.wynntils.core.features.Translatable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ModelRegistry {
    private static final Map<Model, List<ModelDependant>> MODEL_DEPENDENCIES = new HashMap<>();
    private static final Collection<Model> ENABLED_MODELS = new HashSet<>();

    public static void init() {
        // When the model registry is in place, we can activate the functions
        Managers.Function.activateAllFunctions();

        addCrashCallbacks();
    }

    private static void addDependency(ModelDependant dependant, Model dependency) {
        List<ModelDependant> modelDependencies = MODEL_DEPENDENCIES.get(dependency);

        // first dependency, enable model
        if (modelDependencies == null) {
            modelDependencies = new ArrayList<>();
            MODEL_DEPENDENCIES.put(dependency, modelDependencies);

            ENABLED_MODELS.add(dependency);
            tryInitModel(dependency);
        }

        modelDependencies.add(dependant);
    }

    private static void removeDependency(ModelDependant dependant, Model dependency) {
        List<ModelDependant> modelDependencies = MODEL_DEPENDENCIES.get(dependency);

        // Check if present and try to remove
        if (modelDependencies == null || !modelDependencies.remove(dependant)) {
            WynntilsMod.warn(
                    String.format("Could not remove dependency of %s for %s when lacking", dependant, dependency));
            return;
        }

        // Check if should disable model
        if (modelDependencies.isEmpty()) {
            // remove empty list
            MODEL_DEPENDENCIES.remove(dependency);

            ENABLED_MODELS.remove(dependency);
            tryDisableModel(dependency);
        }
    }

    public static void addAllDependencies(ModelDependant dependant) {
        for (Model dependency : dependant.getModelDependencies()) {
            addDependency(dependant, dependency);
        }
    }

    public static void removeAllDependencies(ModelDependant dependant) {
        for (Model dependency : dependant.getModelDependencies()) {
            removeDependency(dependant, dependency);
        }
    }

    private static void tryInitModel(Model model) {
        WynntilsMod.registerEventListener(model);
        model.init();
    }

    private static void tryDisableModel(Model model) {
        WynntilsMod.unregisterEventListener(model);
        model.disable();
    }

    public static boolean isEnabled(Class<? extends Model> model) {
        return ENABLED_MODELS.contains(model);
    }

    private static void addCrashCallbacks() {
        Managers.CrashReport.registerCrashContext("Loaded Models", ModelRegistry::crashHandler);
    }

    private static String crashHandler() {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<Model, List<ModelDependant>> dependencyEntry : MODEL_DEPENDENCIES.entrySet()) {
            if (!ENABLED_MODELS.contains(dependencyEntry.getKey())) continue;

            result.append("\n\t\t")
                    .append(dependencyEntry.getKey().getClass().getName())
                    .append(": ")
                    .append(dependencyEntry.getValue().stream()
                            .map(t -> {
                                if (t instanceof Translatable translatable) {
                                    return translatable.getTranslatedName();
                                } else {
                                    return t.toString();
                                }
                            })
                            .collect(Collectors.joining(", ")));
        }

        return result.toString();
    }
}
