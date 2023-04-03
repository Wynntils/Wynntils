/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.statuseffects.type;

import com.wynntils.utils.mc.type.CodedString;

public class StatusEffect {
    private final CodedString fullName;
    private final CodedString name; // The name of the consumable (also used to identify it)
    private CodedString displayedTime; // The displayed time remaining. Allows for xx:xx for infinite time effects.
    private CodedString prefix; // The prefix to display before the name. Not included in identifying name.

    public StatusEffect(CodedString name, CodedString displayedTime, CodedString prefix) {
        this.name = name;
        this.displayedTime = displayedTime;
        this.prefix = prefix;
        // Don't add extra spaces
        // Sometimes, " §7" will be parsed as part of the prefix, in an attempt to keep the name field
        // as a proper unformatted string
        // The two differing examples I have for this are Archer's Windy Feet and Warrior's Boiling Blood cooldown
        if (prefix.str().endsWith(" ") || prefix.str().endsWith(" §7")) {
            this.fullName = CodedString.concat(prefix, name, CodedString.of(" "), displayedTime);
        } else {
            this.fullName = CodedString.concat(prefix, CodedString.of(" "), name, CodedString.of(" "), displayedTime);
        }
    }

    /**
     * @return The name of the consumable
     */
    public CodedString getName() {
        return name;
    }

    /**
     * @return The time remaining for the consumable
     */
    public CodedString getDisplayedTime() {
        return displayedTime;
    }

    /**
     * @param displayedTime The new time remaining for the consumable
     */
    public void setDisplayedTime(CodedString displayedTime) {
        this.displayedTime = displayedTime;
    }

    /**
     * @return The prefix to display before the name
     */
    public CodedString getPrefix() {
        return prefix;
    }

    /**
     * @param prefix The new prefix to display before the name
     */
    public void setPrefix(CodedString prefix) {
        this.prefix = prefix;
    }

    public CodedString asString() {
        return fullName;
    }
}
