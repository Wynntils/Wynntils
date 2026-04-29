/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class Container {
    private final Predicate<Screen> screenPredicate;
    private final Pattern titlePattern;

    private int containerId;

    protected Container(Pattern titlePattern) {
        this.titlePattern = titlePattern;
        this.screenPredicate =
                screen -> titlePattern.matcher(screen.getTitle().getString()).matches();
    }

    protected Container(Predicate<Screen> screenPredicate) {
        this.titlePattern = null;
        this.screenPredicate = screenPredicate;
    }

    public void setContainerId(int containerId) {
        this.containerId = containerId;
    }

    public int getContainerId() {
        return containerId;
    }

    public boolean isScreen(Screen screen) {
        return screenPredicate.test(screen);
    }

    public boolean matchesTitle(Component title) {
        if (titlePattern != null) {
            return titlePattern.matcher(title.getString()).matches();
        }
        // For custom predicates, fallback to false
        return false;
    }

    public String getContainerName() {
        return this.getClass().getSimpleName().replace("Container", "");
    }
}
