/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import java.util.Objects;
import net.minecraft.client.resources.language.I18n;

public class BankPageCustomization {
    private String name;
    private QuickJumpButtonIcon icon;

    public BankPageCustomization(int pageIndex) {
        this.name = I18n.get("feature.wynntils.personalStorageUtilities.page", pageIndex);
        this.icon = QuickJumpButtonIcon.NONE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QuickJumpButtonIcon getIcon() {
        return icon;
    }

    public void setIcon(QuickJumpButtonIcon icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        BankPageCustomization that = (BankPageCustomization) o;
        return Objects.equals(name, that.name) && icon == that.icon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, icon);
    }
}
