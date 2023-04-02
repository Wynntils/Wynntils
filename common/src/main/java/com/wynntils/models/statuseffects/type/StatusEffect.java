/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.statuseffects.type;

public class StatusEffect {
    private final String fullName;
    private final String name; // The name of the consumable (also used to identify it)
    private String displayedTime; // The displayed time remaining. Allows for xx:xx for infinite time effects.
    private String prefix; // The prefix to display before the name. Not included in identifying name.

    public StatusEffect(String name, String displayedTime, String prefix) {
        this.name = name;
        this.displayedTime = displayedTime;
        this.prefix = prefix;
        // Don't add extra spaces
        // Sometimes, " §7" will be parsed as part of the prefix, in an attempt to keep the name field
        // as a proper unformatted string
        // The two differing examples I have for this are Archer's Windy Feet and Warrior's Boiling Blood cooldown
        if (getPrefix().endsWith(" ") || getPrefix().endsWith(" §7")) {
            this.fullName = getPrefix() + getName() + " " + getDisplayedTime();
        } else {
            this.fullName = getPrefix() + " " + getName() + " " + getDisplayedTime();
        }
    }

    /**
     * @return The name of the consumable
     */
    public String getName() {
        return name;
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

    public String asString() {
        return fullName;
    }
}
