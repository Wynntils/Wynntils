/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.token.type;

import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.CappedValue;
import java.util.Objects;

public final class TokenGatekeeper {
    private final String type;
    private final Location location;
    private CappedValue deposited;

    public TokenGatekeeper(String type, Location location, CappedValue deposited) {
        this.type = type;
        this.location = location;
        this.deposited = deposited;
    }

    public String getType() {
        return type;
    }

    public CappedValue getDeposited() {
        return deposited;
    }

    public void setDeposited(CappedValue value) {
        this.deposited = value;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenGatekeeper that = (TokenGatekeeper) o;
        return Objects.equals(type, that.type) && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, location);
    }

    @Override
    public String toString() {
        return "TokenGatekeeper{" + "type='"
                + type + '\'' + ", location="
                + location + ", deposited="
                + deposited + '}';
    }
}
