/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import static net.minecraft.commands.Commands.*;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.commands.CommandBase;
import com.wynntils.wc.utils.lootrun.LootrunUtils;
import java.io.File;
import java.util.*;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class LootrunCommand extends CommandBase {
    private static final SuggestionProvider<CommandSourceStack> LOOTRUN_SUGGESTION_PROVIDER =
            (context, suggestions) -> SharedSuggestionProvider.suggest(
                    Stream.of(LootrunUtils.LOOTRUNS.list())
                            .map((name) -> name.replaceAll("\\.json$", ""))
                            .map(StringArgumentType::escapeIfRequired),
                    suggestions);

    private int loadLootrun(CommandContext<CommandSourceStack> context) {
        String fileName = StringArgumentType.getString(context, "lootrun");

        boolean successful = LootrunUtils.tryLoadFile(fileName);
        Vec3 startingPoint = LootrunUtils.getStartingPoint();

        if (!successful || startingPoint == null) {
            context.getSource()
                    .sendFailure(new TranslatableComponent("feature.wynntils.lootrunUtils.lootrunCouldNotBeLoaded")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        BlockPos start = new BlockPos(startingPoint);
        context.getSource()
                .sendSuccess(
                        new TranslatableComponent(
                                        "feature.wynntils.lootrunUtils.lootrunStart",
                                        start.getX(),
                                        start.getY(),
                                        start.getZ())
                                .withStyle(ChatFormatting.GREEN),
                        false);
        return 1;
    }

    private int recordLootrun(CommandContext<CommandSourceStack> context) {
        if (LootrunUtils.getState() != LootrunUtils.LootrunState.RECORDING) {
            LootrunUtils.startRecording();
            context.getSource()
                    .sendSuccess(
                            new TranslatableComponent(
                                    "feature.wynntils.lootrunUtils.recordStart",
                                    new TextComponent("/lootrun record")
                                            .withStyle(ChatFormatting.UNDERLINE)
                                            .withStyle((style) -> style.withClickEvent(
                                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lootrun record")))),
                            false);
        } else {
            LootrunUtils.stopRecording();
            context.getSource()
                    .sendSuccess(
                            new TranslatableComponent(
                                            "feature.wynntils.lootrunUtils.recordStop1",
                                            new TextComponent("/lootrun clear")
                                                    .withStyle(ChatFormatting.UNDERLINE)
                                                    .withStyle((style) -> style.withClickEvent(new ClickEvent(
                                                            ClickEvent.Action.RUN_COMMAND, "/lootrun clear"))))
                                    .withStyle(ChatFormatting.RED)
                                    .append("\n")
                                    .append(new TranslatableComponent(
                                                    "feature.wynntils.lootrunUtils.recordStop2",
                                                    new TextComponent("/lootrun save <name>")
                                                            .withStyle(ChatFormatting.UNDERLINE)
                                                            .withStyle((style) -> style.withClickEvent(new ClickEvent(
                                                                    ClickEvent.Action.SUGGEST_COMMAND,
                                                                    "/lootrun save "))))
                                            .withStyle(ChatFormatting.GREEN)),
                            false);
        }
        return 1;
    }

    private int saveLootrun(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        LootrunUtils.LootrunSaveResult lootrunSaveResult = LootrunUtils.saveLootrun(name);
        switch (lootrunSaveResult) {
            case SAVED -> {
                context.getSource()
                        .sendSuccess(
                                new TranslatableComponent("feature.wynntils.lootrunUtils.savedLootrun")
                                        .withStyle(ChatFormatting.GREEN),
                                false);
                return 1;
            }
            case ERROR_SAVING -> {
                context.getSource()
                        .sendFailure(new TranslatableComponent("feature.wynntils.lootrunUtils.errorSavingLootrun")
                                .withStyle(ChatFormatting.RED));
                return 0;
            }
            case ERROR_ALREADY_EXISTS -> {
                context.getSource()
                        .sendFailure(new TranslatableComponent(
                                        "feature.wynntils.lootrunUtils.errorSavingLootrunAlreadyExists")
                                .withStyle(ChatFormatting.RED));
                return 0;
            }
        }
        return 0;
    }

    private int addJsonLootrunNote(CommandContext<CommandSourceStack> context) {
        Component text = ComponentArgument.getComponent(context, "text");
        Entity root = McUtils.player().getRootVehicle();
        BlockPos pos = root.blockPosition();
        context.getSource()
                .sendSuccess(
                        new TranslatableComponent("feature.wynntils.lootrunUtils.addedNote", pos.toShortString())
                                .append("\n" + text),
                        false);
        return LootrunUtils.addNote(text);
    }

    private int addTextLootrunNote(CommandContext<CommandSourceStack> context) {
        Component text = new TextComponent(StringArgumentType.getString(context, "text"));
        Entity root = McUtils.player().getRootVehicle();
        BlockPos pos = root.blockPosition();
        context.getSource()
                .sendSuccess(
                        new TranslatableComponent("feature.wynntils.lootrunUtils.addedNote", pos.toShortString())
                                .append("\n" + text),
                        false);
        return LootrunUtils.addNote(text);
    }

    private int listLootrunNote(CommandContext<CommandSourceStack> context) {
        List<LootrunUtils.Note> notes = LootrunUtils.getCurrentNotes();
        if (notes.isEmpty()) {
            context.getSource().sendFailure(new TranslatableComponent("feature.wynntils.lootrunUtils.listNoteNoNote"));
        } else {
            MutableComponent component = new TranslatableComponent("feature.wynntils.lootrunUtils.listNoteHeader");
            for (LootrunUtils.Note note : notes) {
                BlockPos pos = new BlockPos(note.position());
                String posString = pos.toShortString();

                component
                        .append("\n")
                        .append(new TextComponent("[X]").withStyle((style) -> style.withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        new TranslatableComponent("feature.wynntils.lootrunUtils.listClickToDelete")))
                                .withClickEvent(new ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/lootrun note delete " + posString.replace(",", "")))
                                .withColor(ChatFormatting.RED)))
                        .append(" " + posString + ": ")
                        .append(note.component());
            }
            context.getSource().sendSuccess(component, false);
        }
        return 1;
    }

    private int deleteLootrunNote(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getSpawnablePos(context, "pos");
        var removedNote = LootrunUtils.deleteNoteAt(pos);

        if (removedNote != null) {
            context.getSource()
                    .sendSuccess(
                            new TranslatableComponent(
                                            "feature.wynntils.lootrunUtils.noteRemovedSuccessfully",
                                            removedNote.component())
                                    .withStyle(ChatFormatting.GREEN),
                            false);
        } else {
            String posString = pos.toShortString();
            context.getSource()
                    .sendFailure(
                            new TranslatableComponent("feature.wynntils.lootrunUtils.noteUnableToFind", posString));
        }
        return LootrunUtils.recompileLootrun(true);
    }

    private int clearLootrun(CommandContext<CommandSourceStack> context) {
        if (LootrunUtils.getState() == LootrunUtils.LootrunState.DISABLED) {
            context.getSource().sendFailure(new TranslatableComponent("feature.wynntils.lootrunUtils.noActiveLootrun"));
            return 0;
        }

        LootrunUtils.clearCurrentLootrun();

        context.getSource()
                .sendSuccess(
                        new TranslatableComponent("feature.wynntils.lootrunUtils.clearSuccessful")
                                .withStyle(ChatFormatting.GREEN),
                        false);
        return 1;
    }

    private int deleteLootrun(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        File file = new File(LootrunUtils.LOOTRUNS, name + ".json");
        if (!file.exists()) {
            context.getSource()
                    .sendFailure(new TranslatableComponent("feature.wynntils.lootrunUtils.lootrunDoesntExist", name));
        } else if (file.delete()) {
            context.getSource()
                    .sendSuccess(
                            new TranslatableComponent("feature.wynntils.lootrunUtils.lootrunDeleted", name)
                                    .withStyle(ChatFormatting.GREEN),
                            false);
            return 1;
        } else {
            context.getSource()
                    .sendFailure(
                            new TranslatableComponent("feature.wynntils.lootrunUtils.lootrunCouldNotBeDeleted", name));
        }
        return 0;
    }

    private int renameLootrun(CommandContext<CommandSourceStack> context) {
        String oldName = StringArgumentType.getString(context, "old");
        String newName = StringArgumentType.getString(context, "new");
        File oldFile = new File(LootrunUtils.LOOTRUNS, oldName + ".json");
        File newFile = new File(LootrunUtils.LOOTRUNS, newName + ".json");
        if (!oldFile.exists()) {
            context.getSource()
                    .sendFailure(
                            new TranslatableComponent("feature.wynntils.lootrunUtils.lootrunDoesntExist", oldName));
        } else if (oldFile.renameTo(newFile)) {
            context.getSource()
                    .sendSuccess(
                            new TranslatableComponent("feature.wynntils.lootrunUtils.lootrunRenamed", oldName, newName)
                                    .withStyle(ChatFormatting.GREEN),
                            false);
            return 1;
        } else {
            context.getSource()
                    .sendFailure(new TranslatableComponent(
                            "feature.wynntils.lootrunUtils.lootrunCouldNotBeRenamed", oldName, newName));
        }
        return 0;
    }

    private int addChest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getSpawnablePos(context, "pos");

        boolean successful = LootrunUtils.addChest(pos);

        if (successful) {
            context.getSource()
                    .sendSuccess(
                            new TranslatableComponent("feature.wynntils.lootrunUtils.chestAdded", pos.toShortString())
                                    .withStyle(ChatFormatting.GREEN),
                            false);
        } else {
            context.getSource()
                    .sendFailure(new TranslatableComponent(
                            "feature.wynntils.lootrunUtils.chestAlreadyAdded", pos.toShortString()));
        }

        return LootrunUtils.recompileLootrun(true);
    }

    private int removeChest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getSpawnablePos(context, "pos");

        boolean successful = LootrunUtils.removeChest(pos);

        if (successful) {
            context.getSource()
                    .sendSuccess(
                            new TranslatableComponent("feature.wynntils.lootrunUtils.chestRemoved", pos.toShortString())
                                    .withStyle(ChatFormatting.GREEN),
                            false);
        } else {
            context.getSource()
                    .sendFailure(new TranslatableComponent(
                            "feature.wynntils.lootrunUtils.chestDoesNotExist", pos.toShortString()));
        }

        return LootrunUtils.recompileLootrun(true);
    }

    private int undoLootrun(CommandContext<CommandSourceStack> context) {
        if (LootrunUtils.getState() != LootrunUtils.LootrunState.RECORDING) {
            context.getSource().sendFailure(new TranslatableComponent("feature.wynntils.lootrunUtils.notRecording"));
        } else {
            LootrunUtils.LootrunUndoResult lootrunUndoResult = LootrunUtils.tryUndo();
            switch (lootrunUndoResult) {
                case SUCCESSFUL -> {
                    context.getSource()
                            .sendSuccess(
                                    new TranslatableComponent("feature.wynntils.lootrunUtils.undoSuccessful"), false);
                    return 1;
                }
                case ERROR_STAND_NEAR_POINT -> {
                    context.getSource()
                            .sendFailure(new TranslatableComponent("feature.wynntils.lootrunUtils.undoStandNear"));
                    return 0;
                }
                case ERROR_NOT_FAR_ENOUGH -> {
                    context.getSource()
                            .sendFailure(new TranslatableComponent("feature.wynntils.lootrunUtils.undoNotFarEnough"));
                    return 0;
                }
            }
        }
        return 0;
    }

    private int folderLootrun(CommandContext<CommandSourceStack> context) {
        Util.getPlatform().openFile(LootrunUtils.LOOTRUNS);
        return 1;
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(literal("lootrun")
                .then(literal("load")
                        .then(argument("lootrun", StringArgumentType.string())
                                .suggests(LOOTRUN_SUGGESTION_PROVIDER)
                                .executes(this::loadLootrun)))
                .then(literal("record").executes(this::recordLootrun))
                .then(literal("save")
                        .then(argument("name", StringArgumentType.string()).executes(this::saveLootrun)))
                .then(literal("note")
                        .then(literal("add")
                                .then(literal("json")
                                        .then(argument("text", ComponentArgument.textComponent())
                                                .executes(this::addJsonLootrunNote)))
                                .then(literal("text")
                                        .then(argument("text", StringArgumentType.greedyString())
                                                .executes(this::addTextLootrunNote))))
                        .then(literal("list").executes(this::listLootrunNote))
                        .then(literal("delete")
                                .then(argument("pos", BlockPosArgument.blockPos())
                                        .executes(this::deleteLootrunNote))))
                .then(literal("clear").executes(this::clearLootrun))
                .then(literal("delete")
                        .then(argument("name", StringArgumentType.string())
                                .suggests(LOOTRUN_SUGGESTION_PROVIDER)
                                .executes(this::deleteLootrun)))
                .then(literal("rename")
                        .then(argument("old", StringArgumentType.string())
                                .suggests(LOOTRUN_SUGGESTION_PROVIDER)
                                .then(argument("new", StringArgumentType.string())
                                        .executes(this::renameLootrun))))
                .then(literal("chest")
                        .then(argument("pos", BlockPosArgument.blockPos())
                                .then(literal("add").executes(this::addChest)))
                        .then(literal("remove").executes(this::removeChest)))
                .then(literal("undo").executes(this::undoLootrun))
                .then(literal("folder").executes(this::folderLootrun)));

        dispatcher.register(literal("lr").redirect(node));
    }
}
