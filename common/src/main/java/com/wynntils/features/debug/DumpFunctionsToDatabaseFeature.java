/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterCommand;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ConfigCategory(Category.DEBUG)
public class DumpFunctionsToDatabaseFeature extends Feature {
    private static final String DB_URL = "jdbc:postgresql://ep-morning-frost-16595280.us-west-2.aws.neon.tech/functiondb";
    private static final String DB_USER = "wynntils";
    private static final String DB_PASS = "We0NFqXVPo2n";

    private Connection connection = null;

    @RegisterCommand
    private final LiteralCommandNode<CommandSourceStack> dumpCommand = Commands.literal("dumpFunctionsToDatabase")
            .executes(ctx -> {
                dumpFunctionstoDatabase();
                return 0;
            })
            .build();

    private void dumpFunctionstoDatabase() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }

        String clearDatabase = "DROP SCHEMA public CASCADE; CREATE SCHEMA public;";
        try (PreparedStatement clearDatabaseStatement = connection.prepareStatement(clearDatabase)) {
            clearDatabaseStatement.execute();
            McUtils.sendMessageToClient(Component.literal(ChatFormatting.GREEN + "Cleared database"));
        } catch (SQLException e) {
            McUtils.sendErrorToClient("Failed to clear database");
            e.printStackTrace();
            return;
        }

        Set<String> typeNames = Managers.Function.getFunctions().stream().map(function -> function.getFunctionType().getSimpleName()).collect(Collectors.toSet());
        String enumString = typeNames.stream().map(name -> "'" + name + "'").collect(Collectors.joining(","));

        String makeTypeEnum = "CREATE TYPE type AS ENUM (" + enumString + ");";
        try (PreparedStatement makeTypeEnumStatement = connection.prepareStatement(makeTypeEnum)) {
            makeTypeEnumStatement.execute();
            McUtils.sendMessageToClient(Component.literal(ChatFormatting.GREEN + "Created type enum"));
        } catch (SQLException e) {
            McUtils.sendErrorToClient("Failed to create type enum");
            e.printStackTrace();
            return;
        }

        // no more categories because i do not not want to deal with reflection
        String makeFunctionTable = "CREATE TABLE Functions (" +
                "id serial PRIMARY KEY," +
                "name VARCHAR(255) UNIQUE NOT NULL," +
                "description TEXT NOT NULL," +
                "aliases VARCHAR(255)[]," +
                "returnType type NOT NULL);";
        try (PreparedStatement makeFunctionStatement = connection.prepareStatement(makeFunctionTable)) {
            makeFunctionStatement.execute();
            McUtils.sendMessageToClient(Component.literal(ChatFormatting.GREEN + "Created function table"));
        } catch (SQLException e) {
            McUtils.sendErrorToClient("Failed to create function table");
            e.printStackTrace();
            return;
        }

        String makeArgumentTable = "CREATE TABLE Arguments (" +
                "id serial PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "description TEXT NOT NULL," +
                "required BOOLEAN NOT NULL," +
                "functionId INTEGER REFERENCES Functions(id)," +
                "type type NOT NULL);";
        try (PreparedStatement makeArgumentStatement = connection.prepareStatement(makeArgumentTable)) {
            makeArgumentStatement.execute();
            McUtils.sendMessageToClient(Component.literal(ChatFormatting.GREEN + "Created argument table"));
        } catch (SQLException e) {
            McUtils.sendErrorToClient("Failed to create argument table");
            e.printStackTrace();
            return;
        }

        // stuff is created, time to start dumping
        Managers.Function.getFunctions().forEach(function -> {
            // id is auto generated
            String insertFunction = "INSERT INTO Functions (name, description, aliases, returnType) VALUES (?, ?, ?, ?::type);";
            String name = function.getName();
            String description = function.getDescription();
            List<String> aliases = function.getAliases();
            String returnType = function.getFunctionType().getSimpleName();

            try (PreparedStatement insertFunctionStatement = connection.prepareStatement(insertFunction)) {
                insertFunctionStatement.setString(1, name);
                insertFunctionStatement.setString(2, description);
                insertFunctionStatement.setArray(3, connection.createArrayOf("VARCHAR", aliases.toArray()));
                insertFunctionStatement.setString(4, returnType);
                insertFunctionStatement.execute();
            } catch (SQLException e) {
                McUtils.sendErrorToClient("Failed to insert function " + name);
                e.printStackTrace();
                return;
            }
        });

        Managers.Function.getFunctions().forEach(function -> function.getArgumentsBuilder().getArguments().forEach(argument -> {
            String insertArgument = "INSERT INTO Arguments (name, description, required, functionId, type) VALUES (?, ?, ?, ?, ?::type);";
            String name = argument.getName();
            String description = function.getTranslation("argument." + argument.getName());
            boolean required = function.getArgumentsBuilder() instanceof FunctionArguments.RequiredArgumentBuilder;
            String type = argument.getType().getSimpleName();

            // get function id from populated function table
            String getFunctionId = "SELECT id FROM Functions WHERE name = ?;";
            int functionId;
            try (PreparedStatement getFunctionIdStatement = connection.prepareStatement(getFunctionId)) {
                getFunctionIdStatement.setString(1, function.getName());
                getFunctionIdStatement.execute();
                getFunctionIdStatement.getResultSet().next();
                functionId = getFunctionIdStatement.getResultSet().getInt("id");
            } catch (SQLException e) {
                McUtils.sendErrorToClient("Failed to get function id for " + function.getName());
                e.printStackTrace();
                return;
            }

            try (PreparedStatement insertArgumentStatement = connection.prepareStatement(insertArgument)) {
                insertArgumentStatement.setString(1, name);
                insertArgumentStatement.setString(2, description);
                insertArgumentStatement.setBoolean(3, required);
                insertArgumentStatement.setInt(4, functionId);
                insertArgumentStatement.setString(5, type);
                insertArgumentStatement.execute();
            } catch (SQLException e) {
                McUtils.sendErrorToClient("Failed to insert argument " + name);
                e.printStackTrace();
                return;
            }
        }));

        McUtils.sendMessageToClient(Component.literal(ChatFormatting.GREEN + "Dumped functions to database"));

    }
}
