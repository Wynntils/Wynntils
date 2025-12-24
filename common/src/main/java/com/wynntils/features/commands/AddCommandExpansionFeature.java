/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.commands;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.CommandSuggestionEvent;
import java.util.HashSet;
import java.util.Set;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Wynncraft provides suggestions for command arguments but not the roots, this feature
 * adds those suggestions.
 *
 * The commands in this file were extracted from https://wynncraft.wiki.gg/wiki/Commands,
 * from running the commands in-game, and from a list of server commands provided by HeyZeer0.
 */
@ConfigCategory(Category.COMMANDS)
public class AddCommandExpansionFeature extends Feature {
    @Persisted
    private final Config<Boolean> includeDeprecatedCommands = new Config<>(false);

    @Persisted
    private final Config<AliasCommandLevel> includeAliases = new Config<>(AliasCommandLevel.SHORT_FORMS);

    private final Set<String> suggestions = new HashSet<>();

    public AddCommandExpansionFeature() {
        super(new ProfileDefault.Builder().disableFor(ConfigProfile.BLANK_SLATE).build());
    }

    @SubscribeEvent
    public void onCommandSuggestions(CommandSuggestionEvent.Add event) {
        suggestions.stream().filter(s -> s.startsWith(event.getInput())).forEach(event::addSuggestion);
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        suggestions.clear();

        suggestions.add("claimingredientbomb");
        suggestions.add("claimitembomb");
        suggestions.add("daily");
        suggestions.add("fixquests");
        suggestions.add("fixstart");
        suggestions.add("forum");
        suggestions.add("help");
        suggestions.add("link");
        suggestions.add("rules");
        suggestions.add("sign");
        suggestions.add("skiptutorial");
        suggestions.add("tracking");

        // There is also a command "server" but it is reserved for those with admin permissions
        // only, so don't include it here.
        // The command "checknickname" is also available but is probably a defunct legacy command.

        // "hub" aliases
        suggestions.add("hub");
        addAlias("change", AliasCommandLevel.ALL);
        addAlias("lobby", AliasCommandLevel.ALL);
        addAlias("leave", AliasCommandLevel.ALL);
        addAlias("port", AliasCommandLevel.ALL);

        // There is also an alias "servers" for "hub", but it conflicts with our command
        // so don't include it here

        // "class" aliases
        suggestions.add("class");
        addAlias("classes", AliasCommandLevel.ALL);

        // "characters" aliases
        suggestions.add("characters");
        addAlias("char", AliasCommandLevel.SHORT_FORMS);

        // "crate" aliases
        suggestions.add("crate");
        addAlias("crates", AliasCommandLevel.ALL);

        // "disguises" aliases
        suggestions.add("disguises");
        addAlias("disguise", AliasCommandLevel.ALL);

        // "effects" aliases
        suggestions.add("effects");
        addAlias("effect", AliasCommandLevel.ALL);

        // "hats" aliases
        suggestions.add("hats");
        addAlias("hat", AliasCommandLevel.ALL);

        // "mounts" aliases
        suggestions.add("mounts");
        addAlias("mount", AliasCommandLevel.ALL);

        // "weapons" aliases
        suggestions.add("weapons");
        addAlias("weapon", AliasCommandLevel.ALL);

        // "consumables" aliases
        suggestions.add("consumables");
        addAlias("bomb", AliasCommandLevel.ALL);
        addAlias("bombs", AliasCommandLevel.ALL);
        addAlias("token", AliasCommandLevel.ALL);
        addAlias("tokens", AliasCommandLevel.ALL);
        addAlias("consumable", AliasCommandLevel.ALL);

        // "use" aliases
        suggestions.add("use");
        addAlias("rank", AliasCommandLevel.ALL);
        addAlias("shop", AliasCommandLevel.ALL);
        addAlias("store", AliasCommandLevel.ALL);

        // "kill" aliases
        suggestions.add("kill");
        addAlias("die", AliasCommandLevel.ALL);
        addAlias("suicide", AliasCommandLevel.ALL);

        // "itemlock" aliases
        suggestions.add("itemlock");
        addAlias("ilock", AliasCommandLevel.ALL);
        addAlias("lock", AliasCommandLevel.ALL);
        addAlias("locki", AliasCommandLevel.ALL);
        addAlias("lockitem", AliasCommandLevel.ALL);

        // "pet" aliases
        suggestions.add("pet");
        addAlias("pets", AliasCommandLevel.ALL);

        // "partyfinder" aliases
        suggestions.add("partyfinder");
        addAlias("pfinder", AliasCommandLevel.ALL);

        // "stream" aliases
        suggestions.add("stream");
        addAlias("streamer", AliasCommandLevel.ALL);

        // "totem" aliases
        suggestions.add("totem");
        addAlias("totems", AliasCommandLevel.ALL);

        // "hunted" aliases
        suggestions.add("hunted");
        addAlias("pvp", AliasCommandLevel.SHORT_FORMS);

        // "recruit" aliases
        suggestions.add("recruit");
        addAlias("rf", AliasCommandLevel.ALL);

        suggestions.add("changetag");

        suggestions.add("emote");
        addAlias("emotes", AliasCommandLevel.ALL);

        suggestions.add("friend");
        addAlias("f", AliasCommandLevel.SHORT_FORMS);
        addAlias("friends", AliasCommandLevel.ALL);
        addAlias("buddy", AliasCommandLevel.ALL);
        addAlias("buddies", AliasCommandLevel.ALL);

        suggestions.add("guild");
        addAlias("gu", AliasCommandLevel.SHORT_FORMS);
        addAlias("guilds", AliasCommandLevel.ALL);

        suggestions.add("ignore");

        suggestions.add("housing");
        addAlias("is", AliasCommandLevel.SHORT_FORMS);
        addAlias("hs", AliasCommandLevel.SHORT_FORMS);
        addAlias("home", AliasCommandLevel.ALL);
        addAlias("house", AliasCommandLevel.ALL);
        addAlias("island", AliasCommandLevel.ALL);
        addAlias("plot", AliasCommandLevel.ALL);

        suggestions.add("g");
        suggestions.add("p");
        suggestions.add("r");
        suggestions.add("msg");

        suggestions.add("report");
        suggestions.add("switch");
        suggestions.add("relore");
        suggestions.add("dialogue");
        suggestions.add("thankyou");

        suggestions.add("renameitem");
        addAlias("renameitems", AliasCommandLevel.ALL);

        suggestions.add("renamepet");
        addAlias("renamepets", AliasCommandLevel.ALL);

        suggestions.add("ironman");

        suggestions.add("particles");
        addAlias("pq", AliasCommandLevel.SHORT_FORMS);
        addAlias("particlequality", AliasCommandLevel.ALL);
        addAlias("particlesquality", AliasCommandLevel.ALL);

        suggestions.add("party");
        addAlias("pa", AliasCommandLevel.SHORT_FORMS);
        addAlias("group", AliasCommandLevel.ALL);

        suggestions.add("duel");
        addAlias("d", AliasCommandLevel.SHORT_FORMS);

        suggestions.add("trade");
        addAlias("tr", AliasCommandLevel.SHORT_FORMS);

        suggestions.add("find");

        suggestions.add("toggle");

        if (includeDeprecatedCommands.get()) {
            suggestions.add("legacystore");

            addAlias("buy", AliasCommandLevel.ALL);
            addAlias("cash", AliasCommandLevel.ALL);
            addAlias("cashshop", AliasCommandLevel.ALL);
            addAlias("gc", AliasCommandLevel.ALL);
            addAlias("gold", AliasCommandLevel.ALL);
            addAlias("goldcoins", AliasCommandLevel.ALL);
            addAlias("goldshop", AliasCommandLevel.ALL);

            suggestions.add("rename");
            addAlias("name", AliasCommandLevel.ALL);
        }
    }

    private void addAlias(String alias, AliasCommandLevel level) {
        if (includeAliases.get().ordinal() >= level.ordinal()) {
            suggestions.add(alias);
        }
    }

    public enum AliasCommandLevel {
        NONE,
        SHORT_FORMS,
        ALL
    }
}
