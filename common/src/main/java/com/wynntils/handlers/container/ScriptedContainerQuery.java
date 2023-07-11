/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.container;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class ScriptedContainerQuery {
    private static final Consumer<String> DEFAULT_ERROR_HANDLER =
            (errorMsg) -> WynntilsMod.warn("Error in ScriptedContainerQuery");

    // No op
    private static final Runnable DEFAULT_ON_COMPLETE = () -> {};
    private final LinkedList<ScriptedQueryStep> steps = new LinkedList<>();
    private Consumer<String> errorHandler = DEFAULT_ERROR_HANDLER;
    private Runnable onComplete = DEFAULT_ON_COMPLETE;
    private final String name;

    private ScriptedContainerQuery(String name) {
        this.name = name;
    }

    public static QueryBuilder builder(String name) {
        return new QueryBuilder(new ScriptedContainerQuery(name));
    }

    public void executeQuery() {
        if (steps.isEmpty()) return;

        ScriptedQueryStep firstStep = steps.pop();
        Handlers.ContainerQuery.runQuery(firstStep);
    }

    private void setErrorHandler(Consumer<String> errorHandler) {
        this.errorHandler = errorHandler;
    }

    private void setOnComplete(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    @FunctionalInterface
    public interface StartAction {
        boolean execute(ContainerContent container);
    }

    @FunctionalInterface
    private interface ContainerVerification {
        boolean verify(Component title, MenuType<?> menuType);
    }

    @FunctionalInterface
    public interface ContainerAction {
        void processContainer(ContainerContent container);
    }

    private class ScriptedQueryStep implements ContainerQueryStep {
        final StartAction startAction;
        final ContainerVerification verification;
        final ContainerAction handleContent;

        private ScriptedQueryStep(
                StartAction startAction, ContainerVerification verification, ContainerAction handleContent) {
            this.startAction = startAction;
            this.verification = verification;
            this.handleContent = handleContent;
        }

        @Override
        public boolean startStep(ContainerContent container) {
            return startAction.execute(container);
        }

        @Override
        public boolean verifyContainer(Component title, MenuType<?> menuType) {
            return verification.verify(title, menuType);
        }

        @Override
        public void handleContent(ContainerContent container) {
            handleContent.processContainer(container);
        }

        @Override
        public ContainerQueryStep getNextStep(ContainerContent container) {
            if (ScriptedContainerQuery.this.steps.isEmpty()) return null;

            if (ScriptedContainerQuery.this.steps.getFirst() instanceof RepeatedQueryStep repeatedQueryStep) {
                if (repeatedQueryStep.shouldRepeat(container)) {
                    return ScriptedContainerQuery.this.steps.getFirst();
                }
            }

            return ScriptedContainerQuery.this.steps.pop();
        }

        @Override
        public void onError(String errorMsg) {
            ScriptedContainerQuery.this.errorHandler.accept(errorMsg);
            // Remove all remaining steps
            ScriptedContainerQuery.this.steps.clear();
        }

        @Override
        public void onComplete() {
            ScriptedContainerQuery.this.onComplete.run();
        }

        @Override
        public String getName() {
            return ScriptedContainerQuery.this.name;
        }
    }

    public static class QueryStep {
        public QueryStep(StartAction startAction) {}

        public ScriptedQueryStep toScriptedQueryStep(ScriptedContainerQuery query) {
            return query.new ScriptedQueryStep(null, null, null);
        }

        public static QueryStep useItemInHotbar(int slotNum) {
            return new QueryStep((container) -> ContainerUtils.openInventory(slotNum));
        }

        public static QueryStep clickOnSlot(int slotNum) {
            return new QueryStep(container -> {
                ContainerUtils.clickOnSlot(
                        slotNum, container.containerId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, container.items());
                return true;
            });
        }

        public QueryStep matchTitle(String regExp) {
            if (verification != null) {
                throw new IllegalStateException("Set verification twice");
            }
            this.verification = (title, type) -> title.getString().matches(regExp);
            checkForCompletion();
            return this;
        }

        public QueryStep expectSameMenu() {
            if (verification != null) {
                throw new IllegalStateException("Set verification twice");
            }
            // We should never get to MenuOpenedEvent
            this.verification = (title, type) -> false;
            checkForCompletion();
            return this;
        }

        public QueryStep processIncomingContainer(ContainerAction action) {
            if (handleContent != null) {
                throw new IllegalStateException("Set handleContent twice");
            }
            this.handleContent = action;
            checkForCompletion();
            return this;
        }

        public QueryStep ignoreIncomingContainer() {
            if (handleContent != null) {
                throw new IllegalStateException("Set handleContent twice");
            }
            this.handleContent = c -> {};
            checkForCompletion();
            return this;
        }
    }

    /**
     * The QueryBuilder builds a ScriptedContainerQuery, which is a sequence of ContainerQueryStep,
     * which are executed one after another. Each step requires three parts:
     * 1) a startAction (to open the container)
     * 2) a verification (to check that we got the right container)
     * 3) a handleContent (to actually consume the content of the container)
     *
     * The builder will accept these three in any order, and create a ContainerQueryStep for each
     * such triplet. It will not allow the creation of a step where one of them are missing.
     */
    public static final class QueryBuilder {
        private final ScriptedContainerQuery query;

        private QueryBuilder(ScriptedContainerQuery scriptedContainerQuery) {
            query = scriptedContainerQuery;
        }

        public QueryBuilder onError(Consumer<String> errorHandler) {
            query.setErrorHandler(errorHandler);
            return this;
        }

        public ScriptedContainerQuery build() {
            return query;
        }

        public QueryBuilder reprocess(ContainerAction action) {
            // FIXME
            query.steps.add(null);
            return this;
        }

        public QueryBuilder execute(Runnable r) {
            // FIXME
            query.steps.add(null);
            return this;
        }

        public QueryBuilder then(QueryStep step) {
            query.steps.add(step.toScriptedQueryStep(query));
            return this;
        }

        public QueryBuilder repeat(Predicate<ContainerContent> containerCheck, QueryStep step) {
            // FIXME
            query.steps.add(null);
            return this;
        }
    }

    public static boolean containerHasSlot(
            ContainerContent container, int slotNum, Item expectedItemType, StyledText expectedItemName) {
        ItemStack itemStack = container.items().get(slotNum);
        if (!itemStack.is(expectedItemType)
                || !StyledText.fromComponent(itemStack.getHoverName()).equals(expectedItemName)) return false;

        return true;
    }

    private class RepeatedQueryStep extends ScriptedQueryStep {
        private RepeatedQueryStep(StartAction startAction, ContainerVerification verification, ContainerAction handleContent) {
            super(startAction, verification, handleContent);
        }

        public boolean shouldRepeat(ContainerContent container) {
        }

        public ContainerQueryStep getRepeatStep() {
        }
    }
}
