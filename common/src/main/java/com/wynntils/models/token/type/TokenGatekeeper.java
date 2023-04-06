/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.token.type;

import com.wynntils.core.text.CodedString;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.CappedValue;
import java.util.Objects;

public final class TokenGatekeeper implements Comparable<TokenGatekeeper> {
    private final CodedString gatekeeperTokenName;
    private final CodedString itemTokenName;
    private final Location location;
    private CappedValue deposited;

    public TokenGatekeeper(CodedString gatekeeperTokenName, Location location, CappedValue deposited) {
        this.gatekeeperTokenName = gatekeeperTokenName;
        // Remove the trailing plural 's'
        this.itemTokenName = CodedString.fromString(
                gatekeeperTokenName.getInternalCodedStringRepresentation().replaceAll("s$", ""));
        this.location = location;
        this.deposited = deposited;
    }

    public TokenGatekeeper(
            CodedString gatekeeperTokenName, CodedString itemTokenName, Location location, CappedValue deposited) {
        this.gatekeeperTokenName = gatekeeperTokenName;
        this.itemTokenName = itemTokenName;
        this.location = location;
        this.deposited = deposited;
    }

    public CodedString getGatekeeperTokenName() {
        return gatekeeperTokenName;
    }

    public CodedString getItemTokenName() {
        return itemTokenName;
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
        return Objects.equals(gatekeeperTokenName, that.gatekeeperTokenName) && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatekeeperTokenName, location);
    }

    @Override
    public String toString() {
        return "TokenGatekeeper{" + "type='"
                + gatekeeperTokenName + '\'' + ", location="
                + location + ", deposited="
                + deposited + '}';
    }

    @Override
    public int compareTo(TokenGatekeeper that) {
        // Only the location determines the sorting order
        return getLocation().compareTo(that.getLocation());
    }
}
