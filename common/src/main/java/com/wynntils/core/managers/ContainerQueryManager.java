/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.utils.McUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ContainerQueryManager extends CoreManager {
    private static final int NO_CONTAINER = -2;
    private static int containerId = NO_CONTAINER;
    private static int lastHandledContentId = NO_CONTAINER;
    private static int transactionId = 0;
    private static Component currentTitle;
    private static MenuType currentMenuType;

    private static QueryStep currentStep;

    private static Consumer<String> errorHandler;

    @SubscribeEvent
    public static void onMenuOpened(MenuEvent.MenuOpenedEvent e) {
        if (currentStep == null) return;

        boolean matches = currentStep.verify(e.getTitle(), e.getMenuType());
        if (matches) {
            containerId = e.getContainerId();
            currentTitle = e.getTitle();
            currentMenuType = e.getMenuType();
            transactionId = 0;
            e.setCanceled(true);
        } else {
            handleError("Unexpected container opened");
        }
    }

    @SubscribeEvent
    public static void onMenuForcefullyClosed(MenuEvent.MenuClosedEvent e) {
        if (currentStep == null) return;

        // Server closed our container window. This should not happen
        // but if it do, report failure
        handleError("Server closed container");
    }

    @SubscribeEvent
    public static void onContainerSetContent(ContainerSetContentEvent e) {
        if (currentStep == null) return;

        if (containerId == NO_CONTAINER) {
            // We have not registered a MenuOpenedEvent
            handleError("Container contents without associated container open");
            return;
        }

        int id = e.getContainerId();
        if (id != containerId) {
            handleError("Another container opened");
            return;
        }

        if (containerId == lastHandledContentId) {
            // Wynncraft sometimes sends contents twice; drop this
            e.setCanceled(true);
            return;
        }

        lastHandledContentId = containerId;
        // Callback to handle this container
        ContainerContent currentContainer = new ContainerContent(e.getItems(), currentTitle, currentMenuType);
        currentStep.handleContent(currentContainer);

        QueryStep nextStep = currentStep.getNextStep(currentContainer);

        if (nextStep != null) {
            currentStep = nextStep;
            currentStep.startStep(currentContainer);
        } else {
            // We're done
            currentStep = null;
            containerId = NO_CONTAINER;
            lastHandledContentId = NO_CONTAINER;
            McUtils.sendPacket(new ServerboundContainerClosePacket(id));
        }

        e.setCanceled(true);
    }

    private static void handleError(String errorMsg) {
        System.out.println("error: " + errorMsg);
        errorHandler.accept(errorMsg);
        containerId = NO_CONTAINER;
        lastHandledContentId = NO_CONTAINER;
        currentStep = null;
    }

    public static void clickOnSlot(List<ItemStack> items, int clickedSlot) {
        Int2ObjectMap<ItemStack> changedSlots = new Int2ObjectOpenHashMap<>();
        changedSlots.put(clickedSlot, new ItemStack(Items.AIR));

        int mouseButtonNum = 0;
        McUtils.sendPacket(new ServerboundContainerClickPacket(
                containerId,
                transactionId,
                clickedSlot,
                mouseButtonNum,
                ClickType.PICKUP,
                items.get(clickedSlot),
                changedSlots));
        transactionId++;
    }

    public static void openInventory(int slotNum) {
        int id = McUtils.player().containerMenu.containerId;
        if (id != 0) {
            // another inventory is already open, cannot do this
            return;
        }
        int prevItem = McUtils.inventory().selected;
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(slotNum));
        McUtils.sendPacket(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND));
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(prevItem));
    }

    public static void runQuery(QueryStep firstStep) {
        currentStep = firstStep;
        firstStep.startStep(null);
    }

    @FunctionalInterface
    public interface ContainerAction {
        void processContainer(ContainerContent container);
    }

    @FunctionalInterface
    public interface ContainerVerification {
        boolean verify(Component title, MenuType menuType);
    }

    public interface QueryStep {
        void startStep(ContainerContent currentContainer);

        boolean verify(Component title, MenuType menuType);

        void handleContent(ContainerContent container);

        QueryStep getNextStep(ContainerContent container);
    }

    public record ContainerContent(List<ItemStack> items, Component title, MenuType menuType) {}

    public static class ContainerQueryBuilder {
        ContainerAction startAction;
        ContainerVerification verification;
        ContainerAction handleContent;

        LinkedList<ScriptedQueryStep> steps = new LinkedList<>();
        Consumer<String> errorHandler;

        public static ContainerQueryBuilder start() {
            return new ContainerQueryBuilder();
        }

        public ContainerQueryBuilder onError(Consumer<String> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        private void checkForCompletion() {
            if (startAction != null && verification != null && handleContent != null) {
                ScriptedQueryStep nextStep = new ScriptedQueryStep(startAction, verification, handleContent, steps);
                steps.add(nextStep);
                startAction = null;
                verification = null;
                handleContent = null;
            }
        }

        public ContainerQueryBuilder expectTitle(String expectedTitle) {
            if (verification != null) {
                throw new IllegalStateException("Set verification twice");
            }
            this.verification = (title, type) -> title.getString().equals(expectedTitle);
            checkForCompletion();
            return this;
        }

        public ContainerQueryBuilder processContainer(ContainerAction action) {
            if (handleContent != null) {
                throw new IllegalStateException("Set handleContent twice");
            }
            this.handleContent = action;
            checkForCompletion();
            return this;
        }

        public ContainerQueryBuilder clickOnSlot(int clickedSlot) {
            if (startAction != null) {
                throw new IllegalStateException("Set startAction twice");
            }
            this.startAction = (container) -> {
                System.out.println("Clicking on slot: " + clickedSlot);
                ContainerQueryManager.clickOnSlot(container.items, clickedSlot);
            };
            checkForCompletion();
            return this;
        }

        public ContainerQueryBuilder useItemInHotbar(int slotNum) {
            if (startAction != null) {
                throw new IllegalStateException("Set startAction twice");
            }
            this.startAction = (container) -> {
                System.out.println("Use item in hotbar: " + slotNum);
                ContainerQueryManager.openInventory(slotNum);
            };
            checkForCompletion();
            return this;
        }

        public ScriptedQuery build() {
            if (startAction != null || handleContent != null || verification != null) {
                throw new IllegalStateException("Partial contents only for last step");
            }
            return new ScriptedQuery(steps, errorHandler);
        }
    }

    public static class ScriptedQueryStep implements QueryStep {
        final ContainerAction startAction;
        final ContainerVerification verification;
        final ContainerAction handleContent;
        final LinkedList<ScriptedQueryStep> restOfTheSteps;

        public ScriptedQueryStep(
                ContainerAction startAction,
                ContainerVerification verification,
                ContainerAction handleContent,
                LinkedList<ScriptedQueryStep> restOfTheSteps) {
            this.startAction = startAction;
            this.verification = verification;
            this.handleContent = handleContent;
            this.restOfTheSteps = restOfTheSteps;
        }

        @Override
        public void startStep(ContainerContent container) {
            startAction.processContainer(container);
        }

        @Override
        public boolean verify(Component title, MenuType menuType) {
            return verification.verify(title, menuType);
        }

        @Override
        public void handleContent(ContainerContent container) {
            handleContent.processContainer(container);
        }

        @Override
        public QueryStep getNextStep(ContainerContent container) {
            if (restOfTheSteps.isEmpty()) return null;

            return restOfTheSteps.pop();
        }
    }

    public static class ScriptedQuery {
        private final LinkedList<ScriptedQueryStep> steps;
        private final Consumer<String> errorHandler;

        protected ScriptedQuery(LinkedList<ScriptedQueryStep> steps, Consumer<String> errorHandler) {
            this.steps = steps;
            this.errorHandler = errorHandler;
        }

        public void executeQuery() {
            if (steps.isEmpty()) return;

            ScriptedQueryStep firstStep = steps.pop();
            ContainerQueryManager.runQuery(firstStep);
        }
    }
}
