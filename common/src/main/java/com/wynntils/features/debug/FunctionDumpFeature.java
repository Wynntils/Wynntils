/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterCommand;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.utils.mc.McUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

@ConfigCategory(Category.DEBUG)
public class FunctionDumpFeature extends Feature {
    // names are used for tablenames, which cascades to function/argument cross referencing & any ORM types generated
    private static final String FUNCTION_NAME = "wynntilsFunction";
    private static final String ARGUMENT_NAME = "wynntilsArgument";
    private static final String DATA_VERSION_NAME = "wynntilsDataVersion";
    private static final Map<String, String> FUNCTION_MAP = new LinkedHashMap<>();
    private static final Map<String, String> ARGUMENT_MAP = new LinkedHashMap<>();
    private static final Map<String, String> DATA_VERSION_MAP = new LinkedHashMap<>();

    @RegisterCommand
    private final LiteralCommandNode<CommandSourceStack> dumpCommand = Commands.literal("dumpFunctions")
            .executes(ctx -> {
                FUNCTION_MAP.put("id", "serial PRIMARY KEY");
                FUNCTION_MAP.put("name", "VARCHAR(255) UNIQUE NOT NULL");
                FUNCTION_MAP.put("description", "TEXT NOT NULL");
                FUNCTION_MAP.put("aliases", "VARCHAR(255)[]");
                FUNCTION_MAP.put("returntype", "type NOT NULL");

                ARGUMENT_MAP.put("id", "serial PRIMARY KEY");
                ARGUMENT_MAP.put("name", "VARCHAR(255) NOT NULL");
                ARGUMENT_MAP.put("description", "TEXT NOT NULL");
                ARGUMENT_MAP.put("required", "BOOLEAN NOT NULL");
                ARGUMENT_MAP.put("functionid", "INTEGER REFERENCES " + FUNCTION_NAME + "(id)");
                ARGUMENT_MAP.put("type", "type NOT NULL");
                ARGUMENT_MAP.put("defaultvalue", "VARCHAR(255)");

                DATA_VERSION_MAP.put("id", "serial PRIMARY KEY");
                DATA_VERSION_MAP.put("harvestedat", "BIGINT NOT NULL");
                DATA_VERSION_MAP.put("modversion", "VARCHAR(255) NOT NULL");

                dumpFunctionsToCSV();
                dumpArgumentsToCSV();
                dumpDataVersionToCSV();
                copyPreparationStatement();
                return 0;
            })
            .build();

    public FunctionDumpFeature() {
        super(ProfileDefault.DISABLED);
    }

    private void dumpFunctionsToCSV() {
        List<String[]> dataLines = new ArrayList<>();
        dataLines.add(FUNCTION_MAP.keySet().toArray(new String[0]));

        for (Function<?> function : Managers.Function.getFunctions()) {
            String aliases = "{" + String.join(",", function.getAliasList()) + "}";
            String[] dataLine = {
                String.valueOf(dataLines.size()),
                function.getName(),
                function.getDescription(),
                aliases,
                function.getReturnTypeName()
            };
            dataLines.add(dataLine);
        }

        writeToCSV(dataLines, FUNCTION_NAME);
    }

    private void dumpArgumentsToCSV() {
        List<String[]> dataLines = new ArrayList<>();
        dataLines.add(ARGUMENT_MAP.keySet().toArray(new String[0]));

        for (int i = 0; i < Managers.Function.getFunctions().size(); i++) {
            Function<?> function = Managers.Function.getFunctions().get(i);
            for (Argument<?> argument : function.getArgumentsBuilder().getArguments()) {
                String[] dataLine = {
                    String.valueOf(dataLines.size()),
                    argument.getName(),
                    function.getArgumentDescription(argument.getName()),
                    String.valueOf(function.getArgumentsBuilder() instanceof FunctionArguments.RequiredArgumentBuilder),
                    String.valueOf(i + 1),
                    argument.getType().getSimpleName(),
                    String.valueOf(argument.getDefaultValue())
                };
                dataLines.add(dataLine);
            }
        }

        writeToCSV(dataLines, ARGUMENT_NAME);
    }

