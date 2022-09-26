/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.commands.ClientCommandManager;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.ModelDependant;
import com.wynntils.core.features.Translatable;
import com.wynntils.core.features.overlays.OverlayManager;
import com.wynntils.core.functions.FunctionManager;
import com.wynntils.core.keybinds.KeyBindManager;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.wynn.model.CharacterManager;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.model.container.ContainerQueryManager;
import com.wynntils.wynn.model.questbook.QuestBookManager;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.MethodUtils;

public final class ManagerRegistry {
    private static final List<Class<? extends CoreManager>> PERSISTENT_CORE_MANAGERS = new ArrayList<>();
    private static final Map<Class<? extends Model>, List<ModelDependant>> MODEL_DEPENDENCIES = new HashMap<>();
    private static final Collection<Class<? extends Manager>> ENABLED_MANAGERS = new HashSet<>();

    public static void init() {
        // Bootstrapping order is important, take care if reordering
        registerPersistentManager(ConfigManager.class);
        registerPersistentManager(CharacterManager.class);
        registerPersistentManager(ClientCommandManager.class);
        registerPersistentManager(ContainerQueryManager.class);
        registerPersistentManager(FunctionManager.class);
        registerPersistentManager(KeyBindManager.class);
        registerPersistentManager(OverlayManager.class);
        registerPersistentManager(QuestBookManager.class);
        registerPersistentManager(WebManager.class);
        registerPersistentManager(WorldStateManager.class);

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
    private static void registerPersistentManager(Class<? extends CoreManager> manager) {
        PERSISTENT_CORE_MANAGERS.add(manager);

        WynntilsMod.registerEventListener(manager);

        if (!tryInitManager(manager)) {
            WynntilsMod.error("Failed to initialized peristent manager " + manager.getName());
        }
    }

    public static boolean addDependency(ModelDependant dependant, Class<? extends Model> manager) {
        if (PERSISTENT_CORE_MANAGERS.contains(manager)) {
            throw new IllegalStateException("Tried to register a persistent manager.");
        }

        List<ModelDependant> managerDependencies = MODEL_DEPENDENCIES.computeIfAbsent(manager, (k) -> new ArrayList<>());

        managerDependencies.add(dependant);

        return tryInitManager(manager);
    }

    public static boolean removeDependency(ModelDependant dependant, Class<? extends Model> manager) {
        if (PERSISTENT_CORE_MANAGERS.contains(manager)) {
            throw new IllegalStateException("Tried to unregister a persistent manager.");
        }

        List<ModelDependant> managerDependencies = MODEL_DEPENDENCIES.computeIfAbsent(manager, (k) -> new ArrayList<>());

        managerDependencies.remove(dependant);

        return !managerDependencies.isEmpty() || tryDisableManager(manager);
    }

    public static void removeAllFeatureDependency(ModelDependant dependant) {
        for (Class<? extends Model> model : dependant.getModelDependencies()) {

            List<ModelDependant> managerDependencies = MODEL_DEPENDENCIES.get(model);

            if (managerDependencies != null && managerDependencies.remove(dependant)) {
                tryDisableManager(model);
            }
        }
    }

    private static boolean tryInitManager(Class<? extends Manager> manager) {
        // add if not already present
        if (!ENABLED_MANAGERS.add(manager)) return false;

        WynntilsMod.registerEventListener(manager);

        try {
            MethodUtils.invokeExactStaticMethod(manager, "init");
        } catch (IllegalAccessException | NoSuchMethodException e) {
            WynntilsMod.warn("Misconfigured init() on manager " + manager, e);
            return false;
        } catch (InvocationTargetException e) {
            WynntilsMod.warn("Exception during init of manager " + manager, e.getTargetException());
            return false;
        }

        return true;
    }

    private static boolean tryDisableManager(Class<? extends Model> manager) {
        // remove if not already present
        if (!ENABLED_MANAGERS.remove(manager)) return true;

        WynntilsMod.unregisterEventListener(manager);

        try {
            MethodUtils.invokeExactStaticMethod(manager, "disable");
        } catch (IllegalAccessException | NoSuchMethodException e) {
            // ignored, it is fine to not have a disable method
        }  catch (InvocationTargetException e) {
            WynntilsMod.warn("Exception during init of manager " + manager, e.getTargetException());
            return false;
        }

        return true;
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

                for (Map.Entry<Class<? extends Model>, List<ModelDependant>> dependencyEntry : MODEL_DEPENDENCIES.entrySet()) {
                    if (!ENABLED_MANAGERS.contains(dependencyEntry.getKey())) continue;

                    result.append("\n\t\t")
                            .append(dependencyEntry.getKey().getName())
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
        });
    }

    public static boolean isEnabled(Class<? extends Manager> manager) {
        return ENABLED_MANAGERS.contains(manager);
    }
}
