/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container.scriptedquery;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.container.type.ContainerAction;
import com.wynntils.handlers.container.type.ContainerPredicate;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * The QueryBuilder builds a ScriptedContainerQuery, which is basically a sequence of QueryStep,
 * which are executed one after another. To enable more complex processing, additional subtypes
 * of QuerySteps can be created. Each normal QueryStep consists of three parts:
 * 1) a startAction (to open the container)
 * 2) a verification (to check the container that opened, if any)
 * 3) a handleContent (to actually consume the content of the container)
 *
 * A QueryStep with no verification will assume no new container is opened.
 * A QueryStep with no handleContent will just perform a no-op for consuming the content.
 */
public final class QueryBuilder {
    private static final Consumer<String> DEFAULT_ERROR_HANDLER =
            (errorMsg) -> WynntilsMod.warn("Error in ScriptedContainerQuery: " + errorMsg);

    private final String name;
    private final LinkedList<QueryStep> steps = new LinkedList<>();
    private Consumer<String> errorHandler = DEFAULT_ERROR_HANDLER;

    QueryBuilder(String name) {
        this.name = name;
    }

    public QueryBuilder onError(Consumer<String> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public QueryBuilder then(QueryStep step) {
        steps.add(step);
        return this;
    }

    public QueryBuilder repeat(ContainerPredicate containerCheck, QueryStep step) {
        steps.add(new RepeatedQueryStep(containerCheck, step));
        return this;
    }

    public QueryBuilder reprocess(ContainerAction action) {
        steps.add(new FixedQueryStep().processIncomingContainer(action));
        return this;
    }

    public QueryBuilder execute(Runnable r) {
        return reprocess(c -> r.run());
    }

    public ScriptedContainerQuery build() {
        return new ScriptedContainerQuery(name, steps, errorHandler);
    }
}
