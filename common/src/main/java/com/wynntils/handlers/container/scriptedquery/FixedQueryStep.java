/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container.scriptedquery;

import com.wynntils.handlers.container.ContainerQueryException;
import com.wynntils.handlers.container.type.ContainerContent;

class FixedQueryStep extends QueryStep {
    FixedQueryStep() {
        // Just use a dummy action, it will never be run
        super(c -> true);
    }

    @Override
    public boolean startStep(ScriptedContainerQuery query, ContainerContent container) throws ContainerQueryException {
        // A FixedQueryStep always gets it handleContent called
        getHandleContent().processContainer(container);

        // Try again with next
        if (!query.popOneStep()) return false;
        return query.startStep(container);
    }
}
