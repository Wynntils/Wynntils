/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.items;

import com.google.gson.annotations.SerializedName;

public enum IdentificationModifier {
    @SerializedName("INTEGER")
    Integer(""),
    @SerializedName("PERCENTAGE")
    Percentage("%"),
    @SerializedName("FOUR_SECONDS")
    FourSeconds("/4s"),
    @SerializedName("THREE_SECONDS")
    ThreeSeconds("/3s"),
    @SerializedName("TIER")
    Tier(" tier");

    final String inGame;

    IdentificationModifier(String inGame) {
        this.inGame = inGame;
    }

    public String getInGame(String name) {
        if (this != FourSeconds) return inGame;

        if (name.equals("manaRegen")) return "/5s";
        return "/3s";
    }
}
