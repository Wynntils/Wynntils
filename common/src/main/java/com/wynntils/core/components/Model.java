/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import java.util.List;

/**
 * Models are like managers that can be dependent upon by features / functions.
 */
public abstract class Model extends CoreComponent {
    protected Model(List<Model> dependencies) {
        // dependencies are technically not used, but only required
        // as a reminder for implementers to be wary about dependencies

        // A model is responsible for never accessing another model except
        // those listed in the dependencies, due to bootstrapping ordering
    }

    @Override
    protected String getComponentType() {
        return "Model";
    }

    public void reloadData() {}
}
