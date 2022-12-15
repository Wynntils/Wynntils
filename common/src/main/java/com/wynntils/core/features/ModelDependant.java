/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.managers.Model;
import java.util.List;

public interface ModelDependant {
    List<Model> getModelDependencies();
}
