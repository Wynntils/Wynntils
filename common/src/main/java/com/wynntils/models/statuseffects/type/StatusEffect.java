/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.statuseffects.type;

import com.wynntils.core.text.StyledText2;

public class StatusEffect {
    private final StyledText2 fullName;
    private final StyledText2 name; // The name of the consumable (also used to identify it)
    private StyledText2 displayedTime; // The displayed time remaining. Allows for xx:xx for infinite time effects.
    private StyledText2 prefix; // The prefix to display before the name. Not included in identifying name.

    public StatusEffect(StyledText2 name, StyledText2 displayedTime, StyledText2 prefix) {
        this.name = name;
        this.displayedTime = displayedTime;
        this.prefix = prefix;
        // Don't add extra spaces
        // Sometimes, " §7" will be parsed as part of the prefix, in an attempt to keep the name field
        // as a proper unformatted string
        // The two differing examples I have for this are Archer's Windy Feet and Warrior's Boiling Blood cooldown
        if (prefix.endsWith(" ") || prefix.endsWith(" §7")) {
            this.fullName = StyledText2.concat(prefix, name, StyledText2.fromString(" "), displayedTime);
        } else {
            this.fullName = StyledText2.concat(
                    prefix, StyledText2.fromString(" "), name, StyledText2.fromString(" "), displayedTime);
        }
    }

    /**
     * @return The name of the consumable
     */
    public StyledText2 getName() {
        return name;
    }

    /**
     * @return The time remaining for the consumable
     */
    public StyledText2 getDisplayedTime() {
        return displayedTime;
    }

    /**
     * @param displayedTime The new time remaining for the consumable
     */
    public void setDisplayedTime(StyledText2 displayedTime) {
        this.displayedTime = displayedTime;
    }

    /**
     * @return The prefix to display before the name
     */
    public StyledText2 getPrefix() {
        return prefix;
    }

    /**
     * @param prefix The new prefix to display before the name
     */
    public void setPrefix(StyledText2 prefix) {
        this.prefix = prefix;
    }

    public StyledText2 asString() {
        return fullName;
    }
}
