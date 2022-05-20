/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functionalities;

import com.google.common.base.CaseFormat;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public abstract class FunctionalityBase extends Functionality {
    protected String getNameCamelCase() {
        String name = this.getClass().getTypeName().replace("Functionality", "");
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

    @Override
    public MutableComponent getNameComponent() {
        return new TranslatableComponent("functionality.wynntils." + getNameCamelCase() + ".name");
    }
}
