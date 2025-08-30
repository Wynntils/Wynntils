/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import java.util.List;

/**
 * Services provide support to consumers for anything that is not specifically
 * related to the Wynncraft model, either since it is an external service
 * (3rd party library, or system/environment interaction), or since it is something
 * that we augment the Wynncraft world with, but the Wynncraft servers has no knowledge
 * about. In other words, this is a "misc" category, where everything consumers need
 * that is neither a manager, handler or manager can be placed.
 *
 * Services are created as singletons in the {@link Services} holding class.
 */
public abstract class Service extends CoreComponent {
    protected Service(List<Service> dependencies) {
        // dependencies are technically not used, but only required
        // as a reminder for implementers to be wary about dependencies

        // A manager is responsible for never accessing another manager except
        // those listed in the dependencies, due to bootstrapping ordering
    }

    @Override
    public String getTypeName() {
        return "Service";
    }
}
