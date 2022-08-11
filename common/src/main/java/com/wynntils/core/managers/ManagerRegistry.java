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
import com.wynntils.core.keybinds.KeyManager;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.utils.CompassManager;
import com.wynntils.mc.utils.CrashReportManager;
import com.wynntils.wc.ModelManager;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.reflect.MethodUtils;

public class ManagerRegistry {
    private static final List<Class<? extends Manager>> PERSISTENT_MANAGERS = new ArrayList<>();
    private static final Map<Class<? extends Manager>, List<Feature>> MANAGER_DEPENDENCIES = new HashMap<>();
    private static final Set<Class<? extends Manager>> ENABLED_MANAGERS = new HashSet<>();

    public static void init() {
        registerPersistentDependency(CompassManager.class);
        registerPersistentDependency(ConfigManager.class);
        registerPersistentDependency(ClientCommandManager.class);
        registerPersistentDependency(KeyManager.class);
        registerPersistentDependency(ModelManager.class);
        registerPersistentDependency(OverlayManager.class);
        registerPersistentDependency(WebManager.class);

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
    private static void registerPersistentDependency(Class<? extends Manager> manager) {
        PERSISTENT_MANAGERS.add(manager);

        WynntilsMod.getEventBus().register(manager);

        tryInitManager(manager);
    }

    public static void addDependency(Feature dependant, Class<? extends Manager> dependency) {
        if (PERSISTENT_MANAGERS.contains(dependency)) {
            throw new IllegalStateException("Tried to register a persistent manager.");
        }

        MANAGER_DEPENDENCIES.putIfAbsent(dependency, new ArrayList<>());

        MANAGER_DEPENDENCIES.get(dependency).add(dependant);

        updateManagerState(dependency);
    }

    public static void removeDependency(Feature dependant, Class<? extends Manager> dependency) {
        if (PERSISTENT_MANAGERS.contains(dependency)) {
            throw new IllegalStateException("Tried to unregister a persistent manager.");
        }

        MANAGER_DEPENDENCIES.putIfAbsent(dependency, new ArrayList<>());

        MANAGER_DEPENDENCIES.get(dependency).remove(dependant);

        updateManagerState(dependency);
    }

    public static void removeAllFeatureDependency(Feature dependant) {
        for (Class<? extends Manager> manager : MANAGER_DEPENDENCIES.keySet()) {
            boolean removed = MANAGER_DEPENDENCIES.get(manager).remove(dependant);

            if (removed) {
                updateManagerState(manager);
            }
        }
    }

    private static void updateManagerState(Class<? extends Manager> manager) {
        List<Feature> dependencies = MANAGER_DEPENDENCIES.get(manager);

        if (ENABLED_MANAGERS.contains(manager)) {
            if (dependencies == null || dependencies.isEmpty()) {
                disableManager(manager);

                tryDisableManager(manager);
            }
        } else {
            if (dependencies != null && !dependencies.isEmpty()) {
                enableManager(manager);

                tryInitManager(manager);
            }
        }
    }

    private static void enableManager(Class<? extends Manager> manager) {
        WynntilsMod.getEventBus().register(manager);

        ENABLED_MANAGERS.add(manager);
    }

    private static void disableManager(Class<? extends Manager> manager) {
        WynntilsMod.getEventBus().unregister(manager);

        ENABLED_MANAGERS.remove(manager);
    }

    private static void tryInitManager(Class<? extends Manager> manager) {
        try {
            MethodUtils.invokeExactStaticMethod(manager, "init");
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            WynntilsMod.error(e.getMessage());
        }
    }

    private static void tryDisableManager(Class<? extends Manager> manager) {
        try {
            MethodUtils.invokeExactStaticMethod(manager, "disable");
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            WynntilsMod.error(e.getMessage());
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

                for (Class<? extends Manager> persistentManager : PERSISTENT_MANAGERS) {
                    result.append("\n\t\t").append(persistentManager.getName()).append(": Persistent Manager");
                }

                for (Map.Entry<Class<? extends Manager>, List<Feature>> dependencyEntry :
                        MANAGER_DEPENDENCIES.entrySet()) {
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
}
