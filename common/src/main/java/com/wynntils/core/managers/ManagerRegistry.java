/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.commands.ClientCommandManager;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.OverlayManager;
import com.wynntils.core.functions.DependantFunction;
import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.FunctionManager;
import com.wynntils.core.keybinds.KeyBindManager;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.wynn.model.CharacterManager;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.model.container.ContainerQueryManager;
import com.wynntils.wynn.model.questbook.QuestBookManager;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.reflect.MethodUtils;

public final class ManagerRegistry {
    private static final List<Class<? extends CoreManager>> PERSISTENT_CORE_MANAGERS = new ArrayList<>();
    private static final Map<Class<? extends Model>, List<Feature>> MODEL_DEPENDENCIES = new HashMap<>();
    private static final Map<Class<? extends Model>, List<Function<?>>> FUNCTION_MODEL_DEPENDENCIES = new HashMap<>();
    private static final Set<Class<? extends Manager>> ENABLED_MANAGERS = new HashSet<>();

    public static void init() {
        // Bootstrapping order is important, take care if reordering
        registerPersistentDependency(ConfigManager.class);
        registerPersistentDependency(CharacterManager.class);
        registerPersistentDependency(ClientCommandManager.class);
        registerPersistentDependency(ContainerQueryManager.class);
        registerPersistentDependency(FunctionManager.class);
        registerPersistentDependency(KeyBindManager.class);
        registerPersistentDependency(OverlayManager.class);
        registerPersistentDependency(QuestBookManager.class);
        registerPersistentDependency(WebManager.class);
        registerPersistentDependency(WorldStateManager.class);

        addCrashCallbacks();
    }

    /**
     * Use this if you want to register a manager that is an essential part of the mod.
     * <p>
     * Usually these managers are linked to the internal workings of the mod and not
     * directly modifying the game itself.
     * <p>
     * Do not use this if you don't know what you are doing. Instead, register the manager as a feature dependency.
     * */
    private static void registerPersistentDependency(Class<? extends CoreManager> manager) {
        PERSISTENT_CORE_MANAGERS.add(manager);
        ENABLED_MANAGERS.add(manager);

        WynntilsMod.registerEventListener(manager);

        tryInitManager(manager);
    }

    public static void addDependency(Feature dependant, Class<? extends Model> dependency) {
        if (PERSISTENT_CORE_MANAGERS.contains(dependency)) {
            throw new IllegalStateException("Tried to register a persistent manager.");
        }

        MODEL_DEPENDENCIES.putIfAbsent(dependency, new ArrayList<>());

        MODEL_DEPENDENCIES.get(dependency).add(dependant);

        updateManagerState(dependency);
    }

    public static void addFunctionDependency(
            DependantFunction<?> dependantFunction, Class<? extends Model> dependency) {
        if (PERSISTENT_CORE_MANAGERS.contains(dependency)) {
            throw new IllegalStateException("Tried to register a persistent manager.");
        }

        FUNCTION_MODEL_DEPENDENCIES.putIfAbsent(dependency, new ArrayList<>());

        FUNCTION_MODEL_DEPENDENCIES.get(dependency).add(dependantFunction);

        updateManagerState(dependency);
    }

    public static void removeDependency(Feature dependant, Class<? extends Model> dependency) {
        if (PERSISTENT_CORE_MANAGERS.contains(dependency)) {
            throw new IllegalStateException("Tried to unregister a persistent manager.");
        }

        MODEL_DEPENDENCIES.putIfAbsent(dependency, new ArrayList<>());

        MODEL_DEPENDENCIES.get(dependency).remove(dependant);

        updateManagerState(dependency);
    }

    public static void removeAllFeatureDependency(Feature dependant) {
        for (Class<? extends Manager> manager : MODEL_DEPENDENCIES.keySet()) {
            boolean removed = MODEL_DEPENDENCIES.get(manager).remove(dependant);

            if (removed) {
                updateManagerState(manager);
            }
        }
    }

    private static void updateManagerState(Class<? extends Manager> manager) {
        List<Feature> dependencies = MODEL_DEPENDENCIES.get(manager);
        List<Function<?>> functionDependencies = FUNCTION_MODEL_DEPENDENCIES.get(manager);

        boolean hasDependencies = (dependencies != null && !dependencies.isEmpty())
                || (functionDependencies != null && !functionDependencies.isEmpty());

        if (ENABLED_MANAGERS.contains(manager)) {
            if (!hasDependencies) {
                WynntilsMod.unregisterEventListener(manager);

                ENABLED_MANAGERS.remove(manager);

                tryDisableManager(manager);
            }
        } else {
            if (hasDependencies) {
                WynntilsMod.registerEventListener(manager);

                ENABLED_MANAGERS.add(manager);

                tryInitManager(manager);
            }
        }
    }

    private static void tryInitManager(Class<? extends Manager> manager) {
        try {
            MethodUtils.invokeExactStaticMethod(manager, "init");
        } catch (IllegalAccessException | NoSuchMethodException e) {
            WynntilsMod.warn("Misconfigured init() on manager " + manager, e);
            assert false;
        } catch (InvocationTargetException e) {
            WynntilsMod.error("Exception during init of manager " + manager, e.getTargetException());
        }
    }

    private static void tryDisableManager(Class<? extends Manager> manager) {
        try {
            MethodUtils.invokeExactStaticMethod(manager, "disable");
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            // ignored, it is fine to not have a disable method
        }
    }

    private static void addCrashCallbacks() {
        CrashReportManager.registerCrashContext(new CrashReportManager.ICrashContext() {
            @Override
            public String name() {
                return "Loaded Managers";
            }

            @Override
            public Object generate() {
                StringBuilder result = new StringBuilder();

                for (Class<? extends Manager> persistentManager : PERSISTENT_CORE_MANAGERS) {
                    result.append("\n\t\t").append(persistentManager.getName()).append(": Persistent Manager");
                }

                for (Map.Entry<Class<? extends Model>, List<Feature>> dependencyEntry : MODEL_DEPENDENCIES.entrySet()) {
                    if (!ENABLED_MANAGERS.contains(dependencyEntry.getKey())) continue;

                    result.append("\n\t\t")
                            .append(dependencyEntry.getKey().getName())
                            .append(": ")
                            .append(String.join(
                                    ", ",
                                    dependencyEntry.getValue().stream()
                                            .map(Feature::getTranslatedName)
                                            .toList()));
                }

                return result.toString();
            }
        });
    }

    public static boolean isEnabled(Class<? extends Manager> manager) {
        return ENABLED_MANAGERS.contains(manager);
    }
}
