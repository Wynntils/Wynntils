/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.models.statuseffects.type.StatusEffect;
import com.wynntils.utils.render.Texture;

public enum AbilityCooldown {
    ARCANE_SPEED("Arcane Speed", Texture.ARCANE_SPEED),
    ARMOUR_BREAKER("Armour Breaker", Texture.ARMOUR_BREAKER),
    BAKALS_GRASP("Bak'al's Grasp", Texture.BAKALS_GRASP),
    BAMBOOZLE("Bamboozle", Texture.BAMBOOZLE),
    BOILING_BLOOD("Boiling Blood", Texture.BOILING_BLOOD),
    BURNING_SIGIL("Burning Sigil", Texture.BURNING_SIGIL),
    CHANT_OF_THE_FANATIC("Chant of the Fanatic", Texture.CHANT_OF_THE_FANATIC),
    CHANT_OF_THE_HERETIC("Chant of the Heretic", Texture.CHANT_OF_THE_HERETIC),
    CHANT_OF_THE_LUNATIC("Chant of the Lunatic", Texture.CHANT_OF_THE_LUNATIC),
    CHILLING_SNARE("Chilling Snare", Texture.CHILLING_SNARE),
    CLEANSING_BREEZE("Cleansing Breeze", Texture.CLEANSING_BREEZE),
    COUNTER("Counter", Texture.COUNTER),
    COURSING_RESTRAINTS("Coursing Restraints", Texture.COURSING_RESTRAINTS),
    DEFLAGRATE("Deflagrate", Texture.DEFLAGRATE),
    DUPLICITY("Duplicity", Texture.DUPLICITY),
    FIRE_CREEP("Fire Creep", Texture.FIRE_CREEP),
    FOCUS("Focus", Texture.FOCUS),
    FORTITUDE("Fortitude", Texture.FORTITUDE),
    FREEZING_SIGIL("Freezing Sigil", Texture.FREEZING_SIGIL),
    GHOSTLY_TRIGGER("Ghostly Trigger", Texture.GHOSTLY_TRIGGER),
    HEAVENLY_TRUMPET("Heavenly Trumpet", Texture.HEAVENLY_TRUMPET),
    HOP("Hop", Texture.HOP),
    INTOXICATING_BLOOD("Intoxicating Blood", Texture.INTOXICATING_BLOOD),
    LEAP("Leap", Texture.LEAP),
    LUSTER_PURGE("Luster Purge", Texture.LUSTER_PURGE),
    MANACHISM("Manachism", Texture.MANACHISM),
    MANTLE_OF_THE_BOVEMISTS("Mantle of the Bovemists", Texture.MANTLE_OF_THE_BOVEMISTS),
    MIRAGE("Mirage", Texture.MIRAGE),
    MIRROR_IMAGE("Mirror Image", Texture.MIRROR_IMAGE),
    MISDIRECTION("Misdirection", Texture.MISDIRECTION),
    PARTING_GIFT("Parting Gift", Texture.PARTING_GIFT),
    PROVOKE("Provoke", Texture.PROVOKE),
    PURIFICATION("Purification", Texture.PURIFICATION),
    RADIANCE("Radiance", Texture.RADIANCE),
    SANDBAGGING("Sandbagging", Texture.SANDBAGGING),
    SATSUJIN("Satsujin", Texture.SATSUJIN),
    SECOND_CHANCE("Second Chance", Texture.SECOND_CHANCE),
    SHADOW_PROJECTION("Shadow Projection", Texture.SHADOW_PROJECTION),
    SOUL_SIPHON("Soul Siphon", Texture.SOUL_SIPHON),
    SUNFLARE("Sunflare", Texture.SUNFLARE),
    VANISH("Vanish", Texture.VANISH),
    VIOLENT_VORTEX("Violent Vortex", Texture.VIOLENT_VORTEX);

    private final String name;
    private final Texture texture;

    private float serverRemainingSeconds = 0.0f;
    private float maxSeconds = 0.0f;

    AbilityCooldown(String name, Texture texture) {
        this.name = name;
        this.texture = texture;
    }

    public static AbilityCooldown fromStatusEffect(StatusEffect statusEffect) {
        for (AbilityCooldown cooldown : AbilityCooldown.values()) {
            if (cooldown.getName().equalsIgnoreCase(statusEffect.getName().getStringWithoutFormatting())) {
                return cooldown;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setServerRemainingSeconds(float seconds) {
        this.serverRemainingSeconds = Math.max(0.0f, seconds);

        if (this.maxSeconds <= 0.0f) {
            this.maxSeconds = this.serverRemainingSeconds;
        } else if (this.serverRemainingSeconds > this.maxSeconds) {
            this.maxSeconds = this.serverRemainingSeconds;
        }
    }

    public float getServerRemainingSeconds() {
        return serverRemainingSeconds;
    }

    public float getMaxSeconds() {
        return maxSeconds;
    }

    public void resetCooldownState() {
        serverRemainingSeconds = 0.0f;
        maxSeconds = 0.0f;
    }
}
