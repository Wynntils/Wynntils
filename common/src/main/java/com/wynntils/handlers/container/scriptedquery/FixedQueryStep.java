/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container.scriptedquery;

import com.wynntils.handlers.container.type.ContainerContent;

class FixedQueryStep extends QueryStep {
    FixedQueryStep() {
        super(c -> true);
    }

    @Override
    public boolean startStep(ScriptedContainerQuery query, ContainerContent container) {
        // A FixedQueryStep always gets it handleContent called
        handleContent.processContainer(container);

        // Try again with next
        if (!query.popOneStep()) return false;
        return query.startStep(container);
    }
}
