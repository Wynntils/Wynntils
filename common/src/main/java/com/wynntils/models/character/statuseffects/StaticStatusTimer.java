/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.statuseffects;

public class StaticStatusTimer extends StatusTimer {

    private String displayedTime; // The displayed time remaining. Allows for xx:xx for infinite time effects.
    private String prefix; // The prefix to display before the name. Not included in identifying name.

    private final String fullName;

    public StaticStatusTimer(String prefix, String name, String displayedTime) {
        super(name);
        this.prefix = prefix;
        this.displayedTime = displayedTime;
        this.fullName = getPrefix() + " " + getName() + " " + getDisplayedTime();
    }

    /**
     * @return The time remaining for the consumable
     */
    public String getDisplayedTime() {
        return displayedTime;
    }

    /**
     * @param displayedTime The new time remaining for the consumable
     */
    public void setDisplayedTime(String displayedTime) {
        this.displayedTime = displayedTime;
    }

    /**
     * @return The prefix to display before the name
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix The new prefix to display before the name
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String asString() {
        return fullName;
    }
}
