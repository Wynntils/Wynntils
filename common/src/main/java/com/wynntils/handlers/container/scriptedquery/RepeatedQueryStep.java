/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container.scriptedquery;

import com.wynntils.handlers.container.ContainerQueryStep;
import com.wynntils.handlers.container.type.ContainerContent;
import java.util.function.Predicate;

class RepeatedQueryStep extends QueryStep {
    private final Predicate<ContainerContent> checkRepeat;

    RepeatedQueryStep(Predicate<ContainerContent> checkRepeat, QueryStep queryStep) {
        super(queryStep);
        this.checkRepeat = checkRepeat;
    }

    @Override
    boolean startStep(ScriptedContainerQuery query, ContainerContent container) {
        if (!checkRepeat.test(container)) {
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
