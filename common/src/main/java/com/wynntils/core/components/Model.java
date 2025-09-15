/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import java.util.List;

/**
 * Models are our representation of the Wynncraft world, game state and Wynncraft
 * metadata. In general, any non-trivial interesting aspect of Wynncraft that is
 * provided through vanilla Minecraft elements, should be parsed and provided by
 * a Model.
 *
 * Models are created as singletons in the {@link Models} holding class.
 */
public abstract class Model extends CoreComponent {
    protected Model(List<Model> dependencies) {
        // dependencies are technically not used, but only required
        // as a reminder for implementers to be wary about dependencies

        // A model is responsible for never accessing another model except
        // those listed in the dependencies, due to bootstrapping ordering
    }

    @Override
    public String getTypeName() {
        return "Model";
    }
}
