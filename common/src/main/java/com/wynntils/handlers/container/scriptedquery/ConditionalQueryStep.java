/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container.scriptedquery;

import com.wynntils.handlers.container.ContainerQueryException;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.handlers.container.type.ContainerPredicate;

public class ConditionalQueryStep extends QueryStep {
    private final ContainerPredicate conditionPredicate;

    ConditionalQueryStep(ContainerPredicate conditionPredicate, QueryStep queryStep) {
        super(queryStep);
        this.conditionPredicate = conditionPredicate;
    }

    @Override
    boolean startStep(ScriptedContainerQuery query, ContainerContent container) throws ContainerQueryException {
        if (conditionPredicate.execute(container)) {
            // Run this as a normal step
            return super.startStep(query, container);
        } else {
            // Skip this, and retry with next step from query
            if (!query.popOneStep()) return false;
            return query.startStep(container);
        }
    }
}
