/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.commands.CommandBase;
import com.wynntils.wc.utils.lootrun.LootrunUtils;
import com.wynntils.wc.utils.lootrun.objects.LootrunUncompiled;
import com.wynntils.wc.utils.lootrun.objects.RecordingInformation;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.world.phys.Vec3;

public class LootrunCommand extends CommandBase {
    private static final SuggestionProvider<CommandSourceStack> LOOTRUN_SUGGESTION_PROVIDER =
            (context, suggestions) -> SharedSuggestionProvider.suggest(
                    Stream.of(LootrunUtils.LOOTRUNS.list())
                            .map((name) -> name.replaceAll("\\.json$", ""))
                            .map(StringArgumentType::escapeIfRequired),
                    suggestions);

    private int loadLootrun(CommandContext<CommandSourceStack> context) {
        String lootrun = StringArgumentType.getString(context, "lootrun") + ".json";
        File lootrunFile = new File(LootrunUtils.LOOTRUNS, lootrun);
        if (lootrunFile.exists()) {
            FileReader file;
            try {
                file = new FileReader(lootrunFile);
                JsonObject json = JsonParser.parseReader(file).getAsJsonObject();
                LootrunUtils.register();
                LootrunUtils.uncompiled = readJson(lootrunFile, json);
                LootrunUtils.lootrun = LootrunUtils.compile(LootrunUtils.uncompiled);
                BlockPos start = new BlockPos(LootrunUtils.uncompiled.points().get(0));
                context.getSource()
                        .sendSuccess(
                                new TranslatableComponent(
                                                "feature.wynntils.lootrunUtils.lootrunStart",
                                                start.getX(),
                                                start.getY(),
                                                start.getZ())
                                        .withStyle(ChatFormatting.GREEN),
                                false);
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 1;
    }

    private int recordLootrun(CommandContext<CommandSourceStack> context) {
        LootrunUtils.register();
        if (LootrunUtils.recording == null) {
            LootrunUtils.recording = new LootrunUncompiled(new ArrayList<>(), new HashSet<>(), new ArrayList<>(), null);
            LootrunUtils.recordingInformation = new RecordingInformation();
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
            LootrunUtils.lootrun = LootrunUtils.recordingCompiled;
            LootrunUtils.uncompiled = LootrunUtils.recording;
            LootrunUtils.recording = null;
            LootrunUtils.recordingCompiled = null;
            LootrunUtils.recordingInformation = null;
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
        File file = new File(LootrunUtils.LOOTRUNS, StringArgumentType.getString(context, "name") + ".json");
        LootrunUtils.uncompiled = new LootrunUncompiled(LootrunUtils.uncompiled, file);
        return LootrunUtils.saveLootrun(file, context.getSource());
    }

    private int addJsonLootrunNote(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Component text = ComponentArgument.getComponent(context, "text");
        return LootrunUtils.addNote(context.getSource(), text);
    }

    private int addTextLootrunNote(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Component text = new TextComponent(StringArgumentType.getString(context, "text"));
        return LootrunUtils.addNote(context.getSource(), text);
    }

    private int listLootrunNote(CommandContext<CommandSourceStack> context) {
        LootrunUncompiled current = LootrunUtils.getActiveLootrun();

        if (current == null) return 0;

        if (current.notes().isEmpty()) {
            context.getSource().sendFailure(new TranslatableComponent("feature.wynntils.lootrunUtils.listNoteNoNote"));
        } else {
            MutableComponent component = new TranslatableComponent("feature.wynntils.lootrunUtils.listNoteHeader");
            for (Pair<Vec3, Component> note : current.notes()) {
                BlockPos pos = new BlockPos(note.first());
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
                        .append(note.second());
            }
            context.getSource().sendSuccess(component, false);
        }
        return LootrunUtils.getEditLootrun(context.getSource(), false);
    }

    private int deleteLootrunNote(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        LootrunUncompiled current = LootrunUtils.getActiveLootrun();

        if (current == null) return 0;

        BlockPos pos = BlockPosArgument.getSpawnablePos(context, "pos");
        String posString = pos.toShortString();
        List<Pair<Vec3, Component>> notes = current.notes();
        for (int i = 0; i < notes.size(); i++) {
            Pair<Vec3, Component> note = notes.get(i);
            if (pos.equals(new BlockPos(note.first()))) {
                notes.remove(i);
                context.getSource()
                        .sendSuccess(
                                new TranslatableComponent(
                                                "feature.wynntils.lootrunUtils.noteRemovedSuccessfully", note.second())
                                        .withStyle(ChatFormatting.GREEN),
                                false);
                return 1;
            }
        }
        context.getSource()
                .sendFailure(new TranslatableComponent("feature.wynntils.lootrunUtils.noteUnableToFind", posString));
        return LootrunUtils.getEditLootrun(context.getSource(), true);
    }

    private int clearLootrun(CommandContext<CommandSourceStack> context) {
        if (LootrunUtils.lootrun == null && LootrunUtils.recording == null) {
            context.getSource().sendFailure(new TranslatableComponent("feature.wynntils.lootrunUtils.noActiveLootrun"));
            return 0;
        }

        LootrunUtils.unregister();

        LootrunUtils.lootrun = null;
        LootrunUtils.uncompiled = null;
        LootrunUtils.recording = null;
        LootrunUtils.recordingCompiled = null;
        LootrunUtils.recordingInformation = null;
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

        LootrunUncompiled current = LootrunUtils.getActiveLootrun();

        if (current == null) return 0;

        if (current.chests().add(pos)) {
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
        return LootrunUtils.getEditLootrun(context.getSource(), true);
    }

    private int removeChest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getSpawnablePos(context, "pos");

        LootrunUncompiled current = LootrunUtils.getActiveLootrun();

        if (current == null) return 0;

        if (current.chests().remove(pos)) {
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

        return LootrunUtils.getEditLootrun(context.getSource(), true);
    }

    private int undoLootrun(CommandContext<CommandSourceStack> context) {
        if (LootrunUtils.recording == null) {
            context.getSource().sendFailure(new TranslatableComponent("feature.wynntils.lootrunUtils.notRecording"));
            return 0;
        } else {
            Vec3 position = McUtils.player().position();
            List<Vec3> points = LootrunUtils.recording.points();
            List<Vec3> removed = new ArrayList<>();
            boolean left = false;
            for (int i = points.size() - 1; i >= 0; i--) {
                if (i == 0) {
                    if (left) {
                        context.getSource()
                                .sendFailure(new TranslatableComponent("feature.wynntils.lootrunUtils.undoStandNear"));
                    } else {
                        context.getSource()
                                .sendFailure(
                                        new TranslatableComponent("feature.wynntils.lootrunUtils.undoNotFarEnough"));
                    }
                    return 0;
                }

                if (points.get(i).distanceToSqr(position) < 4) {
                    if (left) {
                        break;
                    }
                } else {
                    left = true;
                }

                removed.add(points.get(i));
            }

            points.removeAll(removed);
            context.getSource()
                    .sendSuccess(new TranslatableComponent("feature.wynntils.lootrunUtils.undoSuccessful"), false);
            LootrunUtils.recordingInformation.setDirty(true);
            return removed.size();
        }
    }

    private int folderLootrun(CommandContext<CommandSourceStack> context) {
        Util.getPlatform().openFile(LootrunUtils.LOOTRUNS);
        return 1;
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(Commands.literal("lootrun")
                .then(Commands.literal("load")
                        .then(Commands.argument("lootrun", StringArgumentType.string())
                                .suggests(LOOTRUN_SUGGESTION_PROVIDER)
                                .executes(this::loadLootrun)))
                .then(Commands.literal("record").executes(this::recordLootrun))
                .then(Commands.literal("save")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(this::saveLootrun)))
                .then(Commands.literal("note")
                        .then(Commands.literal("add")
                                .then(Commands.literal("json")
                                        .then(Commands.argument("text", ComponentArgument.textComponent())
                                                .executes(this::addJsonLootrunNote)))
                                .then(Commands.literal("text")
                                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                                .executes(this::addTextLootrunNote))))
                        .then(Commands.literal("list").executes(this::listLootrunNote))
                        .then(Commands.literal("delete")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(this::deleteLootrunNote))))
                .then(Commands.literal("clear").executes(this::clearLootrun))
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests(LOOTRUN_SUGGESTION_PROVIDER)
                                .executes(this::deleteLootrun)))
                .then(Commands.literal("rename")
                        .then(Commands.argument("old", StringArgumentType.string())
                                .suggests(LOOTRUN_SUGGESTION_PROVIDER)
                                .then(Commands.argument("new", StringArgumentType.string())
                                        .executes(this::renameLootrun))))
                .then(Commands.literal("chest")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.literal("add").executes(this::addChest)))
                        .then(Commands.literal("remove").executes(this::removeChest)))
                .then(Commands.literal("undo").executes(this::undoLootrun))
                .then(Commands.literal("folder").executes(this::folderLootrun)));

        dispatcher.register(Commands.literal("lr").redirect(node));
    }

    private static LootrunUncompiled readJson(File file, JsonObject json) {
        JsonArray points = json.getAsJsonArray("points");
        List<Vec3> pointsList = new ArrayList<>();
        for (JsonElement element : points) {
            JsonObject pointJson = element.getAsJsonObject();
            Vec3 location = new Vec3(
                    pointJson.get("x").getAsDouble(),
                    pointJson.get("y").getAsDouble(),
                    pointJson.get("z").getAsDouble());
            pointsList.add(location);
        }
        JsonArray chestsJson = json.getAsJsonArray("chests");
        Set<BlockPos> chests = new HashSet<>();
        if (chestsJson != null) {
            for (JsonElement element : chestsJson) {
                JsonObject chestJson = element.getAsJsonObject();
                BlockPos pos = new BlockPos(
                        chestJson.get("x").getAsInt(),
                        chestJson.get("y").getAsInt(),
                        chestJson.get("z").getAsInt());
                chests.add(pos);
            }
        }
        JsonArray notesJson = json.getAsJsonArray("notes");
        List<Pair<Vec3, Component>> notes = new ArrayList<>();
        if (notesJson != null) {
            for (JsonElement element : notesJson) {
                JsonObject noteJson = element.getAsJsonObject();
                JsonObject positionJson = noteJson.getAsJsonObject("location");
                Vec3 position = new Vec3(
                        positionJson.get("x").getAsDouble(),
                        positionJson.get("y").getAsDouble(),
                        positionJson.get("z").getAsDouble());
                Component component = Component.Serializer.fromJson(noteJson.get("note"));
                Pair<Vec3, Component> note = new ObjectObjectImmutablePair<>(position, component);
                notes.add(note);
            }
        }
        return new LootrunUncompiled(pointsList, chests, notes, file);
    }
}
