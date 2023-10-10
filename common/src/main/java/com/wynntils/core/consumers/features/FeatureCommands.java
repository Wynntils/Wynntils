/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.features;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.consumers.features.properties.RegisterCommand;
import java.lang.reflect.Field;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.apache.commons.lang3.reflect.FieldUtils;

public class FeatureCommands {
    private final LiteralArgumentBuilder<CommandSourceStack> commandNodeBuilder;
    private LiteralCommandNode<CommandSourceStack> commandNode;

    public FeatureCommands() {
        commandNodeBuilder = Commands.literal("execute");
    }

    public void discoverCommands(Feature feature) {
        for (Field f : FieldUtils.getFieldsWithAnnotation(feature.getClass(), RegisterCommand.class)) {
            if (!f.getType().equals(LiteralCommandNode.class)) {
                WynntilsMod.error("Incorrect type for @RegisterCommand " + f.getName() + " in "
                        + feature.getClass().getName());
                return;
            }

            try {
                LiteralCommandNode<CommandSourceStack> node =
                        (LiteralCommandNode<CommandSourceStack>) FieldUtils.readField(f, feature, true);

                LiteralCommandNode<CommandSourceStack> featureNode =
                        Commands.literal(feature.getShortName()).build();
                featureNode.addChild(node);
                commandNodeBuilder.then(featureNode);
            } catch (IllegalAccessException e) {
                WynntilsMod.error(
                        "Failed reading field of @RegisterCommand " + f.getName() + " in "
                                + feature.getClass().getName(),
                        e);
            }
        }
    }

    public void init() {
        // Build the command node if it hasn't been built yet
        if (commandNode == null) {
            commandNode = commandNodeBuilder.build();
        }
    }

    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        assert commandNode != null;
        return commandNode;
    }
}
