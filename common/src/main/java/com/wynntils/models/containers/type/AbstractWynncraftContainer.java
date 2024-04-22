/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;

public abstract class AbstractWynncraftContainer implements ContainerProperty {
    private final Predicate<Screen> screenPredicate;

    private int containerId;

    protected AbstractWynncraftContainer(Pattern titlePattern) {
        this.screenPredicate =
                screen -> titlePattern.matcher(screen.getTitle().getString()).matches();
    }

    @Override
    public void setContainerId(int containerId) {
        this.containerId = containerId;
    }

    @Override
    public int getContainerId() {
        return containerId;
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screenPredicate.test(screen);
    }
}