    private void dumpDataVersionToCSV() {
        List<String[]> dataLines = new ArrayList<>();
        dataLines.add(DATA_VERSION_MAP.keySet().toArray(new String[0]));

        String modVersion = WynntilsMod.getVersion();

        String[] dataLine = {
            "1",
            String.valueOf(System.currentTimeMillis()),
            modVersion
        };
        dataLines.add(dataLine);

        writeToCSV(dataLines, DATA_VERSION_NAME);
    }

    private void writeToCSV(List<String[]> dataLines, String name) {
        File csvOutputFile = new File(name + ".csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile, StandardCharsets.UTF_8)) {
            dataLines.stream()
                    .map(line -> Stream.of(line)
                            .map(d -> {
                                String value = String.valueOf(d);
                                String escapedData = value.replaceAll("\\R", " ");
                                if (value.contains(",") || value.contains("\"") || value.contains("'")) {
                                    value = value.replace("\"", "\"\"");
                                    escapedData = "\"" + value + "\"";
                                }
                                return escapedData;
                            })
                            .collect(Collectors.joining(",")))
                    .forEach(pw::println);
        } catch (IOException e) {
            McUtils.sendErrorToClient("Failed to write " + name + " to CSV");
        }
        McUtils.sendMessageToClient(Component.literal(
                ChatFormatting.GREEN + "Wrote " + name + " to CSV at " + csvOutputFile.getAbsolutePath()));
    }

    private void copyPreparationStatement() {
        String clearDatabase = "DROP SCHEMA public CASCADE; CREATE SCHEMA public;";

        // add all return types to possible types
        Set<String> typeNames = Managers.Function.getFunctions().stream()
                .map(Function::getReturnTypeName)
                .collect(Collectors.toSet());

        // and additionally all argument types, which catches things like List not being a return type
        Managers.Function.getFunctions().stream()
                .map(Function::getArgumentsBuilder)
                .map(FunctionArguments.Builder::getArguments)
                .forEach(arguments -> arguments.forEach(
                        argument -> typeNames.add(argument.getType().getSimpleName())));

        String makeTypeEnum = "CREATE TYPE type AS ENUM ("
                + typeNames.stream().map(name -> "'" + name + "'").collect(Collectors.joining(",")) + ");";

        // order-preserving
        String makeFunctionTable = "CREATE TABLE " + FUNCTION_NAME + " ("
                + FUNCTION_MAP.entrySet().stream()
                        .map(entry -> entry.getKey() + " " + entry.getValue())
                        .collect(Collectors.joining(","))
                + ");";

        String makeArgumentTable = "CREATE TABLE " + ARGUMENT_NAME + " ("
                + ARGUMENT_MAP.entrySet().stream()
                        .map(entry -> entry.getKey() + " " + entry.getValue())
                        .collect(Collectors.joining(","))
                + ");";

        String makeDataVersionTable = "CREATE TABLE " + DATA_VERSION_NAME + " ("
                + DATA_VERSION_MAP.entrySet().stream()
                        .map(entry -> entry.getKey() + " " + entry.getValue())
                        .collect(Collectors.joining(","))
                + ");";

        McUtils.mc().keyboardHandler.setClipboard(
                clearDatabase + makeTypeEnum + makeFunctionTable + makeArgumentTable + makeDataVersionTable);
        McUtils.sendMessageToClient(Component.literal("\n")
                .append(Component.literal(
                        ChatFormatting.GREEN + "Copied database preparation statement to clipboard.\n"))
                .append(Component.literal(ChatFormatting.GRAY + "Run this statement before importing new CSVs.\n"))
                .append(Component.literal(ChatFormatting.GRAY
                        + "Import CSVs using pgAdmin 4 ensuring Header option is checked and encoding is UTF-8.\n"))
                .append(Component.literal(ChatFormatting.GRAY + "Import functions before arguments.\n"))
                .append(Component.literal(ChatFormatting.GRAY + "Import data version CSV after function data.")));
    }
}
