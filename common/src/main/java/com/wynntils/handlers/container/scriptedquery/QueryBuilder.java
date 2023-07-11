/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container.scriptedquery;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.container.type.ContainerAction;
import com.wynntils.handlers.container.type.ContainerContent;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The QueryBuilder builds a ScriptedContainerQuery, which is a sequence of ContainerQueryStep,
 * which are executed one after another. Each step requires three parts:
 * 1) a startAction (to open the container)
 * 2) a verification (to check that we got the right container)
 * 3) a handleContent (to actually consume the content of the container)
 * <p>
 * The builder will accept these three in any order, and create a ContainerQueryStep for each
 * such triplet. It will not allow the creation of a step where one of them are missing.
 */
public final class QueryBuilder {
    private static final Consumer<String> DEFAULT_ERROR_HANDLER =
            (errorMsg) -> WynntilsMod.warn("Error in ScriptedContainerQuery");

    Consumer<String> errorHandler = DEFAULT_ERROR_HANDLER;
    final LinkedList<QueryStep> steps = new LinkedList<>();
    final String name;

    QueryBuilder(String name) {
        this.name = name;
    }

    public QueryBuilder onError(Consumer<String> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public ScriptedContainerQuery build() {
        return new ScriptedContainerQuery(name, steps, errorHandler);
    }

    public QueryBuilder reprocess(ContainerAction action) {
        steps.add(new FixedQueryStep().processIncomingContainer(action));
        return this;
    }

    public QueryBuilder execute(Runnable r) {
        return reprocess(c -> {
            r.run();
        });
    }

    public QueryBuilder then(QueryStep step) {
        steps.add(step);
        return this;
    }

    public QueryBuilder repeat(Predicate<ContainerContent> containerCheck, QueryStep step) {
        steps.add(new RepeatedQueryStep(containerCheck, step));
        return this;
    }
}
