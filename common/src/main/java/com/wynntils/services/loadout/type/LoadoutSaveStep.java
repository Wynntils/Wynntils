/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.loadout.type;

import java.util.function.Consumer;

@FunctionalInterface
public interface LoadoutSaveStep {
    void run(Consumer<String> onStatus, Consumer<String> onError, Consumer<String> onComplete);
}
