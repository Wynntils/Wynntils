/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;

public abstract class Container {
    private final Predicate<Screen> screenPredicate;

    private int containerId;

    protected Container(Pattern titlePattern) {
        this.screenPredicate =
                screen -> titlePattern.matcher(screen.getTitle().getString()).matches();
    }

    protected Container(Predicate<Screen> screenPredicate) {
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

    public String getContainerName() {
        return this.getClass().getSimpleName().replace("Container", "");
    }
}
