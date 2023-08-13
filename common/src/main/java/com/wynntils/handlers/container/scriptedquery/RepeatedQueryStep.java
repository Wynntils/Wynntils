/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container.scriptedquery;

import com.wynntils.handlers.container.ContainerQueryException;
import com.wynntils.handlers.container.ContainerQueryStep;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.handlers.container.type.ContainerPredicate;

class RepeatedQueryStep extends QueryStep {
    private final ContainerPredicate checkRepeat;

    RepeatedQueryStep(ContainerPredicate checkRepeat, QueryStep queryStep) {
        super(queryStep);
        this.checkRepeat = checkRepeat;
    }

    @Override
    boolean startStep(ScriptedContainerQuery query, ContainerContent container) throws ContainerQueryException {
        if (!checkRepeat.execute(container)) {
            // Skip this, and retry with next step from query
            if (!query.popOneStep()) return false;
            return query.startStep(container);
        }

        // Otherwise run this as a normal step
        return super.startStep(query, container);
    }

    @Override
    ContainerQueryStep getNextStep(ScriptedContainerQuery query) {
        // Try this again
        return query;
    }
}
