/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.components.CoreComponent;
import java.util.List;
import java.util.Set;

public abstract class Dependency {
    private static final Dependency EMPTY = new Dependency() {
        @Override
        public boolean isSatisfied(List<QueuedDownload> finishedDownloads) {
            return true;
        }
    };

    public abstract boolean isSatisfied(List<QueuedDownload> finishedDownloads);

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
        public boolean isSatisfied(List<QueuedDownload> finishedDownloads) {
            assert finishedDownloads.stream().allMatch(download -> download.status() == QueuedDownload.Status.FINISHED);

            return finishedDownloads.stream()
                    .filter(download -> download.callerComponent() == component)
                    .anyMatch(download -> download.urlId() == urlId);
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
        public boolean isSatisfied(List<QueuedDownload> finishedDownloads) {
            assert finishedDownloads.stream().allMatch(download -> download.status() == QueuedDownload.Status.FINISHED);

            return finishedDownloads.stream()
                    .filter(download -> download.callerComponent() == component)
                    .map(QueuedDownload::urlId)
                    .allMatch(urlIds::contains);
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
        public boolean isSatisfied(List<QueuedDownload> finishedDownloads) {
            assert finishedDownloads.stream().allMatch(download -> download.status() == QueuedDownload.Status.FINISHED);

            return dependencies.stream().allMatch(dependency -> dependency.isSatisfied(finishedDownloads));
        }
    }
}
