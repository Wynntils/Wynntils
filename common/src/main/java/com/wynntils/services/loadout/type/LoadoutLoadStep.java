package com.wynntils.services.loadout.type;

import java.util.function.Consumer;

@FunctionalInterface
public interface LoadoutLoadStep {
    void run(Consumer<String> onStatus, Consumer<String> onError, Consumer<String> onComplete);
}