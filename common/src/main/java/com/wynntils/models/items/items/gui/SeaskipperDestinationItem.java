/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.models.items.properties.TargetedItemProperty;

public class SeaskipperDestinationItem extends GuiItem implements TargetedItemProperty, CountedItemProperty {
    private final String destination;
    private final int price;
    private final String shorthand;
    private final boolean available;

    public SeaskipperDestinationItem(String destination, int price, String shorthand, boolean available) {
        this.destination = destination;
        this.price = price;
        this.shorthand = shorthand;
        this.available = available;
    }

    public String getDestination() {
        return destination;
    }

    public int getPrice() {
        return price;
    }

    public String getShorthand() {
        return shorthand;
    }

    public boolean isAvailable() {
        return available;
    }

    @Override
    public int getCount() {
        return price;
    }

    @Override
    public String getTarget() {
        return shorthand;
    }

    @Override
    public String toString() {
        return "SeaskipperDestinationItem{" + "destination='"
                + destination + '\'' + ", price="
                + price + ", shorthand='"
                + shorthand + '\'' + '}';
    }
}
