/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.wynn.item.WynnItemStack;

public class SearchOverlayProperty extends ItemProperty {
    private boolean searched = false;

    public SearchOverlayProperty(WynnItemStack item) {
        super(item);
    }

    public void setSearched(boolean searched) {
        this.searched = searched;
    }

    public boolean isSearched() {
        return searched;
    }
}
