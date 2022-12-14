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
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlManager;
import com.wynntils.core.net.athena.WynntilsAccountManager;
import com.wynntils.mc.MinecraftSchedulerManager;
import com.wynntils.wynn.model.CharacterManager;
import com.wynntils.wynn.model.CharacterSelectionManager;
import com.wynntils.wynn.model.ItemProfilesManager;
import com.wynntils.wynn.model.SplashManager;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.model.container.ContainerQueryManager;
import com.wynntils.wynn.model.discoveries.DiscoveryManager;
import com.wynntils.wynn.model.quests.QuestManager;
import com.wynntils.wynn.model.territory.TerritoryManager;
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
        registerPersistentDependency(NetManager.class);
        registerPersistentDependency(UrlManager.class);
        registerPersistentDependency(ConfigManager.class);
        registerPersistentDependency(CharacterManager.class);
        registerPersistentDependency(CharacterSelectionManager.class);
        registerPersistentDependency(ClientCommandManager.class);
        registerPersistentDependency(ContainerQueryManager.class);
        registerPersistentDependency(DiscoveryManager.class);
        registerPersistentDependency(FunctionManager.class);
        registerPersistentDependency(KeyBindManager.class);
        registerPersistentDependency(MinecraftSchedulerManager.class);
        registerPersistentDependency(OverlayManager.class);
        registerPersistentDependency(QuestManager.class);
        registerPersistentDependency(UpdateManager.class);
        registerPersistentDependency(WynntilsAccountManager.class);
        registerPersistentDependency(ItemProfilesManager.class);
        registerPersistentDependency(SplashManager.class);
        registerPersistentDependency(WorldStateManager.class);
        registerPersistentDependency(TerritoryManager.class);

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
        tryInitManager(manager);
    }

    private static void addDependency(ModelDependant dependant, Class<? extends Model> dependency) {
        if (PERSISTENT_CORE_MANAGERS.contains(dependency)) {
            throw new IllegalStateException("Tried to register a core manager like a model.");
        }

        List<ModelDependant> modelDependencies = MODEL_DEPENDENCIES.get(dependency);

        // first dependency, enable manager
        if (modelDependencies == null) {
            modelDependencies = new ArrayList<>();
            MODEL_DEPENDENCIES.put(dependency, modelDependencies);

            ENABLED_MANAGERS.add(dependency);
            tryInitManager(dependency);
        }

        modelDependencies.add(dependant);
    }

    private static void removeDependency(ModelDependant dependant, Class<? extends Model> dependency) {
        if (PERSISTENT_CORE_MANAGERS.contains(dependency)) {
            throw new IllegalStateException("Tried to unregister a core manager like a model.");
        }

        List<ModelDependant> modelDependencies = MODEL_DEPENDENCIES.get(dependency);

        // Check if present and try to remove
        if (modelDependencies == null || !modelDependencies.remove(dependant)) {
            WynntilsMod.warn(
                    String.format("Could not remove dependency of %s for %s when lacking", dependant, dependency));
            return;
        }

        // Check if should disable manager
        if (modelDependencies.isEmpty()) {
            // remove empty list
            MODEL_DEPENDENCIES.remove(dependency);

            ENABLED_MANAGERS.remove(dependency);
            tryDisableManager(dependency);
        }
    }

    public static void addAllDependencies(ModelDependant dependant) {
        for (Class<? extends Model> dependency : dependant.getModelDependencies()) {
            addDependency(dependant, dependency);
        }
    }

    public static void removeAllDependencies(ModelDependant dependant) {
        for (Class<? extends Model> dependency : dependant.getModelDependencies()) {
            removeDependency(dependant, dependency);
        }
    }

    private static void tryInitManager(Class<? extends Manager> manager) {
        WynntilsMod.registerEventListener(manager);

        try {
            MethodUtils.invokeExactStaticMethod(manager, "init");
        } catch (IllegalAccessException | NoSuchMethodException e) {
            WynntilsMod.error("Misconfigured init() on manager " + manager, e);
            throw new RuntimeException();
        } catch (InvocationTargetException e) {
            WynntilsMod.error("Exception during init of manager " + manager, e.getTargetException());
        }
    }

    private static void tryDisableManager(Class<? extends Manager> manager) {
        WynntilsMod.unregisterEventListener(manager);

        try {
            MethodUtils.invokeExactStaticMethod(manager, "disable");
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            // ignored, it is fine to not have a disable method
        }
    }

    private static void addCrashCallbacks() {
        CrashReportManager.registerCrashContext(new ManagerCrashContext());
    }

    public static boolean isEnabled(Class<? extends Manager> manager) {
        return ENABLED_MANAGERS.contains(manager);
    }

    private static class ManagerCrashContext extends CrashReportManager.ICrashContext {
        public ManagerCrashContext() {
            super("Loaded Managers");
        }

        @Override
        public Object generate() {
            StringBuilder result = new StringBuilder();

            for (Class<? extends Manager> persistentManager : PERSISTENT_CORE_MANAGERS) {
                result.append("\n\t\t").append(persistentManager.getName()).append(": Persistent Manager");
            }

            for (Map.Entry<Class<? extends Model>, List<ModelDependant>> dependencyEntry :
                    MODEL_DEPENDENCIES.entrySet()) {
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
    }
}
