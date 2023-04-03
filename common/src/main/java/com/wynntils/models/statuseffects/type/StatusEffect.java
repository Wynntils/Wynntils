/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.statuseffects.type;

import com.wynntils.utils.mc.type.StyledText;

public class StatusEffect {
    private final StyledText fullName;
    private final StyledText name; // The name of the consumable (also used to identify it)
    private StyledText displayedTime; // The displayed time remaining. Allows for xx:xx for infinite time effects.
    private StyledText prefix; // The prefix to display before the name. Not included in identifying name.

    public StatusEffect(StyledText name, StyledText displayedTime, StyledText prefix) {
        this.name = name;
        this.displayedTime = displayedTime;
        this.prefix = prefix;
        // Don't add extra spaces
        // Sometimes, " §7" will be parsed as part of the prefix, in an attempt to keep the name field
        // as a proper unformatted string
        // The two differing examples I have for this are Archer's Windy Feet and Warrior's Boiling Blood cooldown
        if (prefix.str().endsWith(" ") || prefix.str().endsWith(" §7")) {
            this.fullName = StyledText.concat(prefix, name, StyledText.of(" "), displayedTime);
        } else {
            this.fullName = StyledText.concat(prefix, StyledText.of(" "), name, StyledText.of(" "), displayedTime);
        }
    }

    /**
     * @return The name of the consumable
     */
    public StyledText getName() {
        return name;
    }

    /**
     * @return The time remaining for the consumable
     */
    public StyledText getDisplayedTime() {
        return displayedTime;
    }

    /**
     * @param displayedTime The new time remaining for the consumable
     */
    public void setDisplayedTime(StyledText displayedTime) {
        this.displayedTime = displayedTime;
    }

    /**
     * @return The prefix to display before the name
     */
    public StyledText getPrefix() {
        return prefix;
    }

    /**
     * @param prefix The new prefix to display before the name
     */
    public void setPrefix(StyledText prefix) {
        this.prefix = prefix;
    }

    public StyledText asString() {
        return fullName;
    }
}
