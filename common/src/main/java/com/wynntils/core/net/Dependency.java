/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.components.CoreComponent;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Set;

public abstract class Dependency {
    private static final Dependency EMPTY = new Dependency() {
        @Override
        public List<Pair<CoreComponent, UrlId>> dependencies() {
            return List.of();
        }

        @Override
        public String toString() {
            return "EmptyDependency{}";
        }
    };

    public abstract List<Pair<CoreComponent, UrlId>> dependencies();

    public final boolean dependsOn(CoreComponent component, UrlId urlId) {
        return dependencies().stream().anyMatch(pair -> pair.a() == component && pair.b() == urlId);
    }

    public static Dependency empty() {
        return EMPTY;
    }

    public static Dependency simple(CoreComponent component, UrlId urlId) {
        return new SimpleComponentDataDependency(component, urlId);
    }

    public static Dependency multi(CoreComponent component, Set<UrlId> urlIds) {
        return new SingleComponentMultiDataDependency(component, urlIds);
    }

    public static Dependency complex(Set<Dependency> dependencies) {
        return new ComplexDependency(dependencies);
    }

    /**
     * A simple dependency that checks if a specific component has downloaded a specific file.
     */
    private static final class SimpleComponentDataDependency extends Dependency {
        private final CoreComponent component;
        private final UrlId urlId;

        private SimpleComponentDataDependency(CoreComponent component, UrlId urlId) {
            this.component = component;
            this.urlId = urlId;
        }

        @Override
        public List<Pair<CoreComponent, UrlId>> dependencies() {
            return List.of(Pair.of(component, urlId));
        }

        @Override
        public String toString() {
            return "SimpleComponentDataDependency{" + "component=" + component + ", urlId=" + urlId + '}';
        }
    }

    /**
     * A dependency that checks if a specific component has downloaded a set of files.
     */
    private static final class SingleComponentMultiDataDependency extends Dependency {
        private final CoreComponent component;
        private final Set<UrlId> urlIds;

        private SingleComponentMultiDataDependency(CoreComponent component, Set<UrlId> urlIds) {
            this.component = component;
            this.urlIds = urlIds;
        }

        @Override
        public List<Pair<CoreComponent, UrlId>> dependencies() {
            return urlIds.stream().map(urlId -> Pair.of(component, urlId)).toList();
        }

        @Override
        public String toString() {
            return "SingleComponentMultiDataDependency{" + "component=" + component + ", urlIds=" + urlIds + '}';
        }
    }

    /**
     * A dependency that checks whether a set of other dependencies are satisfied.
     */
    private static final class ComplexDependency extends Dependency {
        private final Set<Dependency> dependencies;

        private ComplexDependency(Set<Dependency> dependencies) {
            this.dependencies = dependencies;
        }

        @Override
        public List<Pair<CoreComponent, UrlId>> dependencies() {
            return dependencies.stream()
                    .flatMap(dependency -> dependency.dependencies().stream())
                    .toList();
        }

        @Override
        public String toString() {
            return "ComplexDependency{" + "dependencies=" + dependencies + '}';
        }
    }
}
